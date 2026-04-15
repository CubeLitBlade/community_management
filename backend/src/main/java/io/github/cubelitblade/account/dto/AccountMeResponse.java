package io.github.cubelitblade.account.dto;

import io.github.cubelitblade.account.model.Account;

public record AccountMeResponse(
    Long id,
    String username,
    String nickname,
    String role,
    String status,
    boolean mustChangePassword) {
  public static AccountMeResponse from(Account account) {
    return new AccountMeResponse(
        account.getId(),
        account.getUsername().value(),
        account.getNickname(),
        account.getRole().getValue(),
        account.getStatus().getValue(),
        account.isMustChangePassword());
  }
}
