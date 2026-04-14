package io.github.cubelitblade.account.application.validation;

import io.github.cubelitblade.account.model.Username;
import io.github.cubelitblade.common.exception.ApiErrorCode;
import java.util.Optional;

public record UsernameChecker() implements FormatChecker, NotBlankChecker, UniqueChecker {
  @Override
  public Optional<ApiErrorCode> checkFormat(String value) {
    return Username.check(value);
  }

  @Override
  public ApiErrorCode blankErrorCode() {
    return ApiErrorCode.INPUT_USERNAME_BLANK;
  }

  @Override
  public ApiErrorCode conflictErrorCode() {
    return ApiErrorCode.CONFLICT_USERNAME_EXISTS;
  }
}
