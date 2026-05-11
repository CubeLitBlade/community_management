package io.github.cubelitblade.event.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import io.github.cubelitblade.common.id.SnowflakeIdGenerator;
import io.github.cubelitblade.common.time.TimeProvider;
import io.github.cubelitblade.event.model.Event;
import io.github.cubelitblade.event.model.Status;
import io.github.cubelitblade.event.model.Type;
import io.github.cubelitblade.event.persistence.EventRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {
  private static final Instant NOW = Instant.parse("2026-04-20T08:00:00Z");

  @Mock private EventRepository eventRepository;
  @Mock private SnowflakeIdGenerator idGenerator;
  @Mock private TimeProvider timeProvider;

  private EventService eventService;

  @BeforeEach
  void setUp() {
    eventService = new EventService(eventRepository, idGenerator, timeProvider);
  }

  @Test
  @DisplayName("Query: should use current time for waiting events")
  void should_use_current_time_for_waiting_events() {
    List<Event> waiting = List.of(Event.create(1L, Type.DEMO_EVENT, payload(), NOW));
    given(timeProvider.now()).willReturn(NOW);
    given(eventRepository.findWaitingEvents(10, NOW)).willReturn(waiting);

    assertThat(eventService.getWaitingEvents(10)).isSameAs(waiting);
  }

  @Test
  @DisplayName("Query: should use current time for zombie events")
  void should_use_current_time_for_zombie_events() {
    Instant threshold = NOW.minusSeconds(600);
    List<Event> zombies = List.of(Event.create(2L, Type.DEMO_EVENT, payload(), NOW));
    given(timeProvider.now()).willReturn(NOW);
    given(eventRepository.findZombieEvents(5, threshold, NOW)).willReturn(zombies);

    assertThat(eventService.getZombieEvents(5, threshold)).isSameAs(zombies);
  }

  @Test
  @DisplayName("Create: should create and save known event with generated id")
  void should_create_and_save_known_event_with_generated_id() {
    JsonNode payload = payload();
    Instant nextRunAt = NOW.plusSeconds(30);
    given(timeProvider.now()).willReturn(NOW);
    given(idGenerator.nextId()).willReturn(40201L);

    Event event = eventService.createEvent("demo", payload, nextRunAt);

    ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
    then(eventRepository).should().save(eventCaptor.capture());
    assertThat(event).isSameAs(eventCaptor.getValue());
    assertThat(event)
        .extracting(
            Event::getId,
            Event::getType,
            Event::getPayload,
            Event::getStatus,
            Event::getCreatedAt,
            Event::getNextRunAt,
            Event::getUpdatedAt,
            Event::getVersion)
        .containsExactly(40201L, Type.DEMO_EVENT, payload, Status.WAITING, NOW, nextRunAt, NOW, 0);
  }

  @Test
  @DisplayName("Create: should reject unknown event type without saving")
  void should_reject_unknown_event_type_without_saving() {
    assertThatThrownBy(() -> eventService.createEvent("unknown", payload(), NOW))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unknown event type: unknown");

    then(eventRepository).should(never()).save(org.mockito.ArgumentMatchers.any(Event.class));
    then(idGenerator).should(never()).nextId();
  }

  @Test
  @DisplayName("Delegate: should delegate find and try update")
  void should_delegate_find_and_try_update() {
    Event event = Event.create(40202L, Type.DEMO_EVENT, payload(), NOW);
    given(eventRepository.find(40202L)).willReturn(event);
    given(eventRepository.tryUpdate(event)).willReturn(true);

    assertThat(eventService.find(40202L)).isSameAs(event);
    assertThat(eventService.tryUpdate(event)).isTrue();
  }

  private JsonNode payload() {
    return JsonNodeFactory.instance.objectNode().put("message", "hello");
  }
}
