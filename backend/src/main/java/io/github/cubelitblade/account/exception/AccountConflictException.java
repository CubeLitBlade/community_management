package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.common.exception.ApiErrorCode;
import io.github.cubelitblade.common.exception.DomainException;

public class AccountConflictException extends DomainException {
  public AccountConflictException(ApiErrorCode errorCode, String detail) {
    super(errorCode, detail);
    ensureCategory(errorCode, ApiErrorCode.Category.CONFLICT);
  }

  public static AccountConflictException from(ApiErrorCode errorCode) {
    return switch (errorCode) {
      case CONFLICT_USERNAME_EXISTS ->
          new AccountConflictException(errorCode, "Username already exists. Try another one");
      case CONFLICT_EMAIL_EXISTS ->
          new AccountConflictException(errorCode, "Email already exists. Try another one");
      case CONFLICT_PHONE_EXISTS ->
          new AccountConflictException(errorCode, "Phone already exists. Try another one");
      default ->
          throw new IllegalArgumentException(
              "Unsupported account conflict error code: " + errorCode.name());
    };
  }
}
