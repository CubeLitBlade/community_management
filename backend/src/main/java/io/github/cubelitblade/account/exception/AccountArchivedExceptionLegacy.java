package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.account.common.AccountErrorCode;

@Deprecated(forRemoval = true)
public class AccountArchivedExceptionLegacy extends AccountExceptionLegacy {

  public AccountArchivedExceptionLegacy() {
    super(AccountErrorCode.LOGIN_FAILED_ARCHIVED);
  }
}
