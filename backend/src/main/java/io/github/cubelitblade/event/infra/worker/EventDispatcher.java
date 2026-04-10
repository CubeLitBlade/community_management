package io.github.cubelitblade.event.infra.worker;

import io.github.cubelitblade.event.application.EventRetryPolicy;
import io.github.cubelitblade.event.application.handler.EventHandler;
import io.github.cubelitblade.event.application.handler.EventLifecycleManager;
import io.github.cubelitblade.event.exception.EventExecutionException;
import io.github.cubelitblade.event.model.Event;
import io.github.cubelitblade.event.model.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventDispatcher {

  private final Map<Type, ? extends EventHandler<?>> map;
  private final EventLifecycleManager lifecycleManager;

  public EventDispatcher(List<EventHandler<?>> handlers, EventLifecycleManager lifecycleManager) {
    map =
        handlers.stream().collect(Collectors.toMap(EventHandler::getEventType, handler -> handler));
    this.lifecycleManager = lifecycleManager;
  }

  @Async("workerExecutor")
  public void dispatch(Event runningEvent) {

    Type type;
    EventHandler<?> handler;

    try {
      type = runningEvent.getType();
    } catch (IllegalArgumentException e) {
      lifecycleManager.abort(runningEvent, "Invalid event type: " + runningEvent.getType());
      log.error("Unknown event type: {}", runningEvent.getType());
      return;
    }

    handler = map.get(type);

    if (handler == null) {
      lifecycleManager.abort(runningEvent, "No handler for event type: " + type);
      log.error("No handler for event type {}", type);
      return;
    }

    try {
      handler.handleEvent(runningEvent);
      lifecycleManager.complete(runningEvent);
    } catch (EventExecutionException e) {
      Throwable cause = e.getCause();

      if (EventRetryPolicy.isTransient(cause)) {
        lifecycleManager.reschedule(runningEvent, cause.getMessage());
      } else {
        lifecycleManager.giveUp(runningEvent, "Fatal error: " + cause.getMessage());
        log.error(
            "Event #{} failed with fatal error: {}", runningEvent.getId(), cause.getMessage());
      }
    } catch (IllegalStateException e) {
      lifecycleManager.abort(runningEvent, "Illegal state: " + e.getMessage());
      log.error("Illegal state: {}", e.getMessage());
    } catch (Exception e) {
      lifecycleManager.abort(runningEvent, "Unexpected error: " + e.getMessage());
      log.error(
          "Unexpected error while processing event #{}: {}", runningEvent.getId(), e.getMessage());
    }
  }
}
