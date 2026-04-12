package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.account.common.AccountErrorCode;

public class InputValidationExceptionLegacy extends AccountExceptionLegacy {

  public InputValidationExceptionLegacy(AccountErrorCode error) {
    super(error);
  }
}
