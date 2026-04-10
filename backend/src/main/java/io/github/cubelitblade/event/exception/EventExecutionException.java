package io.github.cubelitblade.event.exception;

import lombok.Getter;

@Getter
public class EventExecutionException extends RuntimeException {
  private final long eventId;

  public EventExecutionException(long eventId, Throwable cause) {
    super(cause);
    this.eventId = eventId;
  }
}
