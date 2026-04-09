package io.github.cubelitblade.account.application.validation;

import io.github.cubelitblade.account.common.AccountErrorCode;

public interface NotBlankChecker {
  AccountErrorCode blankErrorCode();
}
