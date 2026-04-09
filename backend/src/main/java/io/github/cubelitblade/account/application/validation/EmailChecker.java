package io.github.cubelitblade.account.application.validation;

import io.github.cubelitblade.account.common.AccountErrorCode;
import io.github.cubelitblade.account.model.Email;
import java.util.Optional;

public record EmailChecker() implements FormatChecker, NotBlankChecker, UniqueChecker {
  @Override
  public Optional<AccountErrorCode> checkFormat(String value) {
    return Email.check(value);
  }

  @Override
  public AccountErrorCode blankErrorCode() {
    return AccountErrorCode.INPUT_EMAIL_BLANK;
  }

  @Override
  public AccountErrorCode conflictErrorCode() {
    return AccountErrorCode.CONFLICT_EMAIL_EXISTS;
  }
}
