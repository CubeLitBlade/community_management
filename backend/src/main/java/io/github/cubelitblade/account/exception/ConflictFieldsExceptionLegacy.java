package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.account.common.AccountErrorCode;

public class ConflictFieldsExceptionLegacy extends AccountExceptionLegacy {

  public ConflictFieldsExceptionLegacy(AccountErrorCode error) {
    super(error);
  }

  public ConflictFieldsExceptionLegacy(String username, AccountErrorCode error) {
    super("Username \"" + username + "\" is already in use. Consider another one", error);
  }
}
