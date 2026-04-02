package io.github.cubelitblade.account.infra.persistence.converter;

import io.github.cubelitblade.account.domain.model.*;
import io.github.cubelitblade.account.infra.persistence.po.AccountPo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccountConvertorTest {

    @Test
    void should_convert_wrapped_fields_to_domain() {

        // Given
        AccountPo po = AccountPo.builder()
                .username("domain_user")
                .passwordHash("db_hash")
                .email("domain@test.com")
                .phone("13900139000")
                .role("user")
                .status("suspended")
                .build();

        // When
        Account account = AccountConvertor.toDomain(po);

        // Then
        assertThat(account)
                .extracting(
                        a -> a.getUsername().value(),
                        a -> a.getPasswordHash().value(),
                        a -> a.getEmail().value(),
                        a -> a.getPhone().value(),
                        Account::getRole,
                        Account::getStatus
                )
                .containsExactly(
                        "domain_user",
                        "db_hash",
                        "domain@test.com",
                        "13900139000",
                        Role.USER,
                        Status.SUSPENDED
                );
    }

    @Test
    void should_preserve_data_when_round_tripping_po_and_domain() {

        // Given
        AccountPo original = AccountPo.builder()
                .username("round_trip")
                .passwordHash("hash")
                .email("rt@test.com")
                .phone("13800138000")
                .role("user")
                .status("normal")
                .build();

        // When
        Account domain = AccountConvertor.toDomain(original);
        AccountPo restored = AccountConvertor.toPo(domain);

        // Then
        assertThat(restored)
                .extracting(
                        AccountPo::getUsername,
                        AccountPo::getPasswordHash,
                        AccountPo::getEmail,
                        AccountPo::getPhone,
                        AccountPo::getRole,
                        AccountPo::getStatus
                )
                .containsExactly(
                        "round_trip",
                        "hash",
                        "rt@test.com",
                        "13800138000",
                        "user",
                        "normal"
                );
    }
}