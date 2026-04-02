package io.github.cubelitblade.account.domain.service;

import io.github.cubelitblade.account.domain.model.PasswordHash;

public interface PasswordService {
    PasswordHash fromRaw(String raw);
    boolean matches(String raw, PasswordHash encrypted);
}
