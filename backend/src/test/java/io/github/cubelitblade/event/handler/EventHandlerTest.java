package io.github.cubelitblade.event.handler;

import io.github.cubelitblade.common.exception.FatalEventException;
import io.github.cubelitblade.common.exception.TransientEventException;
import io.github.cubelitblade.event.Event;
import io.github.cubelitblade.event.payload.EventPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class EventHandlerTest {
    @Mock
    private EventWorkflow workflow;

    @Mock
    private Event event;

    private TestEventHandler handler;

    @BeforeEach
    void setUp() {
        TestEventHandler realHandler = new TestEventHandler(workflow);
        handler = spy(realHandler);
    }

    @Test
    @DisplayName("Skip: should skip handling when status is not RUNNING")
    void should_skip_handling_when_status_is_not_running() {
        // Given
        given(event.getStatus()).willReturn(Event.EventStatus.SUCCEEDED);

        // When
        handler.handleEvent(event);

        // Then
        then(workflow).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("Complete: should complete workflow when process succeeds")
    void should_complete_when_process_succeeds() {
        // Given
        given(event.getStatus()).willReturn(Event.EventStatus.RUNNING);

        // When
        handler.handleEvent(event);

        //Then
        then(workflow).should().complete(event);
    }

    @Test
    @DisplayName("GiveUp: should call giveUp on FatalEventException")
    void should_giveUp_on_fatal_exception() {
        // Given
        String reason = "for testing purposes";
        given(event.getStatus()).willReturn(Event.EventStatus.RUNNING);
        willThrow(new FatalEventException(reason)).given(handler).process(event);

        // When
        handler.handleEvent(event);

        // Then
        then(workflow).should().giveUp(eq(event), eq(reason));
    }

    @Test
    @DisplayName("Reschedule: should reschedule on TransientEventException")
    void should_reschedule_on_transient_exception() {
        // Given
        String reason = "for testing purposes";
        given(event.getStatus()).willReturn(Event.EventStatus.RUNNING);
        willThrow(new TransientEventException(reason)).given(handler).process(event);

        // When
        handler.handleEvent(event);

        // Then
        then(workflow).should().reschedule(eq(event), eq(reason));
    }

    @Test
    @DisplayName("Abort: should abort on unknown exception")
    void should_abort_on_unknown_exception() {
        // Given
        String reason = "for testing purposes";
        given(event.getStatus()).willReturn(Event.EventStatus.RUNNING);
        willThrow(new RuntimeException(reason)).given(handler).process(event);

        // When
        handler.handleEvent(event);

        // Then
        then(workflow).should().abort(eq(event), eq(reason));
    }


    private record TestEventPayload() implements EventPayload {
    }

    private static class TestEventHandler extends EventHandler<TestEventPayload> {

        public TestEventHandler(EventWorkflow workflow) {
            super(workflow);
        }

        @Override
        public Class<TestEventPayload> getPayloadType() {
            return TestEventPayload.class;
        }

        @Override
        public Event.EventType getEventType() {
            return Event.EventType.EVENT;
        }

        @Override
        public void process(Event event) {}
    }
}