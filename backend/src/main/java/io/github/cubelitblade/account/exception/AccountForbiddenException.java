package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.common.exception.ApiErrorCode;
import io.github.cubelitblade.common.exception.DomainException;

public class AccountForbiddenException extends DomainException {
  private AccountForbiddenException(String detail) {
    super(ApiErrorCode.FORBIDDEN, detail);
  }

  public static AccountForbiddenException managementRequired() {
    return new AccountForbiddenException("You are not allowed to manage accounts");
  }

  public static AccountForbiddenException operationNotAllowed() {
    return new AccountForbiddenException("You are not allowed to perform this account action");
  }
}
