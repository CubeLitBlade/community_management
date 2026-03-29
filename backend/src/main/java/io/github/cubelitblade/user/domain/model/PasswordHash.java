package io.github.cubelitblade.user.domain.model;

import org.jspecify.annotations.NonNull;

public record PasswordHash(String value) {
    public PasswordHash {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
    }

    /**
     * Always returns a fixed mask to prevent the password hash from leaking in logs.
     *
     * @return a constant mask string, never the actual value.
     */
    @Override
    public @NonNull String toString() {
        return "************";
    }
}
