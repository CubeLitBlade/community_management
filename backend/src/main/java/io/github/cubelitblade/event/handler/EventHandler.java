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

    public abstract void process(Event event);

    public void handleEvent(Event event) {
        process(event);
        eventService.updateById(event);
    }
}
