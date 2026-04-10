package io.github.cubelitblade.event.application.handler;

import io.github.cubelitblade.event.model.Event;

public interface EventScheduler {
  void reschedule(Event event, String reason);
}
