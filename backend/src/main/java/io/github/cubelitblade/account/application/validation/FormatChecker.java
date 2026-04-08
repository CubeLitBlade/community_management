package io.github.cubelitblade.account.application.validation;

import io.github.cubelitblade.account.common.AccountError;
import java.util.Optional;

public interface FormatChecker {
  Optional<AccountError> checkFormat(String value);
}
