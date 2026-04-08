package io.github.cubelitblade.account.application;

import io.github.cubelitblade.account.model.PasswordHash;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringSecurityPasswordHasher implements PasswordHasher {
  private final PasswordEncoder encoder;

  @Override
  public PasswordHash fromRaw(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new IllegalArgumentException("Password cannot be null or blank");
    }
    return new PasswordHash(encoder.encode(raw));
  }

  public boolean matches(String raw, PasswordHash encrypted) {
    return encoder.matches(raw, encrypted.value());
  }
}
