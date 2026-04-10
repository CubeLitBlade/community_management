package io.github.cubelitblade.event.application.handler;

import io.github.cubelitblade.event.model.Event;

public interface EventFinalizer {
  void complete(Event event);

  void abort(Event event, String reason);

  void giveUp(Event event, String reason);
}
