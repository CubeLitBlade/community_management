package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.account.common.AccountErrorCode;

@Deprecated(forRemoval = true)
public class AccountArchivedException extends AccountException {

  public AccountArchivedException() {
    super(AccountErrorCode.LOGIN_FAILED_ARCHIVED);
  }
}
