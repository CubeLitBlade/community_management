package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.account.common.AccountErrorCode;

public class ConflictFieldsException extends AccountException {

  public ConflictFieldsException(AccountErrorCode error) {
    super(error);
  }

  public ConflictFieldsException(String username, AccountErrorCode error) {
    super("Username \"" + username + "\" is already in use. Consider another one", error);
  }
}
