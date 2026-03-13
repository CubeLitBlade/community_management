package io.github.cubelitblade.worker;

import io.github.cubelitblade.entity.Event;
import io.github.cubelitblade.event.handler.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EventDispatcher {
    private final Map<Event.EventType, EventHandler> eventHandlersMap;

    public EventDispatcher(List<EventHandler> handlers) {
        eventHandlersMap = handlers.stream().collect(
                Collectors.toMap(
                        EventHandler::getEventType,
                        handler -> handler
                )
        );
    }

    @Async("workerExecutor")
    public void dispatch(Event event) {

        Event.EventType type;

        try {
            type = event.getType();
        } catch (IllegalArgumentException e) {
            log.error("Unknown event type: {}", event.getType());
            return;
        }

        EventHandler handler = eventHandlersMap.get(type);

        if (handler == null) {
            log.error("No handler for event type {}", type);
            return;
        }

        log.info("Handling event {}", event);
        handler.handleEvent(event);
    }
}
