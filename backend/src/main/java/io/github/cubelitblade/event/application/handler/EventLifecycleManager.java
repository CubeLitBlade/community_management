package io.github.cubelitblade.event.application.handler;

import io.github.cubelitblade.common.time.TimeProvider;
import io.github.cubelitblade.event.application.EventRetryPolicy;
import io.github.cubelitblade.event.model.Event;
import io.github.cubelitblade.event.persistence.EventRepository;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventLifecycleManager implements EventStepper {
  private final EventRepository eventRepository;
  private final TimeProvider timeProvider;
  private final EventRetryPolicy eventRetryPolicy;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void reschedule(Event event, String reason) {
    int retryCount = event.getRetryCount();

    if (eventRetryPolicy.canRetry(retryCount)) {
      Duration backoff = eventRetryPolicy.getExponentialBackoffDuration(retryCount);

      event.retry(timeProvider.now().plus(backoff), reason, timeProvider.now());
      log.warn(
          "[Event #{}]: Scheduled to retry at {} (after {} ms), because {}. ",
          event.getId(),
          event.getNextRunAt(),
          backoff,
          reason);
    } else {
      event.die(
          "The maximum number of retries has been reached, because " + reason, timeProvider.now());
      log.error(
          "[Event #{}]: The maximum number of retries has been reached, marked as dead. ",
          event.getId());
    }

    this.persist(event);
  }

  public void revive(Event event, Instant nextRunAt) {
    event.revive(timeProvider.now(), nextRunAt);
    this.persist(event);
  }

  public boolean run(Event event) {
    event.run(timeProvider.now());
    return this.persist(event);
  }

  public void complete(Event event) {
    event.succeed(timeProvider.now());
    this.persist(event);
  }

  public void abort(Event event, String reason) {
    event.fail(reason, timeProvider.now());
    this.persist(event);
  }

  public void giveUp(Event event, String reason) {
    event.die(reason, timeProvider.now());
    this.persist(event);
  }

  @Override
  public void advanceEventToStep(Event event, String targetStep) {
    event.advanceTo(targetStep, timeProvider.now());
    this.persist(event);
  }

  private boolean persist(Event event) {
    boolean updated = eventRepository.tryUpdate(event);

    if (!updated) {
      log.warn(
          "[Event #{}]: Lifecycle state change failed (optimistic lock conflict)", event.getId());
    }

    return updated;
  }
}
