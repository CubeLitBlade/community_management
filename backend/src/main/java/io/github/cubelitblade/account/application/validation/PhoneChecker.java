package io.github.cubelitblade.account.application.validation;

import io.github.cubelitblade.account.common.AccountError;
import io.github.cubelitblade.account.model.Phone;
import java.util.Optional;

public record PhoneChecker() implements FormatChecker, NotBlankChecker, UniqueChecker {
  @Override
  public Optional<AccountError> checkFormat(String value) {
    return Phone.check(value);
  }

  @Override
  public AccountError blankError() {
    return AccountError.INPUT_PHONE_BLANK;
  }

  @Override
  public AccountError conflictError() {
    return AccountError.CONFLICT_PHONE_EXISTS;
  }
}
