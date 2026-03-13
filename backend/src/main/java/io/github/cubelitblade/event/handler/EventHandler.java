package io.github.cubelitblade.event.handler;

import io.github.cubelitblade.entity.Event;
import io.github.cubelitblade.service.EventService;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
public abstract class EventHandler {
    protected final EventService eventService;
    protected final ObjectMapper objectMapper;

    public abstract Event.EventType getEventType();
    public void handleEvent(Event event) {
        event.setStatus(Event.EventStatus.RUNNING);
        eventService.updateById(event);
    }
}
