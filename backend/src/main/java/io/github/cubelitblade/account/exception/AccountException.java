package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.common.exception.DomainException;
import io.github.cubelitblade.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class AccountException extends DomainException {
  public AccountException(String message, ErrorCode errorCode) {
    super(message, errorCode);
  }

  public AccountException(ErrorCode errorCode) {
    super(errorCode);
  }
}
