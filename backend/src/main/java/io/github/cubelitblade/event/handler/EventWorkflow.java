package io.github.cubelitblade.event.handler;

import io.github.cubelitblade.common.exception.FatalEventException;
import io.github.cubelitblade.configuration.RetryConfig;
import io.github.cubelitblade.event.Event;
import io.github.cubelitblade.event.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventWorkflow {
    private final EventService eventService;
    private final RetryConfig retryConfig;
    
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

        if (retryCount < retryConfig.getMaxRetries()) {
            long targetBackoffMills = retryConfig.getBaseDelay().toMillis() * (1L << retryCount);
            event.setNextRunAt(Instant.now().plusMillis(Math.min(targetBackoffMills, retryConfig.getMaxDelay().toMillis())));
            event.setRetryCount(retryCount + 1);
            event.setStatus(Event.EventStatus.WAITING);
            event.setCurrentStep(null);
            log.warn("Event #{}: Scheduled to retry at {} (after {} ms), because {}. ", event.getId(), event.getNextRunAt(), targetBackoffMills, reason);
        } else {
            die(event, "The maximum number of retries has been reached, because " + reason);
            log.error("[Event #{}]: The maximum number of retries has been reached, marked as dead. ", event.getId());
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
    public void succeed(Event event) {
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
     * @param event  the event to update
     * @param reason the error message describing why the event is dead
     */
    public void die(Event event, String reason) {
        event.setStatus(Event.EventStatus.DEAD);
        event.setErrorMsg(reason);
        event.setNextRunAt(null);
    }

    /**
     * Advance the event to a critical step and persist it.
     *
     * <p><b>Note:</b> This method should <u>only</u> be used for key steps
     * in the event workflow that must be persisted immediately to support
     * resume/recovery. Avoid using this for trivial or intermediate steps
     * to prevent excessive database writes.</p>
     *
     * @param event the event to update
     * @param step  the critical step to set
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkpoint(Event event, String step) {
        event.setCurrentStep(step);
        eventService.updateEventStep(event);
    }

    @Transactional
    public void commit(Event event) {
        try {
            eventService.updateEvent(event);
        } catch (RuntimeException e) {
            log.error("[Event #{}]: Failed to commit event: {}", event.getId(), event.getErrorMsg(), e);
            throw new FatalEventException("Failed to update event", e);
        }
    }
}
