package io.github.cubelitblade.account.application.validation;

import io.github.cubelitblade.common.exception.ApiErrorCode;

public interface NotBlankChecker {
  ApiErrorCode blankErrorCode();
}
