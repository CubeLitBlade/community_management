package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.common.exception.ApiErrorCode;
import io.github.cubelitblade.common.exception.DomainException;

public class AccountInputException extends DomainException {
  public AccountInputException(ApiErrorCode errorCode, String detail) {
    super(errorCode, detail);
    ensureCategory(errorCode, ApiErrorCode.Category.INPUT_VALIDATION);
  }

  public static AccountInputException from(ApiErrorCode errorCode) {
    return switch (errorCode) {
      case INPUT_USERNAME_BLANK -> new AccountInputException(errorCode, "Username cannot be blank");
      case INPUT_EMAIL_BLANK -> new AccountInputException(errorCode, "Email cannot be blank");
      case INPUT_PHONE_BLANK -> new AccountInputException(errorCode, "Phone cannot be blank");
      case INPUT_PASSWORD_BLANK -> new AccountInputException(errorCode, "Password cannot be blank");
      case INPUT_USERNAME_BAD_LENGTH ->
          new AccountInputException(
              errorCode, "Username length must be between 1 and 20 characters");
      case INPUT_PASSWORD_BAD_LENGTH ->
          new AccountInputException(
              errorCode, "Password length must be between 6 and 20 characters");
      case INPUT_PASSWORD_BAD_FORMAT ->
          new AccountInputException(errorCode, "Password must contain letters and numbers");
      case INPUT_EMAIL_BAD_FORMAT -> new AccountInputException(errorCode, "Invalid email format");
      case INPUT_PHONE_BAD_FORMAT -> new AccountInputException(errorCode, "Invalid phone format");
      case INPUT_NO_CONTACT ->
          new AccountInputException(
              errorCode, "At least one contact information (email or phone) must be provided");
      default ->
          throw new IllegalArgumentException(
              "Unsupported account input error code: " + errorCode.name());
    };
  }
}
