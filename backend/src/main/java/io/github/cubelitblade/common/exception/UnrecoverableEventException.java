package io.github.cubelitblade.common.exception;

public class UnrecoverableEventException extends RuntimeException {
    public UnrecoverableEventException(String message) {
        super(message);
    }
    public UnrecoverableEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
