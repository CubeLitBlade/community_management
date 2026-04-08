package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.account.common.AccountError;

@Deprecated(forRemoval = true)
public class AccountSuspendedException extends AccountException {

  public AccountSuspendedException() {
    super(AccountError.LOGIN_FAILED_SUSPENDED);
  }
}
