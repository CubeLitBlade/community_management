package io.github.cubelitblade.account.application.validation;

import io.github.cubelitblade.account.model.Phone;
import io.github.cubelitblade.common.exception.ApiErrorCode;
import java.util.Optional;

public record PhoneChecker() implements FormatChecker, NotBlankChecker, UniqueChecker {
  @Override
  public Optional<ApiErrorCode> checkFormat(String value) {
    return Phone.check(value);
  }

  @Override
  public ApiErrorCode blankErrorCode() {
    return ApiErrorCode.INPUT_PHONE_BLANK;
  }

  @Override
  public ApiErrorCode conflictErrorCode() {
    return ApiErrorCode.CONFLICT_PHONE_EXISTS;
  }
}
