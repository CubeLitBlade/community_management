package io.github.cubelitblade.account.application.validation;

import io.github.cubelitblade.account.model.Password;
import io.github.cubelitblade.common.exception.ApiErrorCode;
import java.util.Optional;

public record PasswordChecker() implements FormatChecker, NotBlankChecker {
  @Override
  public ApiErrorCode blankErrorCode() {
    return ApiErrorCode.INPUT_PASSWORD_BLANK;
  }

  @Override
  public Optional<ApiErrorCode> checkFormat(String value) {
    return Password.check(value);
  }
}
