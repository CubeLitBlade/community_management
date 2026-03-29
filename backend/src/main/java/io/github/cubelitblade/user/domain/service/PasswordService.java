package io.github.cubelitblade.user.domain.service;

import io.github.cubelitblade.user.domain.model.PasswordHash;

public interface PasswordService {
    PasswordHash fromRaw(String raw);
    boolean matches(String raw, PasswordHash encrypted);
}
