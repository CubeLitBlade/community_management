package io.github.cubelitblade.event.infra.worker;

import io.github.cubelitblade.configuration.TimeConfig;
import io.github.cubelitblade.event.application.EventService;
import io.github.cubelitblade.event.application.handler.EventLifecycleManager;
import io.github.cubelitblade.event.model.Event;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Worker {
  private final EventService eventService;
  private final EventDispatcher eventDispatcher;
  private final EventLifecycleManager eventLifecycleManager;
  private final TimeConfig timeConfig;

  @Scheduled(fixedDelay = 5000)
  public void run() {
    Instant now = timeConfig.now();

    resetZombieEvents(now, Duration.ofSeconds(600));

    for (Event event : claimWaitingEvents()) {
      eventDispatcher.dispatch(event);
    }
  }

  private void resetZombieEvents(Instant now, Duration durationAgo) {
    List<Event> zombieEvents = eventService.getZombieEvents(10, now.minus(durationAgo));

    for (Event event : zombieEvents) {
      eventLifecycleManager.revive(event, now);
      log.warn("[Event #{}]: Reset zombie event, requeue for execution.", event.getId());
    }
  }

  private List<Event> claimWaitingEvents() {
    return eventService.getWaitingEvents(10);
  }
}
