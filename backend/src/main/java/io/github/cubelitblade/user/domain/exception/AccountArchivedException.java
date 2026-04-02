package io.github.cubelitblade.user.domain.exception;

import io.github.cubelitblade.common.exception.DomainException;

public class AccountArchivedException extends DomainException {

    public static final String ERROR_CODE = "ACCOUNT_ARCHIVED";
    public static final String DEFAULT_MESSAGE = "The account has been archived.";

    public AccountArchivedException() {
        super(DEFAULT_MESSAGE);
    }
}
