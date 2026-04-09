package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.account.common.AccountErrorCode;

@Deprecated(forRemoval = true)
public class AccountSuspendedException extends AccountException {

  public AccountSuspendedException() {
    super(AccountErrorCode.LOGIN_FAILED_SUSPENDED);
  }
}
