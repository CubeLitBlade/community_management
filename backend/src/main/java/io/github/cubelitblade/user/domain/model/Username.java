package io.github.cubelitblade.user.domain.model;

import java.util.Objects;

public final class Username {
    private final String value;

    private Username(String value) {
        this.value = value;
    }

    public static Username of(String value) {
        if (value == null || value.isBlank() || value.length() > 20) {
            throw new IllegalArgumentException("Username must be between 1 and 20 characters.");
        }
        return new Username(value);
    }

    /**
     * Generates an archived username derived from an existing one.
     * <p>
     * This method bypasses the standard 20-character limit, allowing up to 50 characters to accommodate
     * the system-generated archiving suffix.
     * </p>
     *
     * @throws IllegalArgumentException if the generated username exceeds the database column limit (50 characters)
     */
    public static Username archiveFrom(Username username, long id) {
        String value = username.value + "#archived_" + id;
        if (value.length() > 50) {
            throw new IllegalArgumentException("Username must be between 1 and 50 characters.");
        }
        return new Username(value);
    }

    public String value() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Username username = (Username) o;
        return Objects.equals(value, username.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
