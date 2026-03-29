package io.github.cubelitblade.user.domain.exception;

public class AccountArchivedException extends RuntimeException {
    public AccountArchivedException() {
        super("The account has been archived.");
    }
}
