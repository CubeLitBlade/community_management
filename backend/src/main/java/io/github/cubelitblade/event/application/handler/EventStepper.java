package io.github.cubelitblade.event.application.handler;

import io.github.cubelitblade.event.model.Event;

public interface EventStepper {
  boolean advanceEventToStep(Event event, String step);
}
