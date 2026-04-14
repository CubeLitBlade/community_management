package io.github.cubelitblade.common.exception;

public class ValidationException extends DomainException {
  public ValidationException(ApiErrorCode errorCode, String detail) {
    super(errorCode, detail);
    ensureCategory(errorCode, ApiErrorCode.Category.REQUEST);
  }
}
