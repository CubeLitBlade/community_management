package io.github.cubelitblade.account.infra.security.jwt;

import io.github.cubelitblade.account.domain.model.Role;

public record JwtAuthenticatedUser(Long accountId, Role role) {
}