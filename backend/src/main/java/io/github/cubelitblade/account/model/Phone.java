package io.github.cubelitblade.account.model;

import io.github.cubelitblade.account.common.AccountError;
import io.github.cubelitblade.account.exception.InputValidationException;
import java.util.Optional;

public record Phone(String value) {
  public Phone {
    check(value)
        .ifPresent(
            error -> {
              throw new InputValidationException(error);
            });
  }

  public static Phone of(String value) {
    return new Phone(value);
  }

  public static Optional<AccountError> check(String value) {
    if (value == null || value.isBlank()) {
      return Optional.of(AccountError.INPUT_PHONE_BLANK);
    } else if (!value.matches("^\\+?[0-9]{11}$")) {
      return Optional.of(AccountError.INPUT_PHONE_BAD_FORMAT);
    } else {
      return Optional.empty();
    }
  }
}
