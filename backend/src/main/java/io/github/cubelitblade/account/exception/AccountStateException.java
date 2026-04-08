package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.account.common.AccountError;

public class AccountStateException extends AccountException {
  public AccountStateException(AccountError error) {
    super(error);
  }
}
