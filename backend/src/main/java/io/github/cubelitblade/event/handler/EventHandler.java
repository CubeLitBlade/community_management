package io.github.cubelitblade.event.handler;

import java.time.Instant;

import org.springframework.stereotype.Component;

import io.github.cubelitblade.configuration.RetryConfig;
import io.github.cubelitblade.event.Event;
import io.github.cubelitblade.event.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public abstract class EventHandler<PayloadType> {
    protected final EventService eventService;
    protected final ObjectMapper objectMapper;
    protected final RetryConfig retryConfig;

    public abstract Class<PayloadType> getPayloadType();
    public abstract Event.EventType getEventType();

    /**
     * Handles the business logic of the event.
     * <p>
     * Subclasses must implement this method to define the specific processing logic.
     * </p>
     *
     * @param event the event to process
     * @throws RuntimeException if processing fails
     */
    public abstract void process(Event event);

    /**
     * Processes the event and updates it in the database.
     * <p>
     * This method calls {@link #process(Event)} and then persists the updated event state.
     * </p>
     *
     * @param event the event to handle
     */
    public void handleEvent(Event event) {
        if (event.getStatus() == Event.EventStatus.RUNNING) {
            process(event);
            eventService.updateById(event);
        }
    }

    /**
     * Schedules the next retry for the event using an exponential backoff algorithm.
     * <p>
     * If the event has not exceeded the maximum number of retries, this method calculates
     * the next retry time based on exponential backoff, increments the retry count,
     * and sets the event status to {@code WAITING}.
     * If the maximum retries have been reached, the event is marked as {@code DEAD}
     * and no further retries are scheduled.
     * </p>
     *
     * @param event the event to schedule for retry
     */
    public void scheduleRetry(Event event) {
        int retryCount = event.getRetryCount();

        if (retryCount < retryConfig.getMaxRetries()) {
            long targetBackoffMills = retryConfig.getBaseDelay().toMillis() * (1L << retryCount);
            event.setNextRunAt(Instant.now().plusMillis(Math.min(targetBackoffMills, retryConfig.getMaxDelay().toMillis())));
            event.setRetryCount(retryCount + 1);
            event.setStatus(Event.EventStatus.WAITING);
            log.debug("Event #{}: Scheduled to retry at {} (after {} ms). ", event.getId(), event.getNextRunAt(), targetBackoffMills);
        }
        else {
            die(event, "The maximum number of retries has been reached. ");
            log.debug("Event #{}: The maximum number of retries has been reached, marked as dead. ",  event.getId());
        }
    }

    public void success(Event event) {
        event.setStatus(Event.EventStatus.SUCCEEDED);
        event.setNextRunAt(null);
    }

    public void fail(Event event, String reason) {
        event.setStatus(Event.EventStatus.FAILED);
        event.setErrorMsg(reason);
        event.setNextRunAt(null);
    }

    public void die(Event event, String reason) {
        event.setStatus(Event.EventStatus.DEAD);
        event.setErrorMsg(reason);
        event.setNextRunAt(null);
    }

    /**
     * Parses the payload of the given event into the expected {@code PayloadType}.
     * <p>
     * Uses Jackson's {@link ObjectMapper#convertValue(Object, Class)} internally to
     * convert the {@link Event#getPayload()} JSONB data into the typed payload object.
     * </p>
     * <p>
     * If the conversion fails (e.g., payload structure mismatch), a warning/error is logged
     * and {@code null} is returned. Depending on your business logic, the caller may
     * choose to handle a {@code null} payload or throw an exception.
     * </p>
     *
     * @param event the event containing the JSONB payload to parse
     * @return the payload object of type {@code PayloadType}, or {@code null} if parsing fails
     */
    public PayloadType parsePayload(Event event) {
        try {
            return objectMapper.convertValue(event.getPayload(), getPayloadType());
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse payload for event #{}: {}", event.getId(), event.getPayload(), e);
            return null;
        }
    }

    /**
     * Serializes the given payload object into a Jackson {@link JsonNode}.
     * <p>
     * Useful for persisting or updating the event's payload in the database, which
     * expects JSONB data. If the payload is {@code null}, this method returns {@code null}.
     * </p>
     * <p>
     * Any serialization exceptions are logged and {@code null} is returned.
     * </p>
     *
     * @param payload the payload object to serialize
     * @return a {@link JsonNode} representing the payload, or {@code null} if serialization fails or payload is {@code null}
     */
    public JsonNode serializePayload(PayloadType payload) {
        try {
            return payload == null ? null : objectMapper.valueToTree(payload);
        } catch (JacksonException e) {
            log.error("Failed to serialize payload {}: {}", payload, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Converts the given payload object to its JSON string representation.
     * <p>
     * This is mainly for logging, debugging, or storing payloads as JSON strings.
     * If the payload is {@code null} or serialization fails, {@code null} is returned.
     * </p>
     *
     * @param payload the payload object to convert
     * @return the JSON string representation of the payload, or {@code null} if serialization fails
     */
    public String payloadToString(PayloadType payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException e) {
            log.error("Failed to serialize payload {}: {}", payload, e.getMessage(), e);
            return null;
        }
    }
}
