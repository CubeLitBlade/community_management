package io.github.cubelitblade.account.model;

import io.github.cubelitblade.account.common.AccountErrorCode;
import io.github.cubelitblade.account.exception.InputValidationExceptionLegacy;
import java.util.Objects;
import java.util.Optional;

public final class Username {
  private final String value;

  private Username(String value) {
    this.value = value;
  }

  public static Username of(String value) {
    return (Username)
        check(value)
            .map(
                error -> {
                  throw new InputValidationExceptionLegacy(error);
                })
            .orElseGet(() -> new Username(value));
  }

  /**
   * Generates an archived username derived from an existing one.
   *
   * <p>This method bypasses the standard 20-character limit, allowing up to 50 characters to
   * accommodate the system-generated archiving suffix.
   *
   * @throws IllegalArgumentException if the generated username exceeds the database column limit
   *     (50 characters)
   */
  public static Username archiveFrom(Username username, long id) {
    String value = username.value + "#archived_" + id;
    if (value.length() > 50) {
      throw new IllegalArgumentException("Username must be between 1 and 50 characters.");
    }
    return new Username(value);
  }

  public static Username reconstitute(String value) {
    return new Username(value);
  }

  public String value() {
    return this.value;
  }

  public static Optional<AccountErrorCode> check(String value) {
    if (value == null || value.isBlank()) {
      return Optional.of(AccountErrorCode.INPUT_USERNAME_BLANK);
    } else if (value.length() > 20) {
      return Optional.of(AccountErrorCode.INPUT_USERNAME_BAD_LENGTH);
    } else {
      return Optional.empty();
    }
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
