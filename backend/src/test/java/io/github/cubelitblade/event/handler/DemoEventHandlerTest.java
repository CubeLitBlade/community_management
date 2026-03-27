package io.github.cubelitblade.event.handler;

import io.github.cubelitblade.common.exception.TransientEventException;
import io.github.cubelitblade.event.Event;
import io.github.cubelitblade.event.payload.DemoEventPayload;
import io.github.cubelitblade.event.payload.EventPayloadMapper;
import io.github.cubelitblade.event.sse.SseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class DemoEventHandlerTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-03-26T15:30:00Z"), ZoneId.of("UTC"));

    @Mock
    private EventWorkflow workflow;

    @Mock
    private SseService sseService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private EventPayloadMapper eventPayloadMapper;

    @InjectMocks
    private DemoEventHandler handler;

    private Event event;

    @BeforeEach
    void setUp() {
        event = Event.create(Event.EventType.DEMO_EVENT, JsonNodeFactory.instance.nullNode(), clock);
        event.run(Instant.now(clock));

        willAnswer(inv -> {
            Event e = inv.getArgument(0);
            String step = inv.getArgument(1);
            e.toStep(step, Instant.now(clock));
            return null;
        }).given(workflow).checkpoint(any(Event.class), any(String.class));

        willAnswer(inv -> {
            Consumer<?> action = inv.getArgument(0);
            action.accept(null);
            return null;
        }).given(transactionTemplate).executeWithoutResult(any());
    }

    @Test
    @DisplayName("Process: should execute all steps and complete")
    void should_execute_all_steps_and_complete() {
        // Given
        DemoEventPayload payload = new DemoEventPayload("hello", 0L, 0, true);
        given(eventPayloadMapper.fromJsonNode(any(JsonNode.class), eq(DemoEventPayload.class))).willReturn(payload);

        // When
        handler.process(event);

        // Then
        then(sseService).should().broadcast("hello");
        then(workflow).should().checkpoint(event, "init");
        then(workflow).should().checkpoint(event, "time-consuming-work-done");
        then(workflow).should().checkpoint(event, "tx-validated");
        then(workflow).should().complete(event);
    }

    @Test
    @DisplayName("Resume: should skip completed steps")
    void should_skip_completed_steps_when_resuming() {
        // Given
        DemoEventPayload payload = new DemoEventPayload("hello", 9999L, 0, true);
        given(eventPayloadMapper.fromJsonNode(any(JsonNode.class), eq(DemoEventPayload.class))).willReturn(payload);
        event.toStep("time-consuming-work-done", Instant.now(clock));

        // When
        handler.process(event);

        // Then
        then(sseService).shouldHaveNoInteractions();
        then(workflow).should(never()).checkpoint(event, "init");
        then(workflow).should().checkpoint(event, "tx-validated");
        then(workflow).should().complete(event);
    }

    @Test
    @DisplayName("Decision: should abort on payload failure")
    void should_abort_when_payload_indicates_failure() {
        // Given
        DemoEventPayload payload = new DemoEventPayload(null, 0L, 0, false);
        given(eventPayloadMapper.fromJsonNode(any(JsonNode.class), eq(DemoEventPayload.class))).willReturn(payload);

        // When
        handler.process(event);

        // Then
        then(workflow).should().abort(eq(event), any(String.class));
        then(workflow).should(never()).complete(event);
    }

    @Test
    @DisplayName("Retry: should throw TransientException if threshold not met")
    void should_throw_exception_when_retry_threshold_not_met() {
        // Given
        DemoEventPayload payload = new DemoEventPayload(null, 0L, 1, true);
        given(eventPayloadMapper.fromJsonNode(any(JsonNode.class), eq(DemoEventPayload.class))).willReturn(payload);

        // When & Then
        assertThatThrownBy(() -> handler.process(event))
                .isInstanceOf(TransientEventException.class)
                .hasMessageContaining("Retry threshold not reached");
    }
}
