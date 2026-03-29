package io.github.cubelitblade.user.domain.exception;

public class AccountSuspendedException extends RuntimeException {
    public AccountSuspendedException() {
        super("Your account has been suspended. Please contact the administrators.");
    }
}
