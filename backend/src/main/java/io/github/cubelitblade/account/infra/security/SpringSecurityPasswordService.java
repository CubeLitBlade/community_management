package io.github.cubelitblade.account.infra.security;

import io.github.cubelitblade.account.domain.model.PasswordHash;
import io.github.cubelitblade.account.domain.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringSecurityPasswordService implements PasswordService {
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
