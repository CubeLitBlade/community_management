package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.account.common.AccountError;

public class LoginFailedException extends AccountException {

  public LoginFailedException(AccountError error) {
    super(error);
  }
}
