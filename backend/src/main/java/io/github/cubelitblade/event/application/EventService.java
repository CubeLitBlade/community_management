package io.github.cubelitblade.event.application;

import io.github.cubelitblade.common.id.SnowflakeIdGenerator;
import io.github.cubelitblade.common.time.TimeProvider;
import io.github.cubelitblade.event.model.Event;
import io.github.cubelitblade.event.model.Type;
import io.github.cubelitblade.event.model.payload.DemoEventPayload;
import io.github.cubelitblade.event.model.payload.EventPayload;
import io.github.cubelitblade.event.persistence.EventRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
  private final EventRepository eventRepository;
  private final SnowflakeIdGenerator idGenerator;
  private final TimeProvider timeProvider;

  public List<Event> getWaitingEvents(int count) {
    return eventRepository.findWaitingEvents(count, timeProvider.now());
  }

  public List<Event> getZombieEvents(int count, Instant threshold) {
    return eventRepository.findZombieEvents(count, threshold, timeProvider.now());
  }

  public Event createEvent(String type, JsonNode payloadJson) {
    if (getPayloadClass(type) == null) {
      throw new IllegalArgumentException("Unknown event type: " + type);
    }

    Event event =
        Event.create(idGenerator.nextId(), Type.from(type), payloadJson, timeProvider.now());

    eventRepository.save(event);
    return event;
  }

  public boolean tryUpdate(Event event) {
    return eventRepository.tryUpdate(event);
  }

  public Event find(long id) {
    return eventRepository.find(id);
  }

  private Class<? extends EventPayload> getPayloadClass(String type) {
    return switch (type) {
      case "demo" -> DemoEventPayload.class;
      default -> null;
    };
  }
}
