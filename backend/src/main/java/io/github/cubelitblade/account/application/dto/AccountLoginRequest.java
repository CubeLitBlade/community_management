package io.github.cubelitblade.account.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccountLoginRequest(
        @NotBlank(message = "Username cannot be blank. ")
        String username,

        @NotNull(message = "Password cannot be null. ")
        String password
) {
}
