package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.common.exception.ApiErrorCode;
import io.github.cubelitblade.common.exception.DomainException;

public class AccountStateException extends DomainException {
  public AccountStateException(ApiErrorCode errorCode, String detail) {
    super(errorCode, detail);
    ensureCategory(errorCode, ApiErrorCode.Category.ACCOUNT_STATE);
  }

  public static AccountStateException from(ApiErrorCode errorCode) {
    return switch (errorCode) {
      case ACCOUNT_STATE_SUSPENDED ->
          new AccountStateException(errorCode, "This account has been suspended");
      case ACCOUNT_STATE_ARCHIVED ->
          new AccountStateException(errorCode, "This account has been archived");
      default ->
          throw new IllegalArgumentException(
              "Unsupported account state error code: " + errorCode.name());
    };
  }
}
