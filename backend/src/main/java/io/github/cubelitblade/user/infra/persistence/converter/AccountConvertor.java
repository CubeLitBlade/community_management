package io.github.cubelitblade.user.infra.persistence.converter;

import io.github.cubelitblade.user.domain.model.*;
import io.github.cubelitblade.user.infra.persistence.po.AccountPo;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Function;

@Slf4j
public class AccountConvertor {

    public static AccountPo toPo(Account account) {
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
                .lastLoginIp(mapIfNotNull(account.getLastLoginIp(), InetAddress::getHostAddress))
                .build();
    }

    public static Account toDomain(AccountPo accountPo) {
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
                .lastLoginIp(mapIfNotNull(accountPo.getLastLoginIp(), AccountConvertor::parseInetAddress))
                .build();

        return Account.reconstitute(snapshot);
    }

    private static InetAddress parseInetAddress(String ipStr) {
        if (ipStr == null) return null;
        try {
            return InetAddress.getByName(ipStr);
        } catch (UnknownHostException e) {
            log.warn("Invalid IP address: \"{}\" from database", ipStr, e);
            return null;
        }
    }

    private static <S, T> T mapIfNotNull(S source, Function<S, T> mapper) {
        if (source == null) return null;
        return mapper.apply(source);
    }
}
