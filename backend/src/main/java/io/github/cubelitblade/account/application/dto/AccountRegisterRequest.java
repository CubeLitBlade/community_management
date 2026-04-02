package io.github.cubelitblade.account.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AccountRegisterRequest(
        @NotBlank(message = "Username cannot be blank. ")
        @Size(min = 1, max = 20, message = "Username must be between 1 and 20 characters. ")
        String username,

        @NotNull(message = "Password cannot be null. ")
        @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters. ")
        @Pattern(regexp = "^(?![a-zA-Z]+$)(?![0-9]+$)(?![^a-zA-Z0-9]+$).+$",
                message = "Password must contain at least two of the following: letters, numbers, or special characters. ")
        String password
) {
}
