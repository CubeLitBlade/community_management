package io.github.cubelitblade.account.security;

import io.github.cubelitblade.account.model.Role;

public record JwtAuthenticatedUser(Long accountId, Role role) {}
