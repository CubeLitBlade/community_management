package io.github.cubelitblade.account.application.validation;

import io.github.cubelitblade.common.exception.ApiErrorCode;
import java.util.Optional;

public interface FormatChecker {
  Optional<ApiErrorCode> checkFormat(String value);
}
