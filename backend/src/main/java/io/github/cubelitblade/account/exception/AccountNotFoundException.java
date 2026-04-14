package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.common.exception.ApiErrorCode;
import io.github.cubelitblade.common.exception.DomainException;

public class AccountNotFoundException extends DomainException {
  public AccountNotFoundException(String detail) {
    super(ApiErrorCode.ACCOUNT_NOT_FOUND, detail);
  }

  public static AccountNotFoundException notFound() {
    return new AccountNotFoundException("Account not found");
  }
}
