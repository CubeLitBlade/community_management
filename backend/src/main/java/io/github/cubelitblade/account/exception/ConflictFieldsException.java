package io.github.cubelitblade.account.exception;

import io.github.cubelitblade.account.common.AccountError;

public class ConflictFieldsException extends AccountException {

  public ConflictFieldsException(AccountError error) {
    super(error);
  }

  public ConflictFieldsException(String username, AccountError error) {
    super("Username \"" + username + "\" is already in use. Consider another one", error);
  }
}
