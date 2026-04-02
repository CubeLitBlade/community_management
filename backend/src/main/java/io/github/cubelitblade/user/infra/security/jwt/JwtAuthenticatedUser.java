package io.github.cubelitblade.user.infra.security.jwt;

import io.github.cubelitblade.user.domain.model.Role;

public record JwtAuthenticatedUser(Long accountId, Role role) {
}