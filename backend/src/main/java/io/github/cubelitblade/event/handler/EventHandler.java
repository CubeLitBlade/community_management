package io.github.cubelitblade.event.handler;

import io.github.cubelitblade.common.exception.UnrecoverableEventException;
import io.github.cubelitblade.event.Event;
import io.github.cubelitblade.event.payload.EventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public abstract class EventHandler<PayloadType extends EventPayload> {
    protected final EventHandlerContext context;

    /**
     * Returns the concrete class of the payload type.
     * <p>
     * This is primarily used for JSON deserialization in {@link #parsePayload(Event)}.
     * </p>
     *
     * @return the {@link Class} object of {@code PayloadType}
     */
    public abstract Class<PayloadType> getPayloadType();

    /**
     * Returns the type of event this handler processes.
     *
     * @return the {@link io.github.cubelitblade.event.Event.EventType} enum value
     */
    public abstract Event.EventType getEventType();

    /**
     * The core business logic method to be implemented by subclasses.
     * <p>
     * This method is called by {@link #handleEvent(Event)} when the event status is {@code RUNNING}.
     * Implementations should perform business operations and update the event entity state
     * (e.g., call {@link #success(Event)} or modify payload).
     * </p>
     * <p>
     * <b>Note:</b> If this method throws an exception, the transaction in {@link #handleEvent(Event)}
     * might roll back depending on the caller's configuration, and the event may not be updated automatically.
     * Consider handling exceptions internally or letting them propagate to a global exception handler.
     * </p>
     *
     * @param event the event to process
     */
    public abstract void process(Event event);

    /**
     * Main entry point for handling an event.
     * <p>
     * Checks if the event is in {@code RUNNING} state, delegates to {@link #process(Event)},
     * and persists the updated event state to the database.
     * </p>
     *
     * @param event the event to handle
     */
    public void handleEvent(Event event) {
        if (event.getStatus() != Event.EventStatus.RUNNING) {
            return;
        }
        try {
            process(event);
            if (event.getStatus() == Event.EventStatus.RUNNING) {
                event.setStatus(Event.EventStatus.SUCCEEDED);
            }
        } catch (UnrecoverableEventException e) {
            die(event, e.getMessage());
            log.error(e.getMessage(), e);
        } catch (RuntimeException e) {
            scheduleRetry(event, e.getMessage());
            log.error(e.getMessage(), e);
        } finally {
            context.getEventService().updateById(event);
        }
    }

    /**
     * Schedules the next retry for the event using an exponential backoff algorithm.
     * <p>
     * Logic:
     * <ul>
     *     <li>If retries are not exhausted: calculates next run time ({@code baseDelay * 2^retryCount}),
     *         increments retry count, sets status to {@code WAITING}.</li>
     *     <li>If max retries reached: marks the event as {@code DEAD} via {@link #die(Event, String)}.</li>
     * </ul>
     * </p>
     *
     * @param event the event to schedule for retry
     */
    public void scheduleRetry(Event event, String reason) {
        int retryCount = event.getRetryCount();

        if (retryCount < context.getRetryConfig().getMaxRetries()) {
            long targetBackoffMills = context.getRetryConfig().getBaseDelay().toMillis() * (1L << retryCount);
            event.setNextRunAt(Instant.now().plusMillis(Math.min(targetBackoffMills, context.getRetryConfig().getMaxDelay().toMillis())));
            event.setRetryCount(retryCount + 1);
            event.setStatus(Event.EventStatus.WAITING);
            log.warn("Event #{}: Scheduled to retry at {} (after {} ms), because {}. ", event.getId(), event.getNextRunAt(), targetBackoffMills, reason);
        }
        else {
            die(event, "The maximum number of retries has been reached, because " + reason);
            log.error("Event #{}: The maximum number of retries has been reached, marked as dead. ", event.getId());
        }
    }

    /**
     * Marks the event as successfully processed.
     * <p>
     * Sets status to {@code SUCCEEDED} and clears {@code nextRunAt}.
     * </p>
     *
     * @param event the event to update
     */
    public void success(Event event) {
        event.setStatus(Event.EventStatus.SUCCEEDED);
        event.setNextRunAt(null);
    }

    /**
     * Marks the event as permanently failed (non-recoverable).
     * <p>
     * Sets status to {@code FAILED}, records the error reason, and clears {@code nextRunAt}.
     * </p>
     *
     * @param event  the event to update
     * @param reason the error message describing the failure
     */
    public void fail(Event event, String reason) {
        event.setStatus(Event.EventStatus.FAILED);
        event.setErrorMsg(reason);
        event.setNextRunAt(null);
    }

    /**
     * Marks the event as dead (exhausted retries).
     * <p>
     * Sets status to {@code DEAD}, records the error reason, and clears {@code nextRunAt}.
     * </p>
     *
     * @param event the event to update
     * @param reason the error message describing why the event is dead
     */
    public void die(Event event, String reason) {
        event.setStatus(Event.EventStatus.DEAD);
        event.setErrorMsg(reason);
        event.setNextRunAt(null);
    }

    /**
     * Deserializes the event's payload JSONB data into the typed payload object.
     * <p>
     * This method delegates to {@link io.github.cubelitblade.event.payload.EventPayloadMapper#fromJsonNode(JsonNode, Class)},
     * using the type provided by {@link #getPayloadType()}.
     * </p>
     *
     * @param event the event containing the payload JSONB data
     * @return the deserialized payload object of type {@code PayloadType}
     * @throws RuntimeException if the payload structure is invalid or deserialization fails
     */
    public PayloadType parsePayload(Event event) {
        return context.getEventPayloadMapper().fromJsonNode(event.getPayload(), getPayloadType());
    }

    /**
     * Serializes the given payload object into a Jackson {@link JsonNode}.
     * <p>
     * Useful for persisting or updating the event's payload in the database.
     * Handles {@code null} payloads gracefully by returning {@code null}.
     * </p>
     *
     * @param payload the payload object to serialize
     * @return a {@link JsonNode} representing the payload, or {@code null} if payload is {@code null}
     * @throws RuntimeException if serialization fails
     */
    public JsonNode serializePayload(PayloadType payload) {
        return context.getEventPayloadMapper().toJsonNode(payload);
    }

    /**
     * Converts the given payload object to its JSON string representation.
     * <p>
     * Useful for logging or debugging.
     * </p>
     *
     * @param payload the payload object to convert
     * @return the JSON string representation, or {@code null} if payload is {@code null}
     * @throws RuntimeException if serialization fails
     */
    public String payloadToString(PayloadType payload) {
        return context.getEventPayloadMapper().toJsonString(payload);
    }
}
