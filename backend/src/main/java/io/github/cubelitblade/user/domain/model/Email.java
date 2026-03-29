package io.github.cubelitblade.user.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

public record Email(String value) {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    public Email(String value) {
        Objects.requireNonNull(value, "Email address cannot be null");
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email address format: " + value);
        }

        // Normalize to lowercase to ensure case-insensitive uniqueness in the database
        this.value = value.toLowerCase();
    }
}
