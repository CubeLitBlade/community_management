package io.github.cubelitblade.account.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cubelitblade.account.model.Account;
import io.github.cubelitblade.account.model.Email;
import io.github.cubelitblade.account.model.PasswordHash;
import io.github.cubelitblade.account.model.Phone;
import io.github.cubelitblade.account.model.Profile;
import io.github.cubelitblade.account.model.Role;
import io.github.cubelitblade.account.model.Status;
import io.github.cubelitblade.account.model.Username;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class AccountPersistenceIntegrationTest {
  private static final Instant NOW = Instant.parse("2026-04-22T08:00:00Z");

  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private AccountRepository accountRepository;

  @BeforeEach
  void setUp() {
    insertAccount(20101L, "zeta_contact", "Zeta Contact", "normal", "user");
    insertAccount(20102L, "alpha_contact", "Alpha Contact", "normal", "admin");
    insertAccount(20103L, "suspended_contact", "Suspended Contact", "suspended", "user");
    insertAccount(20104L, "archived_contact", "Archived Contact", "archived", "user");
  }

  @Test
  @DisplayName("AccountRepository: should register and read complete account fields")
  void should_register_and_read_complete_account_fields() throws Exception {
    InetAddress lastLoginIp = InetAddress.getByName("127.0.0.11");
    Account account =
        Account.reconstitute(
            Account.Snapshot.builder()
                .id(20110L)
                .username(Username.of("registered_user"))
                .passwordHash(new PasswordHash("hashed-password"))
                .mustChangePassword(true)
                .nickname("Registered User")
                .email(Email.of("registered@example.com"))
                .phone(Phone.of("+12345678901"))
                .profile(new Profile(2))
                .role(Role.ADMIN)
                .status(Status.SUSPENDED)
                .createdAt(NOW)
                .updatedAt(NOW.plusSeconds(10))
                .lastLoginAt(NOW.plusSeconds(20))
                .lastLoginIp(lastLoginIp)
                .build());

    accountRepository.register(account);

    assertThat(accountRepository.findAccountById(20110L))
        .hasValueSatisfying(
            persisted -> {
              assertThat(persisted.getUsername().value()).isEqualTo("registered_user");
              assertThat(persisted.getPasswordHash().value()).isEqualTo("hashed-password");
              assertThat(persisted.isMustChangePassword()).isTrue();
              assertThat(persisted.getNickname()).isEqualTo("Registered User");
              assertThat(persisted.getEmail().value()).isEqualTo("registered@example.com");
              assertThat(persisted.getPhone().value()).isEqualTo("+12345678901");
              assertThat(persisted.getProfile()).isEqualTo(new Profile(2));
              assertThat(persisted.getRole()).isEqualTo(Role.ADMIN);
              assertThat(persisted.getStatus()).isEqualTo(Status.SUSPENDED);
              assertThat(persisted.getCreatedAt()).isEqualTo(NOW);
              assertThat(persisted.getUpdatedAt()).isEqualTo(NOW.plusSeconds(10));
              assertThat(persisted.getLastLoginAt()).isEqualTo(NOW.plusSeconds(20));
              assertThat(persisted.getLastLoginIp()).isEqualTo(lastLoginIp);
            });
  }

  @Test
  @DisplayName("AccountRepository: should update all mutable account columns")
  void should_update_all_mutable_account_columns() throws Exception {
    Account account = account(20120L, "before_update", Role.USER, Status.NORMAL, false);
    InetAddress lastLoginIp = InetAddress.getByName("192.168.0.8");
    accountRepository.register(account);

    Account updated =
        Account.reconstitute(
            Account.Snapshot.builder()
                .id(20120L)
                .username(Username.reconstitute("after_update"))
                .passwordHash(new PasswordHash("updated-hash"))
                .mustChangePassword(true)
                .nickname("Updated Nickname")
                .email(Email.of("updated@example.com"))
                .phone(Phone.of("12345678901"))
                .profile(new Profile(9))
                .role(Role.ADMIN)
                .status(Status.ARCHIVED)
                .createdAt(NOW.minusSeconds(3600))
                .updatedAt(NOW.plusSeconds(30))
                .lastLoginAt(NOW.plusSeconds(40))
                .lastLoginIp(lastLoginIp)
                .build());

    accountRepository.updateAccount(updated);

    assertThat(accountRepository.findAccountById(20120L))
        .hasValueSatisfying(
            persisted -> {
              assertThat(persisted.getUsername().value()).isEqualTo("after_update");
              assertThat(persisted.getPasswordHash().value()).isEqualTo("updated-hash");
              assertThat(persisted.isMustChangePassword()).isTrue();
              assertThat(persisted.getNickname()).isEqualTo("Updated Nickname");
              assertThat(persisted.getEmail().value()).isEqualTo("updated@example.com");
              assertThat(persisted.getPhone().value()).isEqualTo("12345678901");
              assertThat(persisted.getProfile()).isEqualTo(new Profile(9));
              assertThat(persisted.getRole()).isEqualTo(Role.ADMIN);
              assertThat(persisted.getStatus()).isEqualTo(Status.ARCHIVED);
              assertThat(persisted.getUpdatedAt()).isEqualTo(NOW.plusSeconds(30));
              assertThat(persisted.getLastLoginAt()).isEqualTo(NOW.plusSeconds(40));
              assertThat(persisted.getLastLoginIp()).isEqualTo(lastLoginIp);
            });
  }

  @Test
  @DisplayName("AccountRepository: should find by username and uniqueness fields")
  void should_find_by_username_and_check_unique_fields() {
    insertAccount(
        20130L,
        "unique_lookup",
        "Unique Lookup",
        "unique@example.com",
        "10987654321",
        "normal",
        "user");

    assertThat(accountRepository.findByUsername("unique_lookup"))
        .hasValueSatisfying(account -> assertThat(account.getId()).isEqualTo(20130L));
    assertThat(accountRepository.findByUsername("missing_user")).isEmpty();
    assertThat(accountRepository.existsUserByUsername("unique_lookup")).isTrue();
    assertThat(accountRepository.existsUserByUsername("missing_user")).isFalse();
    assertThat(accountRepository.existsUserByEmail("unique@example.com")).isTrue();
    assertThat(accountRepository.existsUserByEmail("missing@example.com")).isFalse();
    assertThat(accountRepository.existsUserByPhone("10987654321")).isTrue();
    assertThat(accountRepository.existsUserByPhone("10000000000")).isFalse();
  }

  @Test
  @DisplayName("AccountRepository: should list accounts ordered by id")
  void should_find_all_accounts_ordered_by_id() {
    List<Account> accounts =
        accountRepository.findAllAccounts().stream()
            .filter(account -> List.of(20101L, 20102L, 20103L, 20104L).contains(account.getId()))
            .toList();

    assertThat(accounts).extracting(Account::getId).containsExactly(20101L, 20102L, 20103L, 20104L);
  }

  @Test
  @DisplayName(
      "AccountRepository: should list normal contacts excluding current account by username")
  void should_find_normal_contacts_excluding_current_account_ordered_by_username() {
    List<Account> contacts =
        accountRepository.findNormalContactsExcluding(20101L).stream()
            .filter(account -> List.of(20101L, 20102L, 20103L, 20104L).contains(account.getId()))
            .toList();

    assertThat(contacts).extracting(Account::getId).containsExactly(20102L);
    assertThat(contacts)
        .extracting(account -> account.getUsername().value())
        .containsExactly("alpha_contact");
    assertThat(contacts).extracting(Account::getStatus).containsOnly(Status.NORMAL);
  }

  @Test
  @DisplayName("AccountRepository: should find accounts by ids and short-circuit empty ids")
  void should_find_accounts_by_ids_and_return_empty_for_empty_ids() {
    assertThat(accountRepository.findAccountsByIds(List.of())).isEmpty();
    assertThat(accountRepository.findAccountsByIds(null)).isEmpty();

    List<Account> accounts = accountRepository.findAccountsByIds(List.of(20104L, 20101L, 99999L));

    assertThat(accounts).extracting(Account::getId).containsExactlyInAnyOrder(20101L, 20104L);
  }

  private Account account(
      Long id, String username, Role role, Status status, boolean mustChangePassword) {
    return Account.reconstitute(
        Account.Snapshot.builder()
            .id(id)
            .username(Username.of(username))
            .passwordHash(new PasswordHash("hashed-" + username))
            .mustChangePassword(mustChangePassword)
            .nickname(username)
            .role(role)
            .status(status)
            .createdAt(NOW.minusSeconds(3600))
            .updatedAt(NOW.minusSeconds(1800))
            .build());
  }

  private void insertAccount(
      Long id, String username, String nickname, String status, String role) {
    insertAccount(id, username, nickname, null, null, status, role);
  }

  private void insertAccount(
      Long id,
      String username,
      String nickname,
      String email,
      String phone,
      String status,
      String role) {
    jdbcTemplate.update(
        """
        insert into accounts(
          id, username, nickname, password_hash, email, phone, status, role, created_at, updated_at
        )
        values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        id,
        username,
        nickname,
        "hash-" + username,
        email,
        phone,
        status,
        role,
        ts(NOW),
        ts(NOW));
  }

  private Timestamp ts(Instant instant) {
    return instant == null ? null : Timestamp.from(instant);
  }
}
