package io.github.cubelitblade.event.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import io.github.cubelitblade.common.time.TimeProvider;
import io.github.cubelitblade.configuration.RetryConfig;
import io.github.cubelitblade.event.application.EventRetryPolicy;
import io.github.cubelitblade.event.model.Event;
import io.github.cubelitblade.event.model.Status;
import io.github.cubelitblade.event.model.Type;
import io.github.cubelitblade.event.persistence.EventRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.node.JsonNodeFactory;

@ExtendWith(MockitoExtension.class)
class EventLifecycleManagerTest {
  @Mock private EventRepository eventRepository;

  private RetryConfig retryConfig;
  private TimeProvider timeProvider;
  private EventLifecycleManager lifecycleManager;

  @BeforeEach
  void setUp() {
    retryConfig = new RetryConfig(Duration.ofSeconds(1), Duration.ofSeconds(5), 3);

    timeProvider =
        new TimeProvider(Clock.fixed(Instant.parse("2026-03-26T15:30:00Z"), ZoneId.of("UTC+8")));

    EventRetryPolicy eventRetryPolicy = new EventRetryPolicy(retryConfig);

    lifecycleManager = new EventLifecycleManager(eventRepository, timeProvider, eventRetryPolicy);
    given(eventRepository.tryUpdate(org.mockito.ArgumentMatchers.any(Event.class)))
        .willReturn(true);
  }

  @Test
  @DisplayName("Complete: should mark event as SUCCEEDED")
  void should_mark_as_succeeded_when_complete_is_called() {
    // Given
    Event event = Event.create(1L, Type.EVENT, JsonNodeFactory.instance.nullNode(), timeProvider.now());

    // When
    lifecycleManager.complete(event);

    // Then
    assertThat(event)
        .hasFieldOrPropertyWithValue("nextRunAt", null)
        .hasFieldOrPropertyWithValue("status", Status.SUCCEEDED);
    then(eventRepository).should().tryUpdate(event);
  }

  @Test
  @DisplayName("Abort: should mark event as FAILED")
  void should_mark_as_failed_when_abort_is_called() {
    // Given
    Event event = Event.create(1L, Type.EVENT, JsonNodeFactory.instance.nullNode(), timeProvider.now());
    String reason = "for testing purposes";

    // When
    lifecycleManager.abort(event, reason);

    // Then
    assertThat(event)
        .hasFieldOrPropertyWithValue("nextRunAt", null)
        .hasFieldOrPropertyWithValue("errorMsg", reason)
        .hasFieldOrPropertyWithValue("status", Status.FAILED);
    then(eventRepository).should().tryUpdate(event);
  }

  @Test
  @DisplayName("GiveUp: should mark event as DEAD")
  void should_mark_as_dead_when_giveUp_is_called() {
    // Given
    Event event = Event.create(1L, Type.EVENT, JsonNodeFactory.instance.nullNode(), timeProvider.now());
    String reason = "for testing purposes";

    // When
    lifecycleManager.giveUp(event, reason);

    // Then
    assertThat(event)
        .hasFieldOrPropertyWithValue("nextRunAt", null)
        .hasFieldOrPropertyWithValue("errorMsg", reason)
        .hasFieldOrPropertyWithValue("status", Status.DEAD);
    then(eventRepository).should().tryUpdate(event);
  }

  @Test
  @DisplayName("Reschedule: should delay execution and increment retry count")
  void should_reschedule_with_delay_when_reschedule_is_called() {
    // Given
    Event event = Event.create(1L, Type.EVENT, JsonNodeFactory.instance.nullNode(), timeProvider.now());
    lifecycleManager.run(event);
    String reason = "for testing purposes";

    // When
    lifecycleManager.reschedule(event, reason);

    // Then
    assertThat(event)
        .hasFieldOrPropertyWithValue("nextRunAt", timeProvider.now().plus(retryConfig.baseDelay()))
        .hasFieldOrPropertyWithValue("errorMsg", reason)
        .hasFieldOrPropertyWithValue("retryCount", 1)
        .hasFieldOrPropertyWithValue("status", Status.WAITING);
    then(eventRepository).should(times(2)).tryUpdate(event);
  }

  @Test
  @DisplayName("Max Retries: should mark as DEAD when limit exceeded")
  void should_mark_as_dead_when_retry_limit_exceeded() {
    // Given
    Event event = Event.create(1L, Type.EVENT, JsonNodeFactory.instance.nullNode(), timeProvider.now());
    String reason = "for testing purposes";

    // When
    for (int i = 0; i <= retryConfig.maxRetries(); i++) {
      lifecycleManager.run(event);
      lifecycleManager.reschedule(event, reason);
    }

    // Then
    assertThat(event)
        .hasFieldOrPropertyWithValue("nextRunAt", null)
        .hasFieldOrPropertyWithValue("retryCount", retryConfig.maxRetries())
        .hasFieldOrPropertyWithValue("status", Status.DEAD);

    // Total calls: each attempt has one run and one reschedule/dead transition.
    then(eventRepository).should(times((retryConfig.maxRetries() + 1) * 2)).tryUpdate(event);
  }

  @Test
  void should_update_step_when_advanceEventToStep_is_called() {
    // Given
    Event event = Event.create(1L, Type.EVENT, JsonNodeFactory.instance.nullNode(), timeProvider.now());
    lifecycleManager.run(event);
    String step = "checkpoint";

    // When
    lifecycleManager.advanceEventToStep(event, step);

    // Then
    assertThat(event.getCurrentStep()).isEqualTo(step);
    then(eventRepository).should(times(2)).tryUpdate(event);
  }
}
