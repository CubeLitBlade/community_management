package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.account.common.AccountErrorCode;

public class AccountStateExceptionLegacy extends AccountExceptionLegacy {
  public AccountStateExceptionLegacy(AccountErrorCode error) {
    super(error);
  }
}
