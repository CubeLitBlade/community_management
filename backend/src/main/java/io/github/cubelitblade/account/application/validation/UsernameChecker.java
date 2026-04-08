package io.github.cubelitblade.account.application.validation;

import io.github.cubelitblade.account.common.AccountError;
import io.github.cubelitblade.account.model.Username;
import java.util.Optional;

public record UsernameChecker() implements FormatChecker, NotBlankChecker, UniqueChecker {
  @Override
  public Optional<AccountError> checkFormat(String value) {
    return Username.check(value);
  }

  @Override
  public AccountError blankError() {
    return AccountError.INPUT_USERNAME_BLANK;
  }

  @Override
  public AccountError conflictError() {
    return AccountError.CONFLICT_USERNAME_EXISTS;
  }
}
