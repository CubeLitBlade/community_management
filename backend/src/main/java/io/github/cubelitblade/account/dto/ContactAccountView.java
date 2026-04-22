package io.github.cubelitblade.account.dto;

import io.github.cubelitblade.account.model.Account;

public record ContactAccountView(Long id, String username, String nickname) {
  public static ContactAccountView from(Account account) {
    return new ContactAccountView(
        account.getId(), account.getUsername().value(), account.getNickname());
  }
}
