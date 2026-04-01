package io.github.cubelitblade.user.application.dto;

public record AccountLoginRequest(
        String username,
        String password
) {
}
