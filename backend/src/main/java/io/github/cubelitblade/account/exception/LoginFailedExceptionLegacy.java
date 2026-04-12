package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.account.common.AccountErrorCode;

public class LoginFailedExceptionLegacy extends AccountExceptionLegacy {

  public LoginFailedExceptionLegacy(AccountErrorCode error) {
    super(error);
  }
}
