package io.github.cubelitblade.account.application;

import io.github.cubelitblade.account.model.PasswordHash;

public interface PasswordHasher {
  PasswordHash fromRaw(String raw);

  boolean matches(String raw, PasswordHash encrypted);
}
