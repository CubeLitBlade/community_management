package io.github.cubelitblade.event.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cubelitblade.event.model.Event;
import io.github.cubelitblade.event.model.Status;
import io.github.cubelitblade.event.model.Type;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.JsonNodeFactory;

class EventPoTest {
  private static final Instant NOW = Instant.parse("2026-03-26T15:30:00Z");

  @Test
  @DisplayName("Map: should convert Event to EventPo and back without losing data")
  void should_round_trip_without_losing_data() {
    Event.Snapshot snapshot =
        Event.Snapshot.builder()
            .id(11L)
            .type(Type.DEMO_EVENT)
            .payload(JsonNodeFactory.instance.objectNode().put("message", "hello"))
            .status(Status.RUNNING)
            .retryCount(3)
            .errorMsg("transient")
            .createdAt(NOW.minusSeconds(120))
            .nextRunAt(NOW.plusSeconds(45))
            .updatedAt(NOW.plusSeconds(10))
            .currentStep("tx-validated")
            .version(8)
            .build();

    Event original = Event.reconstitute(snapshot);
    EventPo po = EventPo.of(original);
    Event restored = po.toEvent();

    assertThat(po)
        .extracting(
            EventPo::getId,
            EventPo::getType,
            EventPo::getPayload,
            EventPo::getStatus,
            EventPo::getRetryCount,
            EventPo::getErrorMsg,
            EventPo::getCreatedAt,
            EventPo::getNextRunAt,
            EventPo::getUpdatedAt,
            EventPo::getCurrentStep,
            EventPo::getVersion)
        .containsExactly(
            11L,
            "demo",
            JsonNodeFactory.instance.objectNode().put("message", "hello"),
            "running",
            3,
            "transient",
            NOW.minusSeconds(120),
            NOW.plusSeconds(45),
            NOW.plusSeconds(10),
            "tx-validated",
            8);

    assertThat(restored)
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
            11L,
            Type.DEMO_EVENT,
            JsonNodeFactory.instance.objectNode().put("message", "hello"),
            Status.RUNNING,
            3,
            "transient",
            NOW.minusSeconds(120),
            NOW.plusSeconds(45),
            NOW.plusSeconds(10),
            "tx-validated",
            8);
  }

  @Test
  @DisplayName("Map: should return null for null events")
  void should_return_null_for_null_event() {
    assertThat(EventPo.of(null)).isNull();
  }
}
