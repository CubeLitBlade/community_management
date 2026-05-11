package io.github.cubelitblade.event.infra.worker;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

import io.github.cubelitblade.event.application.handler.EventHandler;
import io.github.cubelitblade.event.application.handler.EventLifecycleManager;
import io.github.cubelitblade.event.exception.DownstreamTimeoutException;
import io.github.cubelitblade.event.exception.EventExecutionException;
import io.github.cubelitblade.event.model.Event;
import io.github.cubelitblade.event.model.Type;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.node.JsonNodeFactory;

@ExtendWith(MockitoExtension.class)
class EventDispatcherTest {
  private static final Instant NOW = Instant.parse("2026-04-20T08:00:00Z");

  @Mock private EventHandler<?> handler;
  @Mock private EventLifecycleManager lifecycleManager;

  private EventDispatcher dispatcher;

  @BeforeEach
  void setUp() {
    given(handler.getEventType()).willReturn(Type.DEMO_EVENT);
    dispatcher = new EventDispatcher(List.of(handler), lifecycleManager);
  }

  @Test
  @DisplayName("Dispatch: should run handler and complete event")
  void should_run_handler_and_complete_event() {
    Event event = demoEvent();
    given(lifecycleManager.run(event)).willReturn(true);

    dispatcher.dispatch(event);

    then(handler).should().handleEvent(event);
    then(lifecycleManager).should().complete(event);
  }

  @Test
  @DisplayName("Dispatch: should skip handler when event claim fails")
  void should_skip_handler_when_event_claim_fails() {
    Event event = demoEvent();
    given(lifecycleManager.run(event)).willReturn(false);

    dispatcher.dispatch(event);

    then(handler).should(never()).handleEvent(event);
    then(lifecycleManager).should(never()).complete(event);
  }

  @Test
  @DisplayName("Dispatch: should abort when no handler exists for event type")
  void should_abort_when_no_handler_exists() {
    Event event =
        Event.create(40301L, Type.NOTIFICATION_DELIVERY, JsonNodeFactory.instance.nullNode(), NOW);

    dispatcher.dispatch(event);

    then(lifecycleManager)
        .should()
        .abort(event, "No handler for event type: " + Type.NOTIFICATION_DELIVERY);
    then(lifecycleManager).should(never()).run(event);
  }

  @Test
  @DisplayName("Dispatch: should reschedule transient event execution failures")
  void should_reschedule_transient_event_execution_failures() {
    Event event = demoEvent();
    DownstreamTimeoutException cause = new DownstreamTimeoutException("temporary outage");
    given(lifecycleManager.run(event)).willReturn(true);
    willThrow(new EventExecutionException(event.getId(), cause)).given(handler).handleEvent(event);

    dispatcher.dispatch(event);

    then(lifecycleManager).should().reschedule(event, "temporary outage");
    then(lifecycleManager).should(never()).complete(event);
  }

  @Test
  @DisplayName("Dispatch: should give up on fatal event execution failures")
  void should_give_up_on_fatal_event_execution_failures() {
    Event event = demoEvent();
    RuntimeException cause = new RuntimeException("fatal");
    given(lifecycleManager.run(event)).willReturn(true);
    willThrow(new EventExecutionException(event.getId(), cause)).given(handler).handleEvent(event);

    dispatcher.dispatch(event);

    then(lifecycleManager).should().giveUp(event, "Fatal error: fatal");
    then(lifecycleManager).should(never()).complete(event);
  }

  @Test
  @DisplayName("Dispatch: should abort on illegal state from handler")
  void should_abort_on_illegal_state_from_handler() {
    Event event = demoEvent();
    given(lifecycleManager.run(event)).willReturn(true);
    willThrow(new IllegalStateException("bad state")).given(handler).handleEvent(event);

    dispatcher.dispatch(event);

    then(lifecycleManager).should().abort(event, "Illegal state: bad state");
    then(lifecycleManager).should(never()).complete(event);
  }

  @Test
  @DisplayName("Dispatch: should abort on unexpected handler exception")
  void should_abort_on_unexpected_handler_exception() {
    Event event = demoEvent();
    given(lifecycleManager.run(event)).willReturn(true);
    willThrow(new RuntimeException("boom")).given(handler).handleEvent(event);

    dispatcher.dispatch(event);

    then(lifecycleManager).should().abort(event, "Unexpected error: boom");
    then(lifecycleManager).should(never()).complete(event);
  }

  private Event demoEvent() {
    return Event.create(40300L, Type.DEMO_EVENT, JsonNodeFactory.instance.nullNode(), NOW);
  }
}
