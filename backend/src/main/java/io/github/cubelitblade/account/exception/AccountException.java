package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.account.common.AccountError;
import lombok.Getter;

@Getter
public class AccountException extends RuntimeException {
  public final AccountError error;

  public AccountException(String message, AccountError error) {
    super(message);
    this.error = error;
  }

  public AccountException(AccountError error) {
    super(error.getDefaultMessage());
    this.error = error;
  }

  public String getErrorCode() {
    return this.error.getCode();
  }
}
