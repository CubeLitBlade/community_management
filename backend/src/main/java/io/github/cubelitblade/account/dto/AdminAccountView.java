package io.github.cubelitblade.account.dto;

import io.github.cubelitblade.account.model.Account;

public record AdminAccountView(
    Long id,
    String username,
    String nickname,
    String role,
    String status,
    boolean mustChangePassword) {
  public static AdminAccountView from(Account account) {
    return new AdminAccountView(
        account.getId(),
        account.getUsername().value(),
        account.getNickname(),
        account.getRole().getValue(),
        account.getStatus().getValue(),
        account.isMustChangePassword());
  }
}
