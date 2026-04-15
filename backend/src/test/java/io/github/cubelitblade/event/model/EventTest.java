package io.github.cubelitblade.event.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.JsonNodeFactory;

class EventTest {
  private static final Instant NOW = Instant.parse("2026-03-26T15:30:00Z");

  @Test
  @DisplayName("Create: should initialize a waiting event with version 0 and retry count 0")
  void should_create_event_with_initial_state() {
    Event event = Event.create(1L, Type.DEMO_EVENT, JsonNodeFactory.instance.nullNode(), NOW);

    assertThat(event)
        .extracting(
            Event::getType,
            Event::getPayload,
            Event::getStatus,
            Event::getRetryCount,
            Event::getCreatedAt,
            Event::getNextRunAt,
            Event::getUpdatedAt,
            Event::getCurrentStep,
            Event::getVersion)
        .containsExactly(
            Type.DEMO_EVENT,
            JsonNodeFactory.instance.nullNode(),
            Status.WAITING,
            0,
            NOW,
            NOW,
            NOW,
            null,
            0);
  }

  @Test
  @DisplayName("Reconstitute: should restore the full snapshot")
  void should_reconstitute_snapshot() {
    Event.Snapshot snapshot =
        Event.Snapshot.builder()
            .id(7L)
            .type(Type.EVENT)
            .payload(JsonNodeFactory.instance.objectNode().put("payload", "value"))
            .status(Status.RUNNING)
            .retryCount(2)
            .errorMsg("boom")
            .createdAt(NOW.minusSeconds(60))
            .nextRunAt(NOW.plusSeconds(30))
            .updatedAt(NOW)
            .currentStep("step-2")
            .version(9)
            .build();

    Event event = Event.reconstitute(snapshot);

    assertThat(event)
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
            7L,
            Type.EVENT,
            JsonNodeFactory.instance.objectNode().put("payload", "value"),
            Status.RUNNING,
            2,
            "boom",
            NOW.minusSeconds(60),
            NOW.plusSeconds(30),
            NOW,
            "step-2",
            9);
  }

  @Test
  @DisplayName("Lifecycle: should run, advance, retry and tick correctly")
  void should_run_advance_retry_and_tick() {
    Event event = Event.create(1L, Type.DEMO_EVENT, JsonNodeFactory.instance.nullNode(), NOW);

    event.run(NOW.plusSeconds(1));
    event.advanceTo("step-1", NOW.plusSeconds(2));
    event.retry(NOW.plusSeconds(30), "temporary failure", NOW.plusSeconds(3));
    event.tick();

    assertThat(event)
        .extracting(
            Event::getStatus,
            Event::getRetryCount,
            Event::getErrorMsg,
            Event::getNextRunAt,
            Event::getUpdatedAt,
            Event::getCurrentStep,
            Event::getVersion)
        .containsExactly(
            Status.WAITING,
            1,
            "temporary failure",
            NOW.plusSeconds(30),
            NOW.plusSeconds(3),
            null,
            1);
  }
}
