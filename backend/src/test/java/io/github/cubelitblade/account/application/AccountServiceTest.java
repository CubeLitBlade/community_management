package io.github.cubelitblade.account.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.github.cubelitblade.account.dto.AccountLoginRequest;
import io.github.cubelitblade.account.dto.ResetPasswordRequest;
import io.github.cubelitblade.account.exception.AccountForbiddenException;
import io.github.cubelitblade.account.model.Account;
import io.github.cubelitblade.account.model.PasswordHash;
import io.github.cubelitblade.account.model.Role;
import io.github.cubelitblade.account.model.Status;
import io.github.cubelitblade.account.model.Username;
import io.github.cubelitblade.account.persistence.AccountRepository;
import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.account.security.JwtTokenProvider;
import io.github.cubelitblade.common.exception.ValidationException;
import io.github.cubelitblade.common.id.SnowflakeIdGenerator;
import io.github.cubelitblade.common.time.TimeProvider;
import java.net.InetAddress;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
  private static final Instant NOW = Instant.parse("2026-04-18T08:00:00Z");

  @Mock private AccountRepository accountRepository;
  @Mock private PasswordHasher passwordHasher;
  @Mock private SnowflakeIdGenerator idGenerator;
  @Mock private TimeProvider timeProvider;
  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private StringRedisTemplate stringRedisTemplate;

  private AccountService accountService;

  @BeforeEach
  void setUp() {
    accountService =
        new AccountService(
            accountRepository,
            passwordHasher,
            idGenerator,
            timeProvider,
            jwtTokenProvider,
            stringRedisTemplate);
    given(timeProvider.now()).willReturn(NOW);
  }

  @Test
  @DisplayName("Manage accounts: owner can suspend admin")
  void should_allow_owner_to_suspend_admin() {
    Account admin = account(2L, "admin", Role.ADMIN, Status.NORMAL, false, "hashed-admin");
    given(accountRepository.findAccountById(2L)).willReturn(Optional.of(admin));

    var result = accountService.suspendAccount(new JwtAuthenticatedUser(1L, Role.OWNER), 2L);

    verify(accountRepository).updateAccount(admin);
    assertThat(result.status()).isEqualTo("suspended");
    assertThat(admin.getStatus()).isEqualTo(Status.SUSPENDED);
  }

  @Test
  @DisplayName("Manage accounts: admin cannot suspend admin")
  void should_forbid_admin_from_suspending_admin() {
    Account admin = account(2L, "admin", Role.ADMIN, Status.NORMAL, false, "hashed-admin");
    given(accountRepository.findAccountById(2L)).willReturn(Optional.of(admin));

    assertThatThrownBy(
            () -> accountService.suspendAccount(new JwtAuthenticatedUser(10L, Role.ADMIN), 2L))
        .isInstanceOf(AccountForbiddenException.class);

    verify(accountRepository, never()).updateAccount(any());
  }

  @Test
  @DisplayName("Manage accounts: admin can suspend user")
  void should_allow_admin_to_suspend_user() {
    Account user = account(3L, "user", Role.USER, Status.NORMAL, false, "hashed-user");
    given(accountRepository.findAccountById(3L)).willReturn(Optional.of(user));

    var result = accountService.suspendAccount(new JwtAuthenticatedUser(10L, Role.ADMIN), 3L);

    verify(accountRepository).updateAccount(user);
    assertThat(result.status()).isEqualTo("suspended");
  }

  @Test
  @DisplayName("Manage accounts: cannot suspend owner")
  void should_forbid_suspending_owner() {
    Account owner = account(1L, "owner", Role.OWNER, Status.NORMAL, false, "hashed-owner");
    given(accountRepository.findAccountById(1L)).willReturn(Optional.of(owner));

    assertThatThrownBy(
            () -> accountService.suspendAccount(new JwtAuthenticatedUser(9L, Role.OWNER), 1L))
        .isInstanceOf(AccountForbiddenException.class);
  }

  @Test
  @DisplayName("Manage accounts: owner can promote user to admin")
  void should_allow_owner_to_promote_user() {
    Account user = account(3L, "user", Role.USER, Status.NORMAL, false, "hashed-user");
    given(accountRepository.findAccountById(3L)).willReturn(Optional.of(user));

    var result = accountService.promoteAccount(new JwtAuthenticatedUser(1L, Role.OWNER), 3L);

    verify(accountRepository).updateAccount(user);
    assertThat(result.role()).isEqualTo("admin");
    assertThat(user.getRole()).isEqualTo(Role.ADMIN);
  }

  @Test
  @DisplayName("Manage accounts: admin cannot promote anyone")
  void should_forbid_admin_from_promoting() {
    Account user = account(3L, "user", Role.USER, Status.NORMAL, false, "hashed-user");
    given(accountRepository.findAccountById(3L)).willReturn(Optional.of(user));

    assertThatThrownBy(
            () -> accountService.promoteAccount(new JwtAuthenticatedUser(10L, Role.ADMIN), 3L))
        .isInstanceOf(AccountForbiddenException.class);
  }

  @Test
  @DisplayName("Manage accounts: cannot promote admin again")
  void should_reject_promoting_admin_again() {
    Account admin = account(2L, "admin", Role.ADMIN, Status.NORMAL, false, "hashed-admin");
    given(accountRepository.findAccountById(2L)).willReturn(Optional.of(admin));

    assertThatThrownBy(
            () -> accountService.promoteAccount(new JwtAuthenticatedUser(1L, Role.OWNER), 2L))
        .isInstanceOf(ValidationException.class);

    verify(accountRepository, never()).updateAccount(any());
  }

  @Test
  @DisplayName("Manage accounts: admin can reactivate suspended user")
  void should_allow_admin_to_reactivate_suspended_user() {
    Account user = account(3L, "user", Role.USER, Status.SUSPENDED, false, "hashed-user");
    given(accountRepository.findAccountById(3L)).willReturn(Optional.of(user));

    var result = accountService.reactivateAccount(new JwtAuthenticatedUser(10L, Role.ADMIN), 3L);

    verify(accountRepository).updateAccount(user);
    assertThat(result.status()).isEqualTo("normal");
    assertThat(user.getStatus()).isEqualTo(Status.NORMAL);
  }

  @Test
  @DisplayName("Manage accounts: cannot reactivate normal account")
  void should_reject_reactivating_normal_account() {
    Account user = account(3L, "user", Role.USER, Status.NORMAL, false, "hashed-user");
    given(accountRepository.findAccountById(3L)).willReturn(Optional.of(user));

    assertThatThrownBy(
            () -> accountService.reactivateAccount(new JwtAuthenticatedUser(10L, Role.ADMIN), 3L))
        .isInstanceOf(ValidationException.class);

    verify(accountRepository, never()).updateAccount(any());
  }

  @Test
  @DisplayName("Manage accounts: owner can archive admin")
  void should_allow_owner_to_archive_admin() {
    Account admin = account(2L, "admin", Role.ADMIN, Status.NORMAL, false, "hashed-admin");
    given(accountRepository.findAccountById(2L)).willReturn(Optional.of(admin));

    var result = accountService.archiveAccount(new JwtAuthenticatedUser(1L, Role.OWNER), 2L);

    verify(accountRepository).updateAccount(admin);
    assertThat(result.status()).isEqualTo("archived");
    assertThat(result.username()).isEqualTo("admin#archived_2");
    assertThat(admin.getStatus()).isEqualTo(Status.ARCHIVED);
  }

  @Test
  @DisplayName("Manage accounts: owner can demote admin to user")
  void should_allow_owner_to_demote_admin() {
    Account admin = account(2L, "admin", Role.ADMIN, Status.NORMAL, false, "hashed-admin");
    given(accountRepository.findAccountById(2L)).willReturn(Optional.of(admin));

    var result = accountService.demoteAccount(new JwtAuthenticatedUser(1L, Role.OWNER), 2L);

    verify(accountRepository).updateAccount(admin);
    assertThat(result.role()).isEqualTo("user");
    assertThat(admin.getRole()).isEqualTo(Role.USER);
  }

  @Test
  @DisplayName("Manage accounts: cannot demote user")
  void should_reject_demoting_user() {
    Account user = account(3L, "user", Role.USER, Status.NORMAL, false, "hashed-user");
    given(accountRepository.findAccountById(3L)).willReturn(Optional.of(user));

    assertThatThrownBy(
            () -> accountService.demoteAccount(new JwtAuthenticatedUser(1L, Role.OWNER), 3L))
        .isInstanceOf(ValidationException.class);

    verify(accountRepository, never()).updateAccount(any());
  }

  @Test
  @DisplayName("Manage accounts: reset password marks target to change password on next login")
  void should_mark_account_to_change_password_after_reset() throws Exception {
    Account user = account(3L, "user", Role.USER, Status.NORMAL, false, "hashed-user");
    PasswordHash resetHash = new PasswordHash("hashed-new");
    given(accountRepository.findAccountById(3L)).willReturn(Optional.of(user));
    given(passwordHasher.fromRaw("Newpass123")).willReturn(resetHash);
    given(accountRepository.findByUsername("user")).willReturn(Optional.of(user));
    given(passwordHasher.matches("Newpass123", resetHash)).willReturn(true);
    given(jwtTokenProvider.generateToken(eq(3L), eq(Role.USER), eq(NOW))).willReturn("jwt-token");

    accountService.resetPassword(
        new JwtAuthenticatedUser(10L, Role.ADMIN), 3L, new ResetPasswordRequest("Newpass123"));

    var loginResult =
        accountService.login(
            new AccountLoginRequest("user", "Newpass123"), InetAddress.getByName("127.0.0.1"));

    assertThat(loginResult.response().mustChangePassword()).isTrue();
  }

  @Test
  @DisplayName("Manage accounts: should list all accounts for moderators")
  void should_list_accounts_for_moderators() {
    Account owner = account(1L, "owner", Role.OWNER, Status.NORMAL, false, "hashed-owner");
    Account admin = account(2L, "admin", Role.ADMIN, Status.NORMAL, false, "hashed-admin");
    Account user = account(3L, "user", Role.USER, Status.SUSPENDED, true, "hashed-user");
    given(accountRepository.findAllAccounts()).willReturn(List.of(owner, admin, user));

    var response = accountService.getManageableAccounts(new JwtAuthenticatedUser(1L, Role.OWNER));

    assertThat(response.accounts()).hasSize(3);
    assertThat(response.accounts().get(2).mustChangePassword()).isTrue();
  }

  private Account account(
      Long id,
      String username,
      Role role,
      Status status,
      boolean mustChangePassword,
      String passwordHash) {
    return Account.reconstitute(
        Account.Snapshot.builder()
            .id(id)
            .username(Username.of(username))
            .nickname(username)
            .role(role)
            .status(status)
            .mustChangePassword(mustChangePassword)
            .passwordHash(new PasswordHash(passwordHash))
            .createdAt(NOW.minusSeconds(3600))
            .updatedAt(NOW.minusSeconds(1800))
            .build());
  }
}
