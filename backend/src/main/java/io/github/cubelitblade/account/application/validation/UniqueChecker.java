package io.github.cubelitblade.account.application.validation;

import io.github.cubelitblade.account.common.AccountError;

public interface UniqueChecker {
  AccountError conflictError();
}
