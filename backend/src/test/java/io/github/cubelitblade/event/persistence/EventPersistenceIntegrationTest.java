package io.github.cubelitblade.event.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.cubelitblade.event.model.Event;
import io.github.cubelitblade.event.model.Status;
import io.github.cubelitblade.event.model.Type;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.node.JsonNodeFactory;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class EventPersistenceIntegrationTest {
  private static final Instant NOW = Instant.parse("2026-04-20T08:00:00Z");

  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private EventRepository eventRepository;

  @Test
  @DisplayName("EventRepository: should save and find event with full payload state")
  void should_save_and_find_event_with_full_payload_state() {
    Event event =
        Event.reconstitute(
            Event.Snapshot.builder()
                .id(40101L)
                .type(Type.DEMO_EVENT)
                .payload(JsonNodeFactory.instance.objectNode().put("message", "hello"))
                .status(Status.RUNNING)
                .retryCount(2)
                .errorMsg("retrying")
                .createdAt(NOW.minusSeconds(60))
                .nextRunAt(NOW.plusSeconds(30))
                .updatedAt(NOW)
                .currentStep("send-message")
                .version(3)
                .build());

    eventRepository.save(event);

    assertThat(eventRepository.find(40101L))
        .extracting(
            Event::getId,
            Event::getType,
            Event::getPayload,
            Event::getStatus,
            Event::getRetryCount,
            Event::getErrorMsg,
            Event::getCreatedAt,
            Event::getNextRunAt,
            Event::getUpdatedAt,
            Event::getCurrentStep,
            Event::getVersion)
        .containsExactly(
            40101L,
            Type.DEMO_EVENT,
            JsonNodeFactory.instance.objectNode().put("message", "hello"),
            Status.RUNNING,
            2,
            "retrying",
            NOW.minusSeconds(60),
            NOW.plusSeconds(30),
            NOW,
            "send-message",
            3);
  }

  @Test
  @DisplayName("EventRepository: should find due waiting events ordered by next run time")
  void should_find_due_waiting_events_ordered_by_next_run_time() {
    insertEvent(40111L, "demo", "waiting", NOW.minusSeconds(30), NOW.minusSeconds(10), NOW, 0);
    insertEvent(40112L, "demo", "waiting", NOW.minusSeconds(40), NOW.minusSeconds(20), NOW, 0);
    insertEvent(40113L, "demo", "waiting", NOW.minusSeconds(50), NOW.plusSeconds(10), NOW, 0);
    insertEvent(40114L, "demo", "running", NOW.minusSeconds(60), NOW.minusSeconds(30), NOW, 0);

    List<Event> events = eventRepository.findWaitingEvents(2, NOW);

    assertThat(events).extracting(Event::getId).containsExactly(40112L, 40111L);
    assertThat(events).extracting(Event::getStatus).containsOnly(Status.WAITING);
    assertThatThrownBy(() -> eventRepository.findWaitingEvents(0, NOW))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Count must be greater than 0");
  }

  @Test
  @DisplayName("EventRepository: should find zombie running events ordered by updated time")
  void should_find_zombie_running_events_ordered_by_updated_time() {
    Instant threshold = NOW.minusSeconds(600);
    insertEvent(
        40121L, "demo", "running", NOW.minusSeconds(900), null, threshold.minusSeconds(20), 0);
    insertEvent(
        40122L, "demo", "running", NOW.minusSeconds(900), null, threshold.minusSeconds(40), 0);
    insertEvent(
        40123L, "demo", "running", NOW.minusSeconds(900), null, threshold.plusSeconds(1), 0);
    insertEvent(
        40124L, "demo", "waiting", NOW.minusSeconds(900), threshold.minusSeconds(60), threshold, 0);

    List<Event> events = eventRepository.findZombieEvents(2, threshold, NOW);

    assertThat(events).extracting(Event::getId).containsExactly(40122L, 40121L);
    assertThat(events).extracting(Event::getStatus).containsOnly(Status.RUNNING);
    assertThatThrownBy(() -> eventRepository.findZombieEvents(0, threshold, NOW))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Count must be greater than 0");
  }

  @Test
  @DisplayName("EventRepository: should update with optimistic lock and tick in-memory version")
  void should_update_with_optimistic_lock_and_tick_in_memory_version() {
    insertEvent(40131L, "demo", "waiting", NOW, NOW, NOW, 0);
    Event firstCopy = eventRepository.find(40131L);
    Event staleCopy = eventRepository.find(40131L);

    firstCopy.run(NOW.plusSeconds(10));

    assertThat(eventRepository.tryUpdate(firstCopy)).isTrue();
    assertThat(firstCopy.getVersion()).isEqualTo(1);
    assertThat(eventRepository.find(40131L))
        .extracting(Event::getStatus, Event::getVersion, Event::getNextRunAt)
        .containsExactly(Status.RUNNING, 1, null);

    staleCopy.run(NOW.plusSeconds(20));

    assertThat(eventRepository.tryUpdate(staleCopy)).isFalse();
    assertThat(staleCopy.getVersion()).isZero();
    assertThatThrownBy(() -> eventRepository.updateOrThrow(staleCopy))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("State update failed");
  }

  private void insertEvent(
      Long id,
      String type,
      String status,
      Instant createdAt,
      Instant nextRunAt,
      Instant updatedAt,
      Integer version) {
    jdbcTemplate.update(
        """
        insert into events(
          id, type, payload, status, retry_count, error_msg, created_at,
          next_run_at, updated_at, current_step, version
        )
        values (?, ?, cast(? as jsonb), ?, 0, null, ?, ?, ?, null, ?)
        """,
        id,
        type,
        "{\"id\":" + id + "}",
        status,
        ts(createdAt),
        ts(nextRunAt),
        ts(updatedAt),
        version);
  }

  private Timestamp ts(Instant instant) {
    return instant == null ? null : Timestamp.from(instant);
  }
}
