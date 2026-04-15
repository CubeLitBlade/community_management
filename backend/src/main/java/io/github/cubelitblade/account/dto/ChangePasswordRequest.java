package io.github.cubelitblade.account.dto;

public record ChangePasswordRequest(String currentPassword, String newPassword) {}
