package io.github.cubelitblade.account.model;

import io.github.cubelitblade.account.common.AccountErrorCode;
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

  public static Optional<AccountErrorCode> check(String value) {
    if (value == null || value.isBlank()) {
      return Optional.of(AccountErrorCode.INPUT_PHONE_BLANK);
    } else if (!value.matches("^\\+?[0-9]{11}$")) {
      return Optional.of(AccountErrorCode.INPUT_PHONE_BAD_FORMAT);
    } else {
      return Optional.empty();
    }
  }
}
