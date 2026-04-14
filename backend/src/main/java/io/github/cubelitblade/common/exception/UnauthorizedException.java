package io.github.cubelitblade.common.exception;

public class UnauthorizedException extends DomainException {
  public UnauthorizedException(ApiErrorCode errorCode, String detail) {
    super(errorCode, detail);
    ensureCategory(errorCode, ApiErrorCode.Category.SECURITY);
  }
}
