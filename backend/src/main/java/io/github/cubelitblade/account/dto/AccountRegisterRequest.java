package io.github.cubelitblade.account.dto;

import jakarta.annotation.Nullable;

public record AccountRegisterRequest(
    String username, String password, @Nullable String email, @Nullable String phone) {}
