package io.github.cubelitblade.common.exception;

import lombok.Getter;

@Getter
public class InvalidParameterException extends IllegalArgumentException {
    private String detail;

    public InvalidParameterException(String message) {
        super(message);
    }

    public InvalidParameterException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public InvalidParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidParameterException(String message, String detail, Throwable cause) {
        super(message, cause);
        this.detail = detail;
    }
}
