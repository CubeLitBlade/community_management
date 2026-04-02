package io.github.cubelitblade.user.interfaces.vo;

import io.github.cubelitblade.user.domain.model.Account;

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

