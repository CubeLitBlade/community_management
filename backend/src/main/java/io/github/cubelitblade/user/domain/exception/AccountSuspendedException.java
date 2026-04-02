package io.github.cubelitblade.user.domain.exception;

import io.github.cubelitblade.common.exception.DomainException;

public class AccountSuspendedException extends DomainException {

    public static final String ERROR_CODE = "ACCOUNT_SUSPENDED";
    public static final String DEFAULT_MESSAGE = "Your account has been suspended. Please contact the administrators.";

    public AccountSuspendedException() {
        super(DEFAULT_MESSAGE);
    }
}
