package io.github.cubelitblade.account.model;

import io.github.cubelitblade.account.common.AccountError;
import java.util.Optional;

public record Phone(String value) {
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
