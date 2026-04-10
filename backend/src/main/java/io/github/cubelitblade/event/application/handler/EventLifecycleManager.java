package io.github.cubelitblade.event.application.handler;

import io.github.cubelitblade.configuration.TimeConfig;
import io.github.cubelitblade.event.application.EventRetryPolicy;
import io.github.cubelitblade.event.application.EventService;
import io.github.cubelitblade.event.model.Event;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventLifecycleManager implements EventStepper, EventFinalizer, EventScheduler {
  private final EventService eventService;
  private final TimeConfig timeConfig;
  private final EventRetryPolicy eventRetryPolicy;

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void reschedule(Event event, String reason) {
    int retryCount = event.getRetryCount();

    if (eventRetryPolicy.canRetry(retryCount)) {
      Duration backoff = eventRetryPolicy.getExponentialBackoffDuration(retryCount);

      event.retry(timeConfig.now().plus(backoff), reason, timeConfig.now());
      log.warn(
          "[Event #{}]: Scheduled to retry at {} (after {} ms), because {}. ",
          event.getId(),
          event.getNextRunAt(),
          backoff,
          reason);
    } else {
      event.die(
          "The maximum number of retries has been reached, because " + reason, timeConfig.now());
      log.error(
          "[Event #{}]: The maximum number of retries has been reached, marked as dead. ",
          event.getId());
    }

    this.persist(event);
  }

  /**
   * Marks the event as successfully processed.
   *
   * <p>Sets status to {@code SUCCEEDED} and clears {@code nextRunAt}.
   *
   * @param event the event to update
   */
  @Override
  public void complete(Event event) {
    event.succeed(timeConfig.now());
    this.persist(event);
  }

  /**
   * Marks the event as permanently failed (non-recoverable).
   *
   * <p>Sets status to {@code FAILED}, records the error reason, and clears {@code nextRunAt}.
   *
   * @param event the event to update
   * @param reason the error message describing the failure
   */
  @Override
  public void abort(Event event, String reason) {
    event.fail(reason, timeConfig.now());
    this.persist(event);
  }

  /**
   * Marks the event as dead (exhausted retries).
   *
   * <p>Sets status to {@code DEAD}, records the error reason, and clears {@code nextRunAt}.
   *
   * @param event the event to update
   * @param reason the error message describing why the event is dead
   */
  @Override
  public void giveUp(Event event, String reason) {
    event.die(reason, timeConfig.now());
    this.persist(event);
  }

  /**
   * Advance the event to a critical step and persist it.
   *
   * <p><b>Note:</b> This method should <u>only</u> be used for key steps in the event workflow that
   * must be persisted immediately to support resume/recovery. Avoid using this for trivial or
   * intermediate steps to prevent excessive database writes.
   *
   * @param event the event to update
   * @param targetStep the critical step to set
   */
  @Override
  public void advanceEventToStep(Event event, String targetStep) {
    event.advanceTo(targetStep, timeConfig.now());
    this.persist(event);
  }

  private void persist(Event event) {
    eventService.updateEvent(event);
  }
}
