package io.github.cubelitblade.common.exception;

public class TransientEventException extends RuntimeException {
    public TransientEventException(String message) {
        super(message);
    }

    public TransientEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
