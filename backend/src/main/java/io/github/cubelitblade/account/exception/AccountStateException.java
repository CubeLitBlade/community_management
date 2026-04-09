package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.account.common.AccountErrorCode;

public class AccountStateException extends AccountException {
  public AccountStateException(AccountErrorCode error) {
    super(error);
  }
}
