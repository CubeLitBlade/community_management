package io.github.cubelitblade.user.domain.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public static final String ERROR_CODE = "USERNAME_ALREADY_EXISTS";

    public UsernameAlreadyExistsException(String username) {
        super("Username \"" + username + "\" is already in use.");
    }
}
