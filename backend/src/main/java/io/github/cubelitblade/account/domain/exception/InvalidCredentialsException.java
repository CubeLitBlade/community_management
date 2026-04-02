package io.github.cubelitblade.account.domain.exception;

import io.github.cubelitblade.common.exception.DomainException;

public class InvalidCredentialsException extends DomainException {

    public static String ERROR_CODE = "INVALID_CREDENTIALS";
    public static String DEFAULT_MESSAGE = "Username or password is incorrect.";

    public InvalidCredentialsException() {
        super(DEFAULT_MESSAGE);
    }
}
