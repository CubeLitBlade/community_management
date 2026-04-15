package io.github.cubelitblade.account.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cubelitblade.account.model.Account;
import io.github.cubelitblade.account.model.Role;
import io.github.cubelitblade.account.model.Status;
import org.junit.jupiter.api.Test;

class AccountPoMappingTest {

  @Test
  void should_convert_wrapped_fields_to_domain() {

    // Given
    AccountPo po =
        AccountPo.builder()
            .username("domain_user")
            .passwordHash("db_hash")
            .mustChangePassword(true)
            .email("domain@test.com")
            .phone("13900139000")
            .role("user")
            .status("suspended")
            .build();

    // When
    Account account = po.toDomain();

    // Then
    assertThat(account)
        .extracting(
            a -> a.getUsername().value(),
            a -> a.getPasswordHash().value(),
            Account::isMustChangePassword,
            a -> a.getEmail().value(),
            a -> a.getPhone().value(),
            Account::getRole,
            Account::getStatus)
        .containsExactly(
            "domain_user",
            "db_hash",
            true,
            "domain@test.com",
            "13900139000",
            Role.USER,
            Status.SUSPENDED);
  }

  @Test
  void should_preserve_data_when_round_tripping_po_and_domain() {

    // Given
    AccountPo original =
        AccountPo.builder()
            .username("round_trip")
            .passwordHash("hash")
            .mustChangePassword(true)
            .email("rt@test.com")
            .phone("13800138000")
            .role("user")
            .status("normal")
            .build();

    // When
    Account domain = original.toDomain();
    AccountPo restored = AccountPo.fromDomain(domain);

    // Then
    assertThat(restored)
        .extracting(
            AccountPo::getUsername,
            AccountPo::getPasswordHash,
            AccountPo::getMustChangePassword,
            AccountPo::getEmail,
            AccountPo::getPhone,
            AccountPo::getRole,
            AccountPo::getStatus)
        .containsExactly(
            "round_trip", "hash", true, "rt@test.com", "13800138000", "user", "normal");
  }
}
