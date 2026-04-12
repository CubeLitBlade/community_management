package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.account.common.AccountErrorCode;

@Deprecated(forRemoval = true)
public class AccountSuspendedExceptionLegacy extends AccountExceptionLegacy {

  public AccountSuspendedExceptionLegacy() {
    super(AccountErrorCode.LOGIN_FAILED_SUSPENDED);
  }
}
