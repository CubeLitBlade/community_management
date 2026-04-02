package io.github.cubelitblade.account.interfaces.vo;

import io.github.cubelitblade.account.domain.model.Account;

public record AccountMeResponse(
        Long id,
        String username,
        String nickname,
        String role,
        String status
) {
    public static AccountMeResponse from(Account account) {
        return new AccountMeResponse(
                account.getId(),
                account.getUsername().value(),
                account.getNickname(),
                account.getRole().getValue(),
                account.getStatus().getValue()
        );
    }
}

