package io.github.cubelitblade.event.handler;

import io.github.cubelitblade.configuration.RetryConfig;
import io.github.cubelitblade.configuration.TimeConfig;
import io.github.cubelitblade.event.Event;
import io.github.cubelitblade.event.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.node.JsonNodeFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class EventWorkflowTest {
    @Mock
    private EventService eventService;

    private RetryConfig retryConfig;
    private TimeConfig timeConfig;
    private EventWorkflow workflow;

    @BeforeEach
    void setUp() {
        retryConfig = new RetryConfig();
        retryConfig.setBaseDelay(Duration.ofSeconds(1));
        retryConfig.setMaxDelay(Duration.ofSeconds(5));
        retryConfig.setMaxRetries(3);

        timeConfig = new TimeConfig();
        timeConfig.setClock(Clock.fixed(Instant.parse("2026-03-26T15:30:00Z"), ZoneId.of("UTC+8")));

        workflow = new EventWorkflow(eventService, retryConfig, timeConfig);
    }

    @Test
    @DisplayName("Abort: should mark event as FAILED")
    void should_mark_as_succeeded_when_complete_is_called() {
        // Given
        Event event = Event.create(Event.EventType.EVENT, JsonNodeFactory.instance.nullNode(), timeConfig.getClock());

        // When
        workflow.complete(event);

        // Then
        assertThat(event)
                .hasFieldOrPropertyWithValue("nextRunAt", null)
                .hasFieldOrPropertyWithValue("status", Event.EventStatus.SUCCEEDED);
        then(eventService).should().updateEvent(event);
    }

    @Test
    @DisplayName("Abort: should mark event as FAILED")
    void should_mark_as_failed_when_abort_is_called() {
        // Given
        Event event = Event.create(Event.EventType.EVENT, JsonNodeFactory.instance.nullNode(), timeConfig.getClock());
        String reason = "for testing purposes";

        // When
        workflow.abort(event, reason);

        // Then
        assertThat(event)
                .hasFieldOrPropertyWithValue("nextRunAt", null)
                .hasFieldOrPropertyWithValue("errorMsg", reason)
                .hasFieldOrPropertyWithValue("status", Event.EventStatus.FAILED);
        then(eventService).should().updateEvent(event);
    }

    @Test
    @DisplayName("GiveUp: should mark event as DEAD")
    void should_mark_as_dead_when_giveUp_is_called() {
        // Given
        Event event = Event.create(Event.EventType.EVENT, JsonNodeFactory.instance.nullNode(), timeConfig.getClock());
        String reason = "for testing purposes";

        // When
        workflow.giveUp(event, reason);

        // Then
        assertThat(event)
                .hasFieldOrPropertyWithValue("nextRunAt", null)
                .hasFieldOrPropertyWithValue("errorMsg", reason)
                .hasFieldOrPropertyWithValue("status", Event.EventStatus.DEAD);
        then(eventService).should().updateEvent(event);
    }

    @Test
    @DisplayName("Reschedule: should delay execution and increment retry count")
    void should_reschedule_with_delay_when_reschedule_is_called() {
        // Given
        Event event = Event.create(Event.EventType.EVENT, JsonNodeFactory.instance.nullNode(), timeConfig.getClock());
        Instant before = event.getNextRunAt();
        String reason = "for testing purposes";

        // When
        workflow.reschedule(event, reason);

        // Then
        assertThat(event)
                .hasFieldOrPropertyWithValue("nextRunAt", before.plus(retryConfig.getBaseDelay()))
                .hasFieldOrPropertyWithValue("errorMsg", reason)
                .hasFieldOrPropertyWithValue("retryCount", 1)
                .hasFieldOrPropertyWithValue("status", Event.EventStatus.WAITING);
        then(eventService).should().updateEvent(event);
    }

    @Test
    @DisplayName("Max Retries: should mark as DEAD when limit exceeded")
    void should_mark_as_dead_when_retry_limit_exceeded() {
        // Given
        Event event = Event.create(Event.EventType.EVENT, JsonNodeFactory.instance.nullNode(), timeConfig.getClock());
        String reason = "for testing purposes";

        // When
        for (int i = 0; i <= retryConfig.getMaxRetries(); i++) {
            workflow.reschedule(event, reason);
        }

        // Then
        assertThat(event)
                .hasFieldOrPropertyWithValue("nextRunAt", null)
                .hasFieldOrPropertyWithValue("retryCount", retryConfig.getMaxRetries())
                .hasFieldOrPropertyWithValue("status", Event.EventStatus.DEAD);

        // Total calls: maxRetries (reschedules) + 1 (final dead mark)
        then(eventService).should(times(retryConfig.getMaxRetries() + 1)).updateEvent(event);
    }

    @Test
    void should_update_step_when_checkpoint_is_called() {
        // Given
        Event event = Event.create(Event.EventType.EVENT, JsonNodeFactory.instance.nullNode(), timeConfig.getClock());
        String step = "checkpoint";

        // When
        workflow.checkpoint(event, step);

        // Then
        assertThat(event.getCurrentStep()).isEqualTo(step);
        then(eventService).should().updateEventStep(event);
    }
}
