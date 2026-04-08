package io.github.cubelitblade.account.common;

import lombok.Getter;

@Getter
public enum AccountError {
  INPUT_USERNAME_BLANK("Username cannot be blank"),
  INPUT_EMAIL_BLANK("Email cannot be blank"),
  INPUT_PHONE_BLANK("Phone cannot be blank"),
  INPUT_PASSWORD_BLANK("Password cannot be blank"),

  INPUT_USERNAME_BAD_LENGTH("Username length must be between 1 and 20 characters"),
  INPUT_PASSWORD_BAD_LENGTH("Password length must be between 6 and 20 characters"),

  INPUT_PASSWORD_BAD_FORMAT("Password must contain letters and numbers"),
  INPUT_EMAIL_BAD_FORMAT("Invalid email format"),
  INPUT_PHONE_BAD_FORMAT("Invalid phone format"),

  INPUT_WITHOUT_CONTACT("At least one contact information (email or phone) must be provided"),

  CONFLICT_USERNAME_EXISTS("Username already exists. Try another one"),
  CONFLICT_EMAIL_EXISTS("Email already exists. Try another one"),
  CONFLICT_PHONE_EXISTS("Phone already exists. Try another one"),

  LOGIN_FAILED_SUSPENDED("Your account has been suspended. Please contact the administrators"),
  LOGIN_FAILED_ARCHIVED("This account has been archived and cannot log in"),
  LOGIN_FAILED_INVALID_CREDENTIALS("Username or password is incorrect"),

  ACCOUNT_STATE_SUSPENDED("This account has been suspended"),
  ACCOUNT_STATE_ARCHIVED("This account has been archived");

  private final String defaultMessage;

  AccountError(String defaultMessage) {
    this.defaultMessage = defaultMessage;
  }

  public String getCode() {
    return this.name();
  }
}
