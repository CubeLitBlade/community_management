package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.account.common.AccountErrorCode;

public class InputValidationException extends AccountException {

  public InputValidationException(AccountErrorCode error) {
    super(error);
  }
}
