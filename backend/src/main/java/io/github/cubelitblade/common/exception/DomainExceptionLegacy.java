package io.github.cubelitblade.common.exception;

import lombok.Getter;

@Getter
public abstract class DomainExceptionLegacy extends RuntimeException {

  public final ErrorCode errorCode;

  public DomainExceptionLegacy(String message, ErrorCode errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  public DomainExceptionLegacy(ErrorCode errorCode) {
    super(errorCode.getDefaultMessage());
    this.errorCode = errorCode;
  }
}
