package io.github.cubelitblade.account.application.validation;

import io.github.cubelitblade.account.common.AccountError;
import io.github.cubelitblade.account.model.Email;
import java.util.Optional;

public record EmailChecker() implements FormatChecker, NotBlankChecker, UniqueChecker {
  @Override
  public Optional<AccountError> checkFormat(String value) {
    return Email.check(value);
  }

  @Override
  public AccountError blankError() {
    return AccountError.INPUT_EMAIL_BLANK;
  }

  @Override
  public AccountError conflictError() {
    return AccountError.CONFLICT_EMAIL_EXISTS;
  }
}
