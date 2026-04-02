package io.github.cubelitblade.account.infra.persistence.converter;

import io.github.cubelitblade.account.domain.model.*;
import io.github.cubelitblade.account.infra.persistence.po.AccountPo;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j
public class AccountConvertor {

    public static AccountPo toPo(Account account) {
        if (account == null) {
            return null;
        }
        return AccountPo.builder()
                .id(account.getId())
                .username(mapIfNotNull(account.getUsername(), Username::value))
                .passwordHash(mapIfNotNull(account.getPasswordHash(), PasswordHash::value))
                .nickname(account.getNickname())
                .email(mapIfNotNull(account.getEmail(), Email::value))
                .phone(mapIfNotNull(account.getPhone(), Phone::value))
                .profile(account.getProfile())
                .role(mapIfNotNull(account.getRole(), Role::getValue))
                .status(mapIfNotNull(account.getStatus(), Status::getValue))
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .lastLoginAt(account.getLastLoginAt())
                .lastLoginIp(account.getLastLoginIp())
                .build();
    }

    public static Account toDomain(AccountPo accountPo) {
        if (accountPo == null) {
            return null;
        }
        Account.Snapshot snapshot = Account.Snapshot.builder()
                .id(accountPo.getId())
                .username(mapIfNotNull(accountPo.getUsername(), Username::reconstitute))
                .passwordHash(mapIfNotNull(accountPo.getPasswordHash(), PasswordHash::new))
                .nickname(accountPo.getNickname())
                .email(mapIfNotNull(accountPo.getEmail(), Email::new))
                .phone(mapIfNotNull(accountPo.getPhone(), Phone::new))
                .profile(accountPo.getProfile())
                .role(mapIfNotNull(accountPo.getRole(), Role::from))
                .status(mapIfNotNull(accountPo.getStatus(), Status::from))
                .createdAt(accountPo.getCreatedAt())
                .updatedAt(accountPo.getUpdatedAt())
                .lastLoginAt(accountPo.getLastLoginAt())
                .lastLoginIp(accountPo.getLastLoginIp())
                .build();

        return Account.reconstitute(snapshot);
    }

    private static <S, T> T mapIfNotNull(S source, Function<S, T> mapper) {
        if (source == null) return null;
        return mapper.apply(source);
    }
}
