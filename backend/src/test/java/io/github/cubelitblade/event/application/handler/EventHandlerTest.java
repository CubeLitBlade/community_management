package io.github.cubelitblade.event.application.handler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

import io.github.cubelitblade.event.exception.EventExecutionException;
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
  @Mock private Event event;

  private TestEventHandler handler;

  @BeforeEach
  void setUp() {
    TestEventHandler realHandler = new TestEventHandler();
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
    then(handler).should(never()).process(event);
  }

  @Test
  @DisplayName("Process: should invoke process when event is RUNNING")
  void should_invoke_process_when_status_is_running() {
    // Given
    given(event.getStatus()).willReturn(Status.RUNNING);

    // When
    handler.handleEvent(event);

    // Then
    then(handler).should().process(event);
  }

  @Test
  @DisplayName("Wrap: should wrap process exceptions in EventExecutionException")
  void should_wrap_process_exception() {
    // Given
    String reason = "for testing purposes";
    given(event.getStatus()).willReturn(Status.RUNNING);
    given(event.getId()).willReturn(42L);
    willThrow(new Exception(reason)).given(handler).process(event);

    // When
    assertThatThrownBy(() -> handler.handleEvent(event))
        .isInstanceOf(EventExecutionException.class)
        .hasCauseInstanceOf(Exception.class)
        .hasMessageContaining(reason);
  }

  private record TestEventPayload() implements EventPayload {}

  private static class TestEventHandler extends EventHandler<TestEventPayload> {

    public TestEventHandler() {
      super(null);
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
