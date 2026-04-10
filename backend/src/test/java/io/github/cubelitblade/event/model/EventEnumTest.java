package io.github.cubelitblade.event.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventEnumTest {
  @Test
  @DisplayName("Type.from should map known values and reject unknown values")
  void should_map_type_values() {
    assertThat(Type.from("event")).isEqualTo(Type.EVENT);
    assertThat(Type.from("demo")).isEqualTo(Type.DEMO_EVENT);

    assertThatThrownBy(() -> Type.from("unknown"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unknown event type: unknown");
  }

  @Test
  @DisplayName("Status.from should map known values and reject unknown values")
  void should_map_status_values() {
    assertThat(Status.from("waiting")).isEqualTo(Status.WAITING);
    assertThat(Status.from("running")).isEqualTo(Status.RUNNING);
    assertThat(Status.from("succeeded")).isEqualTo(Status.SUCCEEDED);

    assertThatThrownBy(() -> Status.from("invalid"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unknown event status: invalid");
  }
}
