package io.github.cubelitblade.worker;

import io.github.cubelitblade.entity.Event;
import io.github.cubelitblade.service.EventService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class Worker {
    private final EventService eventService;
    private final EventDispatcher eventDispatcher;

    @Scheduled(fixedDelay = 5000)
    public void run() {
        log.info("Worker running...");

        List<Event> eventList = eventService.claimEvents(10);

        log.info("Event list size: {}", eventList.size());

        for (Event event : eventList) {
            eventDispatcher.dispatch(event);
        }
    }
}
