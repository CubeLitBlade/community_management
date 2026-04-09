package io.github.cubelitblade.account.application.validation;

import io.github.cubelitblade.account.common.AccountErrorCode;
import io.github.cubelitblade.account.model.Username;
import java.util.Optional;

public record UsernameChecker() implements FormatChecker, NotBlankChecker, UniqueChecker {
  @Override
  public Optional<AccountErrorCode> checkFormat(String value) {
    return Username.check(value);
  }

  @Override
  public AccountErrorCode blankErrorCode() {
    return AccountErrorCode.INPUT_USERNAME_BLANK;
  }

  @Override
  public AccountErrorCode conflictErrorCode() {
    return AccountErrorCode.CONFLICT_USERNAME_EXISTS;
  }
}
