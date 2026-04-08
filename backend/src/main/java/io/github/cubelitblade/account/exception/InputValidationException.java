package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.account.common.AccountError;

public class InputValidationException extends AccountException {

  public InputValidationException(AccountError error) {
    super(error);
  }
}
