package io.github.cubelitblade.account.application.validation;

import io.github.cubelitblade.account.common.AccountError;
import io.github.cubelitblade.account.model.Password;
import java.util.Optional;

public record PasswordChecker() implements FormatChecker, NotBlankChecker {
  @Override
  public AccountError blankError() {
    return AccountError.INPUT_PASSWORD_BLANK;
  }

  @Override
  public Optional<AccountError> checkFormat(String value) {
    return Password.check(value);
  }
}
