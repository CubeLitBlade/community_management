package io.github.cubelitblade.account.model;

import io.github.cubelitblade.account.exception.AccountInputException;
import io.github.cubelitblade.common.exception.ApiErrorCode;
import java.util.Optional;

public record Phone(String value) {
  public Phone {
    check(value)
        .ifPresent(
            error -> {
              throw AccountInputException.from(error);
            });
  }

  public static Phone of(String value) {
    return new Phone(value);
  }

  public static Optional<ApiErrorCode> check(String value) {
    if (value == null || value.isBlank()) {
      return Optional.of(ApiErrorCode.INPUT_PHONE_BLANK);
    } else if (!value.matches("^\\+?[0-9]{11}$")) {
      return Optional.of(ApiErrorCode.INPUT_PHONE_BAD_FORMAT);
    } else {
      return Optional.empty();
    }
  }
}
