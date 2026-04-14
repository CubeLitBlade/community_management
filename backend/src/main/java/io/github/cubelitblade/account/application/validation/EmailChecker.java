package io.github.cubelitblade.account.application.validation;

import io.github.cubelitblade.account.model.Email;
import io.github.cubelitblade.common.exception.ApiErrorCode;
import java.util.Optional;

public record EmailChecker() implements FormatChecker, NotBlankChecker, UniqueChecker {
  @Override
  public Optional<ApiErrorCode> checkFormat(String value) {
    return Email.check(value);
  }

  @Override
  public ApiErrorCode blankErrorCode() {
    return ApiErrorCode.INPUT_EMAIL_BLANK;
  }

  @Override
  public ApiErrorCode conflictErrorCode() {
    return ApiErrorCode.CONFLICT_EMAIL_EXISTS;
  }
}
