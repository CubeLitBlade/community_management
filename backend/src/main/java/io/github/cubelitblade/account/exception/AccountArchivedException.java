package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.account.common.AccountError;

@Deprecated(forRemoval = true)
public class AccountArchivedException extends AccountException {

  public AccountArchivedException() {
    super(AccountError.LOGIN_FAILED_ARCHIVED);
  }
}
