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
  public void dispatch(Event event) {

    Type type;
    EventHandler<?> handler;

    try {
      type = event.getType();
    } catch (IllegalArgumentException e) {
      lifecycleManager.abort(event, "Invalid event type: " + event.getType());
      log.error("Unknown event type: {}", event.getType());
      return;
    }

    handler = map.get(type);

    if (handler == null) {
      lifecycleManager.abort(event, "No handler for event type: " + type);
      log.error("No handler for event type {}", type);
      return;
    }

    if (!lifecycleManager.run(event)) {
      log.warn("[Event #{}]: Failed to claim (optimistic lock), aborting dispatch.", event.getId());
      return;
    }

    try {
      handler.handleEvent(event);
      lifecycleManager.complete(event);
    } catch (EventExecutionException e) {
      Throwable cause = e.getCause();

      if (EventRetryPolicy.isTransient(cause)) {
        lifecycleManager.reschedule(event, cause.getMessage());
      } else {
        lifecycleManager.giveUp(event, "Fatal error: " + cause.getMessage());
        log.error("[Event #{}] Failed with fatal error: {}", event.getId(), cause.getMessage());
      }
    } catch (IllegalStateException e) {
      lifecycleManager.abort(event, "Illegal state: " + e.getMessage());
      log.error("Illegal state: {}", e.getMessage());
    } catch (Exception e) {
      lifecycleManager.abort(event, "Unexpected error: " + e.getMessage());
      log.error("Unexpected error while processing event #{}: {}", event.getId(), e.getMessage());
    }
  }
}
