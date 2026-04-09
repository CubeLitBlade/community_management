package io.github.cubelitblade.account.common;

import io.github.cubelitblade.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public enum AccountErrorCode implements ErrorCode {
  INPUT_USERNAME_BLANK(ErrorCategory.INPUT_VALIDATION, "Username cannot be blank"),
  INPUT_EMAIL_BLANK(ErrorCategory.INPUT_VALIDATION, "Email cannot be blank"),
  INPUT_PHONE_BLANK(ErrorCategory.INPUT_VALIDATION, "Phone cannot be blank"),
  INPUT_PASSWORD_BLANK(ErrorCategory.INPUT_VALIDATION, "Password cannot be blank"),

  INPUT_USERNAME_BAD_LENGTH(
      ErrorCategory.INPUT_VALIDATION, "Username length must be between 1 and 20 characters"),
  INPUT_PASSWORD_BAD_LENGTH(
      ErrorCategory.INPUT_VALIDATION, "Password length must be between 6 and 20 characters"),

  INPUT_PASSWORD_BAD_FORMAT(
      ErrorCategory.INPUT_VALIDATION, "Password must contain letters and numbers"),
  INPUT_EMAIL_BAD_FORMAT(ErrorCategory.INPUT_VALIDATION, "Invalid email format"),
  INPUT_PHONE_BAD_FORMAT(ErrorCategory.INPUT_VALIDATION, "Invalid phone format"),

  INPUT_NO_CONTACT(
      ErrorCategory.INPUT_VALIDATION,
      "At least one contact information (email or phone) must be provided"),

  CONFLICT_USERNAME_EXISTS(ErrorCategory.CONFLICT, "Username already exists. Try another one"),
  CONFLICT_EMAIL_EXISTS(ErrorCategory.CONFLICT, "Email already exists. Try another one"),
  CONFLICT_PHONE_EXISTS(ErrorCategory.CONFLICT, "Phone already exists. Try another one"),

  LOGIN_FAILED_SUSPENDED(
      ErrorCategory.LOGIN_FAILURE,
      "Your account has been suspended. Please contact the administrators"),
  LOGIN_FAILED_ARCHIVED(
      ErrorCategory.LOGIN_FAILURE, "This account has been archived and cannot log in"),
  LOGIN_FAILED_INVALID_CREDENTIALS(
      ErrorCategory.LOGIN_FAILURE, "Username or password is incorrect"),

  ACCOUNT_STATE_SUSPENDED(ErrorCategory.ACCOUNT_STATE, "This account has been suspended"),
  ACCOUNT_STATE_ARCHIVED(ErrorCategory.ACCOUNT_STATE, "This account has been archived");

  private final ErrorCategory category;
  private final String defaultMessage;

  AccountErrorCode(ErrorCategory category, String defaultMessage) {
    this.category = category;
    this.defaultMessage = defaultMessage;
  }
}
