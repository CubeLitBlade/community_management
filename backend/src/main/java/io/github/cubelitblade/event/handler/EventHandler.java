package io.github.cubelitblade.event.handler;

import io.github.cubelitblade.configuration.RetryConfig;
import io.github.cubelitblade.entity.Event;
import io.github.cubelitblade.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public abstract class EventHandler {
    protected final EventService eventService;
    protected final ObjectMapper objectMapper;
    protected final RetryConfig retryConfig;

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
            event.setNextRunAt(null);
            event.setStatus(Event.EventStatus.DEAD);
            log.debug("Event #{}: The maximum number of retries has been reached, marked as dead. ",  event.getId());
        }
    }
}
