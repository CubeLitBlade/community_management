package io.github.cubelitblade.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ApiErrorCode {
  INVALID_REQUEST(Category.REQUEST, HttpStatus.BAD_REQUEST, "Invalid request"),
  INVALID_TOKEN(Category.REQUEST, HttpStatus.BAD_REQUEST, "Invalid token"),
  UNAUTHORIZED(Category.SECURITY, HttpStatus.UNAUTHORIZED, "Unauthorized"),

  INPUT_USERNAME_BLANK(Category.INPUT_VALIDATION, HttpStatus.BAD_REQUEST, "Invalid input"),
  INPUT_EMAIL_BLANK(Category.INPUT_VALIDATION, HttpStatus.BAD_REQUEST, "Invalid input"),
  INPUT_PHONE_BLANK(Category.INPUT_VALIDATION, HttpStatus.BAD_REQUEST, "Invalid input"),
  INPUT_PASSWORD_BLANK(Category.INPUT_VALIDATION, HttpStatus.BAD_REQUEST, "Invalid input"),
  INPUT_USERNAME_BAD_LENGTH(Category.INPUT_VALIDATION, HttpStatus.BAD_REQUEST, "Invalid input"),
  INPUT_PASSWORD_BAD_LENGTH(Category.INPUT_VALIDATION, HttpStatus.BAD_REQUEST, "Invalid input"),
  INPUT_PASSWORD_BAD_FORMAT(Category.INPUT_VALIDATION, HttpStatus.BAD_REQUEST, "Invalid input"),
  INPUT_EMAIL_BAD_FORMAT(Category.INPUT_VALIDATION, HttpStatus.BAD_REQUEST, "Invalid input"),
  INPUT_PHONE_BAD_FORMAT(Category.INPUT_VALIDATION, HttpStatus.BAD_REQUEST, "Invalid input"),
  INPUT_NO_CONTACT(Category.INPUT_VALIDATION, HttpStatus.BAD_REQUEST, "Invalid input"),

  CONFLICT_USERNAME_EXISTS(Category.CONFLICT, HttpStatus.CONFLICT, "Conflict"),
  CONFLICT_EMAIL_EXISTS(Category.CONFLICT, HttpStatus.CONFLICT, "Conflict"),
  CONFLICT_PHONE_EXISTS(Category.CONFLICT, HttpStatus.CONFLICT, "Conflict"),

  LOGIN_FAILED_SUSPENDED(Category.AUTHENTICATION, HttpStatus.UNAUTHORIZED, "Failed to login"),
  LOGIN_FAILED_ARCHIVED(Category.AUTHENTICATION, HttpStatus.UNAUTHORIZED, "Failed to login"),
  LOGIN_FAILED_INVALID_CREDENTIALS(
      Category.AUTHENTICATION, HttpStatus.UNAUTHORIZED, "Failed to login"),

  ACCOUNT_STATE_SUSPENDED(Category.ACCOUNT_STATE, HttpStatus.CONFLICT, "Invalid account state"),
  ACCOUNT_STATE_ARCHIVED(Category.ACCOUNT_STATE, HttpStatus.CONFLICT, "Invalid account state"),

  ACCOUNT_NOT_FOUND(Category.NOT_FOUND, HttpStatus.NOT_FOUND, "Account not found"),
  POST_NOT_FOUND(Category.NOT_FOUND, HttpStatus.NOT_FOUND, "Post not found"),
  POST_FORBIDDEN(Category.FORBIDDEN, HttpStatus.FORBIDDEN, "Not allowed to access");

  private final Category category;
  private final HttpStatus status;
  private final String title;

  ApiErrorCode(Category category, HttpStatus status, String title) {
    this.category = category;
    this.status = status;
    this.title = title;
  }

  public HttpStatusCode statusCode() {
    return status;
  }

  public enum Category {
    REQUEST,
    INPUT_VALIDATION,
    CONFLICT,
    AUTHENTICATION,
    ACCOUNT_STATE,
    FORBIDDEN,
    NOT_FOUND,
    SECURITY
  }
}
