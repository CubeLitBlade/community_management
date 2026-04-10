package io.github.cubelitblade.event.exception;

public class DownstreamTimeoutException extends TransientEventException {
  public DownstreamTimeoutException(String message) {
    super(message);
  }
}
