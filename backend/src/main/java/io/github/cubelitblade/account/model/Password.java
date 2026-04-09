package io.github.cubelitblade.account.model;

import io.github.cubelitblade.account.common.AccountErrorCode;
import java.util.Optional;
import java.util.regex.Pattern;

public record Password(String value) {

  private static final Pattern PASSWORD_PATTERN =
      Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{6,20}$");

  public static Optional<AccountErrorCode> check(String value) {
    if (value == null || value.isBlank()) {
      return Optional.of(AccountErrorCode.INPUT_PASSWORD_BLANK);
    } else if (value.length() < 6 || value.length() > 20) {
      return Optional.of(AccountErrorCode.INPUT_PASSWORD_BAD_LENGTH);
    } else if (!PASSWORD_PATTERN.matcher(value).matches()) {
      return Optional.of(AccountErrorCode.INPUT_PASSWORD_BAD_FORMAT);
    } else {
      return Optional.empty();
    }
  }
}
