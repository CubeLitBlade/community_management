package io.github.cubelitblade.common.exception;

public class FatalEventException extends RuntimeException {
    public FatalEventException(String message) {
        super(message);
    }

    public FatalEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
