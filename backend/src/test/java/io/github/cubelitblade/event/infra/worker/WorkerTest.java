package io.github.cubelitblade.event.infra.worker;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.github.cubelitblade.common.time.TimeProvider;
import io.github.cubelitblade.event.application.EventService;
import io.github.cubelitblade.event.application.handler.EventLifecycleManager;
import io.github.cubelitblade.event.model.Event;
import io.github.cubelitblade.event.model.Type;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.node.JsonNodeFactory;

@ExtendWith(MockitoExtension.class)
class WorkerTest {
  private static final Instant NOW = Instant.parse("2026-04-20T08:00:00Z");

  @Mock private EventService eventService;
  @Mock private EventDispatcher eventDispatcher;
  @Mock private EventLifecycleManager eventLifecycleManager;
  @Mock private TimeProvider timeProvider;

  private Worker worker;

  @BeforeEach
  void setUp() {
    worker = new Worker(eventService, eventDispatcher, eventLifecycleManager, timeProvider);
  }

  @Test
  @DisplayName("Run: should revive zombies before dispatching waiting events")
  void should_revive_zombies_before_dispatching_waiting_events() {
    Event zombie = Event.create(40401L, Type.DEMO_EVENT, JsonNodeFactory.instance.nullNode(), NOW);
    Event waiting = Event.create(40402L, Type.DEMO_EVENT, JsonNodeFactory.instance.nullNode(), NOW);
    Instant threshold = NOW.minusSeconds(600);
    given(timeProvider.now()).willReturn(NOW);
    given(eventService.getZombieEvents(10, threshold)).willReturn(List.of(zombie));
    given(eventService.getWaitingEvents(10)).willReturn(List.of(waiting));

    worker.run();

    InOrder inOrder = org.mockito.Mockito.inOrder(eventLifecycleManager, eventDispatcher);
    inOrder.verify(eventLifecycleManager).revive(zombie, NOW);
    inOrder.verify(eventDispatcher).dispatch(waiting);
  }

  @Test
  @DisplayName("Run: should not dispatch or revive when no events are available")
  void should_not_dispatch_or_revive_when_no_events_are_available() {
    given(timeProvider.now()).willReturn(NOW);
    given(eventService.getZombieEvents(10, NOW.minusSeconds(600))).willReturn(List.of());
    given(eventService.getWaitingEvents(10)).willReturn(List.of());

    worker.run();

    then(eventLifecycleManager).shouldHaveNoInteractions();
    then(eventDispatcher).shouldHaveNoInteractions();
  }
}
