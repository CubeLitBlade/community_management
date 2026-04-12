package io.github.cubelitblade.account.model;

import io.github.cubelitblade.account.common.AccountErrorCode;
import io.github.cubelitblade.account.exception.InputValidationExceptionLegacy;
import java.util.Optional;
import java.util.regex.Pattern;

public record Email(String value) {
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

  public Email(String value) {
    this.value =
        check(value)
            .map(
                error -> {
                  throw new InputValidationExceptionLegacy(error);
                })
            .orElseGet(value::toLowerCase)
            .toString();
    // Normalize to lowercase to ensure case-insensitive uniqueness in the database
  }

  public static Email of(String value) {
    return new Email(value);
  }

  public static Optional<AccountErrorCode> check(String value) {
    if (value == null || value.isBlank()) {
      return Optional.of(AccountErrorCode.INPUT_EMAIL_BLANK);
    } else if (!EMAIL_PATTERN.matcher(value).matches()) {
      return Optional.of(AccountErrorCode.INPUT_EMAIL_BAD_FORMAT);
    } else {
      return Optional.empty();
    }
  }
}
