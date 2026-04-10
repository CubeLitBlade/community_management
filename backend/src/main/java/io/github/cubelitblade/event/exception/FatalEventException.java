package io.github.cubelitblade.event.exception;

@Deprecated
public class FatalEventException extends RuntimeException {
  public FatalEventException(String message) {
    super(message);
  }

  public FatalEventException(String message, Throwable cause) {
    super(message, cause);
  }
}
