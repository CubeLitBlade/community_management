package io.github.cubelitblade.event.infra.worker;

import io.github.cubelitblade.configuration.TimeConfig;
import io.github.cubelitblade.event.application.EventService;
import io.github.cubelitblade.event.model.Event;
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
  private final TimeConfig timeConfig;

  @Scheduled(fixedDelay = 5000)
  public void run() {
    eventService.resetZombieEvents(timeConfig.now().minusSeconds(600));
    List<Event> eventList = eventService.claimWaitingEvents(10);

    if (!eventList.isEmpty()) {
      log.debug("Event list size: {}", eventList.size());
    }

    for (Event event : eventList) {
      eventDispatcher.dispatch(event);
    }
  }
}
