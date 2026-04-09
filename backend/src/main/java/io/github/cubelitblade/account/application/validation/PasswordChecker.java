package io.github.cubelitblade.account.application.validation;

import io.github.cubelitblade.account.common.AccountErrorCode;
import io.github.cubelitblade.account.model.Password;
import java.util.Optional;

public record PasswordChecker() implements FormatChecker, NotBlankChecker {
  @Override
  public AccountErrorCode blankErrorCode() {
    return AccountErrorCode.INPUT_PASSWORD_BLANK;
  }

  @Override
  public Optional<AccountErrorCode> checkFormat(String value) {
    return Password.check(value);
  }
}
