package io.github.cubelitblade.event;

import io.github.cubelitblade.common.exception.ExceptionFactory;
import io.github.cubelitblade.event.payload.DemoEventPayload;
import io.github.cubelitblade.event.payload.EventPayloadMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final EventPayloadMapper eventPayloadMapper;
    private final ExceptionFactory exceptionFactory;

    public List<Event> claimWaitingEvents(int count) {
        return eventRepository.claimWaitingEvents(count);
    }

    public Event enqueueEvent(Event.EventType eventType, DemoEventPayload eventPayload) {
        Event event = Event.builder()
                .type(eventType)
                .status(Event.EventStatus.WAITING)
                .payload(eventPayloadMapper.toJsonNode(eventPayload))
                .build();

        eventRepository.saveOrThrow(event);
        return event;
    }

    public Event enqueueEvent(String eventType, DemoEventPayload eventPayload) {
        try {
            Event event = Event.builder()
                    .type(Event.EventType.from(eventType))
                    .status(Event.EventStatus.WAITING)
                    .payload(eventPayloadMapper.toJsonNode(eventPayload))
                    .build();
            eventRepository.saveOrThrow(event);
            return event;
        } catch (IllegalArgumentException e) {
            log.error("Invalid event type: {}", eventType);
            throw exceptionFactory.onUnknownEventType(eventType);
        }
    }

    public Event enqueueEvent(Event event) {
        eventRepository.saveOrThrow(event);
        return event;
    }

    public void updateEvent(Event event) {
        eventRepository.updateOrThrow(event);
    }

    public void updateEventStep(Event event) {
        eventRepository.updateEventStep(event);
    }

    public Event find(long id) {
        return eventRepository.findById(id);
    }
}
