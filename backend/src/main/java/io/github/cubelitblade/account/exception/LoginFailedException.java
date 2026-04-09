package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.account.common.AccountErrorCode;

public class LoginFailedException extends AccountException {

  public LoginFailedException(AccountErrorCode error) {
    super(error);
  }
}
