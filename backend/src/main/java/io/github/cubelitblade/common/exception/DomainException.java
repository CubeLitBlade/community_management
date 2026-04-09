package io.github.cubelitblade.common.exception;

import lombok.Getter;

@Getter
public abstract class DomainException extends RuntimeException {

  public final ErrorCode errorCode;

  public DomainException(String message, ErrorCode errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  public DomainException(ErrorCode errorCode) {
    super(errorCode.getDefaultMessage());
    this.errorCode = errorCode;
  }
}
