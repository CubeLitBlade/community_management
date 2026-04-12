package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.common.exception.DomainExceptionLegacy;
import io.github.cubelitblade.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class AccountExceptionLegacy extends DomainExceptionLegacy {
  public AccountExceptionLegacy(String message, ErrorCode errorCode) {
    super(message, errorCode);
  }

  public AccountExceptionLegacy(ErrorCode errorCode) {
    super(errorCode);
  }
}
