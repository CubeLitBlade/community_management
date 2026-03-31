package io.github.cubelitblade.user.application.dto;

public record AccountRegisterRequest(
        String username,
        String rawPassword
) {
}
