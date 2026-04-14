package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.common.exception.ApiErrorCode;
import io.github.cubelitblade.common.exception.DomainException;

public class LoginFailedException extends DomainException {
  public LoginFailedException(ApiErrorCode errorCode, String detail) {
    super(errorCode, detail);
    ensureCategory(errorCode, ApiErrorCode.Category.AUTHENTICATION);
  }

  public static LoginFailedException from(ApiErrorCode errorCode) {
    return switch (errorCode) {
      case LOGIN_FAILED_INVALID_CREDENTIALS ->
          new LoginFailedException(errorCode, "Username or password is incorrect");
      case LOGIN_FAILED_SUSPENDED ->
          new LoginFailedException(
              errorCode, "Your account has been suspended. Please contact the administrators");
      case LOGIN_FAILED_ARCHIVED ->
          new LoginFailedException(errorCode, "This account has been archived and cannot log in");
      default ->
          throw new IllegalArgumentException(
              "Unsupported login failure error code: " + errorCode.name());
    };
  }
}
