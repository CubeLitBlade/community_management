package io.github.cubelitblade.account.domain.exception;

import io.github.cubelitblade.common.exception.DomainException;

public class UsernameAlreadyExistsException extends DomainException {

    public static final String ERROR_CODE = "USERNAME_ALREADY_EXISTS";

    public UsernameAlreadyExistsException(String username) {
        super("Username \"" + username + "\" is already in use.");
    }
}
