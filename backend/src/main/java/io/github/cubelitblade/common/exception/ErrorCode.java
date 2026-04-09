package io.github.cubelitblade.common.exception;

public interface ErrorCode {
  String getDefaultMessage();

  enum ErrorCategory {
    INPUT_VALIDATION,
    CONFLICT,
    LOGIN_FAILURE,
    ACCOUNT_STATE
  }
}
