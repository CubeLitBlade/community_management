package io.github.cubelitblade.event.application;

import io.github.cubelitblade.common.exception.ExceptionFactory;
import io.github.cubelitblade.event.model.Event;
import io.github.cubelitblade.event.model.Type;
import io.github.cubelitblade.event.model.payload.DemoEventPayload;
import io.github.cubelitblade.event.model.payload.EventPayloadMapper;
import io.github.cubelitblade.event.persistence.EventRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

  public void resetZombieEvents(Instant threshold) {
    eventRepository.resetZombieEvents(threshold);
  }

  public Event enqueueEvent(Type eventType, DemoEventPayload eventPayload) {
    Event event =
        Event.create(
            eventType, eventPayloadMapper.toJsonNode(eventPayload), Clock.systemDefaultZone());
    eventRepository.saveOrThrow(event);
    return event;
  }

  public Event enqueueEvent(String eventType, DemoEventPayload eventPayload) {
    try {
      Event event =
          Event.create(
              eventType, eventPayloadMapper.toJsonNode(eventPayload), Clock.systemDefaultZone());
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

  public Event find(long id) {
    return eventRepository.findById(id);
  }
}
