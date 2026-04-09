package io.github.cubelitblade.account.application.validation;

import io.github.cubelitblade.account.common.AccountErrorCode;
import io.github.cubelitblade.account.model.Phone;
import java.util.Optional;

public record PhoneChecker() implements FormatChecker, NotBlankChecker, UniqueChecker {
  @Override
  public Optional<AccountErrorCode> checkFormat(String value) {
    return Phone.check(value);
  }

  @Override
  public AccountErrorCode blankErrorCode() {
    return AccountErrorCode.INPUT_PHONE_BLANK;
  }

  @Override
  public AccountErrorCode conflictErrorCode() {
    return AccountErrorCode.CONFLICT_PHONE_EXISTS;
  }
}
