package io.github.cubelitblade.event.application.handler;

import static org.mockito.BDDMockito.*;

import io.github.cubelitblade.event.exception.DownstreamTimeoutException;
import io.github.cubelitblade.event.exception.RejectedEventException;
import io.github.cubelitblade.event.model.Event;
import io.github.cubelitblade.event.model.Status;
import io.github.cubelitblade.event.model.Type;
import io.github.cubelitblade.event.model.payload.EventPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventHandlerTest {
  @Mock private EventLifecycleManager workflow;

  @Mock private Event event;

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
    given(event.getStatus()).willReturn(Status.SUCCEEDED);

    // When
    handler.handleEvent(event);

    // Then
    then(workflow).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("Complete: should complete workflow when process succeeds")
  void should_complete_when_process_succeeds() {
    // Given
    given(event.getStatus()).willReturn(Status.RUNNING);

    // When
    handler.handleEvent(event);

    // Then
    then(workflow).should().complete(event);
  }

  @Test
  @DisplayName("GiveUp: should call giveUp on FatalEventException")
  void should_giveUp_on_fatal_exception() {
    // Given
    String reason = "for testing purposes";
    given(event.getStatus()).willReturn(Status.RUNNING);
    willThrow(new RejectedEventException(reason)).given(handler).process(event);

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
    given(event.getStatus()).willReturn(Status.RUNNING);
    willThrow(new DownstreamTimeoutException(reason)).given(handler).process(event);

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
    given(event.getStatus()).willReturn(Status.RUNNING);
    willThrow(new RuntimeException(reason)).given(handler).process(event);

    // When
    handler.handleEvent(event);

    // Then
    then(workflow).should().abort(eq(event), eq(reason));
  }

  private record TestEventPayload() implements EventPayload {}

  private static class TestEventHandler extends EventHandler<TestEventPayload> {

    public TestEventHandler(EventLifecycleManager workflow) {
      super(workflow);
    }

    @Override
    public Class<TestEventPayload> getPayloadType() {
      return TestEventPayload.class;
    }

    @Override
    public Type getEventType() {
      return Type.EVENT;
    }

    @Override
    public void process(Event event) {}
  }
}
