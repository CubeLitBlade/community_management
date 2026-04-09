package io.github.cubelitblade.account.application.validation;

import io.github.cubelitblade.account.common.AccountErrorCode;
import java.util.Optional;

public interface FormatChecker {
  Optional<AccountErrorCode> checkFormat(String value);
}
