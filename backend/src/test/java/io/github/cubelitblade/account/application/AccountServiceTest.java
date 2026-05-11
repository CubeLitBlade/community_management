package io.github.cubelitblade.account.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.github.cubelitblade.account.dto.AccountLoginRequest;
import io.github.cubelitblade.account.dto.AccountRegisterRequest;
import io.github.cubelitblade.account.dto.ChangePasswordRequest;
import io.github.cubelitblade.account.dto.ResetPasswordRequest;
import io.github.cubelitblade.account.exception.AccountConflictException;
import io.github.cubelitblade.account.exception.AccountForbiddenException;
import io.github.cubelitblade.account.exception.AccountInputException;
import io.github.cubelitblade.account.exception.AccountNotFoundException;
import io.github.cubelitblade.account.exception.AccountStateException;
import io.github.cubelitblade.account.exception.LoginFailedException;
import io.github.cubelitblade.account.model.Account;
import io.github.cubelitblade.account.model.PasswordHash;
import io.github.cubelitblade.account.model.Role;
import io.github.cubelitblade.account.model.Status;
import io.github.cubelitblade.account.model.Username;
import io.github.cubelitblade.account.persistence.AccountRepository;
import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.account.security.JwtTokenProvider;
import io.github.cubelitblade.common.exception.ApiErrorCode;
import io.github.cubelitblade.common.exception.DomainException;
import io.github.cubelitblade.common.exception.ValidationException;
import io.github.cubelitblade.common.id.SnowflakeIdGenerator;
import io.github.cubelitblade.common.time.TimeProvider;
import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

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
  }

  @Test
  @DisplayName("Register: should create user with hashed password and contact info")
  void should_register_account() {
    given(timeProvider.now()).willReturn(NOW);
    given(idGenerator.nextId()).willReturn(100L);
    given(passwordHasher.fromRaw("Pass123")).willReturn(new PasswordHash("hashed-pass"));

    Account account =
        accountService.register(
            new AccountRegisterRequest("new_user", "Pass123", "NEW@Example.COM", "+12345678901"));

    verify(accountRepository).register(account);
    assertThat(account.getId()).isEqualTo(100L);
    assertThat(account.getUsername().value()).isEqualTo("new_user");
    assertThat(account.getPasswordHash().value()).isEqualTo("hashed-pass");
    assertThat(account.getEmail().value()).isEqualTo("new@example.com");
    assertThat(account.getPhone().value()).isEqualTo("+12345678901");
    assertThat(account.getRole()).isEqualTo(Role.USER);
    assertThat(account.getStatus()).isEqualTo(Status.NORMAL);
    assertThat(account.isMustChangePassword()).isFalse();
  }

  @Test
  @DisplayName("Register: should reject blank required fields and missing contact")
  void should_reject_register_required_field_errors() {
    assertDomainError(
        () -> accountService.register(new AccountRegisterRequest(" ", "Pass123", "a@b.com", null)),
        AccountInputException.class,
        ApiErrorCode.INPUT_USERNAME_BLANK);
    assertDomainError(
        () -> accountService.register(new AccountRegisterRequest("user", " ", "a@b.com", null)),
        AccountInputException.class,
        ApiErrorCode.INPUT_PASSWORD_BLANK);
    assertDomainError(
        () -> accountService.register(new AccountRegisterRequest("user", "Pass123", " ", null)),
        AccountInputException.class,
        ApiErrorCode.INPUT_NO_CONTACT);

    verify(accountRepository, never()).register(any());
  }

  @Test
  @DisplayName("Register: should reject invalid field formats")
  void should_reject_register_format_errors() {
    assertDomainError(
        () ->
            accountService.register(
                new AccountRegisterRequest(
                    "this_username_is_too_long", "Pass123", "a@b.com", null)),
        AccountInputException.class,
        ApiErrorCode.INPUT_USERNAME_BAD_LENGTH);
    assertDomainError(
        () ->
            accountService.register(new AccountRegisterRequest("user", "abcdef", "a@b.com", null)),
        AccountInputException.class,
        ApiErrorCode.INPUT_PASSWORD_BAD_FORMAT);
    assertDomainError(
        () ->
            accountService.register(
                new AccountRegisterRequest("user", "Pass123", "not-an-email", null)),
        AccountInputException.class,
        ApiErrorCode.INPUT_EMAIL_BAD_FORMAT);
    assertDomainError(
        () -> accountService.register(new AccountRegisterRequest("user", "Pass123", null, "12345")),
        AccountInputException.class,
        ApiErrorCode.INPUT_PHONE_BAD_FORMAT);

    verify(accountRepository, never()).register(any());
  }

  @Test
  @DisplayName("Register: should reject unique field conflicts")
  void should_reject_register_conflicts() {
    given(accountRepository.existsUserByUsername("user")).willReturn(true);
    assertDomainError(
        () ->
            accountService.register(new AccountRegisterRequest("user", "Pass123", "a@b.com", null)),
        AccountConflictException.class,
        ApiErrorCode.CONFLICT_USERNAME_EXISTS);

    given(accountRepository.existsUserByEmail("used@example.com")).willReturn(true);
    assertDomainError(
        () ->
            accountService.register(
                new AccountRegisterRequest("other", "Pass123", "used@example.com", null)),
        AccountConflictException.class,
        ApiErrorCode.CONFLICT_EMAIL_EXISTS);

    given(accountRepository.existsUserByPhone("12345678901")).willReturn(true);
    assertDomainError(
        () ->
            accountService.register(
                new AccountRegisterRequest("other", "Pass123", null, "12345678901")),
        AccountConflictException.class,
        ApiErrorCode.CONFLICT_PHONE_EXISTS);

    verify(accountRepository, never()).register(any());
  }

  @Test
  @DisplayName("Login: should reject missing user, bad password, suspended, and archived accounts")
  void should_reject_login_failures() throws Exception {
    InetAddress clientIp = InetAddress.getByName("127.0.0.1");
    Account user = account(3L, "user", Role.USER, Status.NORMAL, false, "hashed-user");
    Account suspended = account(4L, "suspended", Role.USER, Status.SUSPENDED, false, "hash");
    Account archived = account(5L, "archived", Role.USER, Status.ARCHIVED, false, "hash");

    given(accountRepository.findByUsername("missing")).willReturn(Optional.empty());
    assertDomainError(
        () -> accountService.login(new AccountLoginRequest("missing", "Pass123"), clientIp),
        LoginFailedException.class,
        ApiErrorCode.LOGIN_FAILED_INVALID_CREDENTIALS);

    given(accountRepository.findByUsername("user")).willReturn(Optional.of(user));
    given(passwordHasher.matches("wrong", user.getPasswordHash())).willReturn(false);
    assertDomainError(
        () -> accountService.login(new AccountLoginRequest("user", "wrong"), clientIp),
        LoginFailedException.class,
        ApiErrorCode.LOGIN_FAILED_INVALID_CREDENTIALS);

    given(accountRepository.findByUsername("suspended")).willReturn(Optional.of(suspended));
    given(passwordHasher.matches("Pass123", suspended.getPasswordHash())).willReturn(true);
    assertDomainError(
        () -> accountService.login(new AccountLoginRequest("suspended", "Pass123"), clientIp),
        LoginFailedException.class,
        ApiErrorCode.LOGIN_FAILED_SUSPENDED);

    given(accountRepository.findByUsername("archived")).willReturn(Optional.of(archived));
    given(passwordHasher.matches("Pass123", archived.getPasswordHash())).willReturn(true);
    assertDomainError(
        () -> accountService.login(new AccountLoginRequest("archived", "Pass123"), clientIp),
        LoginFailedException.class,
        ApiErrorCode.LOGIN_FAILED_ARCHIVED);

    verify(accountRepository, never()).updateAccount(any());
  }

  @Test
  @DisplayName("Login: should record login metadata and generate token")
  void should_login_and_generate_token() throws Exception {
    InetAddress clientIp = InetAddress.getByName("127.0.0.1");
    Account user = account(3L, "user", Role.USER, Status.NORMAL, true, "hashed-user");
    given(timeProvider.now()).willReturn(NOW);
    given(accountRepository.findByUsername("user")).willReturn(Optional.of(user));
    given(passwordHasher.matches("Pass123", user.getPasswordHash())).willReturn(true);
    given(jwtTokenProvider.generateToken(3L, Role.USER, NOW)).willReturn("jwt-token");

    var result = accountService.login(new AccountLoginRequest("user", "Pass123"), clientIp);

    verify(accountRepository).updateAccount(user);
    assertThat(result.token()).isEqualTo("jwt-token");
    assertThat(result.response().mustChangePassword()).isTrue();
    assertThat(user.getLastLoginAt()).isEqualTo(NOW);
    assertThat(user.getLastLoginIp()).isEqualTo(clientIp);
  }

  @Test
  @DisplayName("Change password: should reject invalid current or new password")
  void should_reject_change_password_input_errors() {
    Account user = account(3L, "user", Role.USER, Status.NORMAL, true, "hashed-user");
    given(accountRepository.findAccountById(3L)).willReturn(Optional.of(user));

    assertDomainError(
        () -> accountService.changePassword(3L, new ChangePasswordRequest(" ", "Newpass123")),
        AccountInputException.class,
        ApiErrorCode.INPUT_PASSWORD_BLANK);
    assertDomainError(
        () -> accountService.changePassword(3L, new ChangePasswordRequest("Oldpass123", "short")),
        AccountInputException.class,
        ApiErrorCode.INPUT_PASSWORD_BAD_LENGTH);

    verify(accountRepository, never()).updateAccount(any());
  }

  @Test
  @DisplayName("Change password: should reject missing account and wrong current password")
  void should_reject_change_password_not_found_and_wrong_current_password() {
    Account user = account(3L, "user", Role.USER, Status.NORMAL, true, "hashed-user");
    given(accountRepository.findAccountById(404L)).willReturn(Optional.empty());
    assertThatThrownBy(
            () ->
                accountService.changePassword(
                    404L, new ChangePasswordRequest("Oldpass123", "Newpass123")))
        .isInstanceOf(AccountNotFoundException.class);

    given(accountRepository.findAccountById(3L)).willReturn(Optional.of(user));
    given(passwordHasher.matches("wrong", user.getPasswordHash())).willReturn(false);
    assertDomainError(
        () -> accountService.changePassword(3L, new ChangePasswordRequest("wrong", "Newpass123")),
        LoginFailedException.class,
        ApiErrorCode.LOGIN_FAILED_INVALID_CREDENTIALS);

    verify(accountRepository, never()).updateAccount(any());
  }

  @Test
  @DisplayName("Change password: should update hash and clear must-change flag")
  void should_change_password() {
    Account user = account(3L, "user", Role.USER, Status.NORMAL, true, "hashed-user");
    given(accountRepository.findAccountById(3L)).willReturn(Optional.of(user));
    given(passwordHasher.matches("Oldpass123", user.getPasswordHash())).willReturn(true);
    given(passwordHasher.fromRaw("Newpass123")).willReturn(new PasswordHash("hashed-new"));
    given(timeProvider.now()).willReturn(NOW);

    accountService.changePassword(3L, new ChangePasswordRequest("Oldpass123", "Newpass123"));

    verify(accountRepository).updateAccount(user);
    assertThat(user.getPasswordHash().value()).isEqualTo("hashed-new");
    assertThat(user.isMustChangePassword()).isFalse();
    assertThat(user.getUpdatedAt()).isEqualTo(NOW);
  }

  @Test
  @DisplayName("Logout: should blacklist unexpired token and ignore expired token")
  @SuppressWarnings("unchecked")
  void should_blacklist_only_unexpired_logout_token() {
    ValueOperations<String, String> valueOperations =
        org.mockito.Mockito.mock(ValueOperations.class);
    given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
    given(timeProvider.now()).willReturn(NOW);
    given(jwtTokenProvider.getExpirationDate("active-token"))
        .willReturn(Date.from(NOW.plusSeconds(60)));
    given(jwtTokenProvider.getExpirationDate("expired-token"))
        .willReturn(Date.from(NOW.minusSeconds(1)));

    accountService.logout("active-token");
    accountService.logout("expired-token");

    ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);
    verify(valueOperations)
        .set(eq("jwt:blacklist:active-token"), eq("logout"), durationCaptor.capture());
    assertThat(durationCaptor.getValue()).isEqualTo(Duration.ofSeconds(60));
    verify(valueOperations, never()).set(eq("jwt:blacklist:expired-token"), eq("logout"), any());
  }

  @Test
  @DisplayName("Manage accounts: owner can suspend admin")
  void should_allow_owner_to_suspend_admin() {
    Account admin = account(2L, "admin", Role.ADMIN, Status.NORMAL, false, "hashed-admin");
    given(accountRepository.findAccountById(2L)).willReturn(Optional.of(admin));
    given(timeProvider.now()).willReturn(NOW);

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
    given(timeProvider.now()).willReturn(NOW);

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
    given(timeProvider.now()).willReturn(NOW);

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
    given(timeProvider.now()).willReturn(NOW);

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
    given(timeProvider.now()).willReturn(NOW);

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
    given(timeProvider.now()).willReturn(NOW);

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
    given(timeProvider.now()).willReturn(NOW);

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

  @Test
  @DisplayName("Manage accounts: should reject list for non-manager")
  void should_reject_manageable_account_list_for_non_manager() {
    assertThatThrownBy(
            () -> accountService.getManageableAccounts(new JwtAuthenticatedUser(3L, Role.USER)))
        .isInstanceOf(AccountForbiddenException.class);
    assertThatThrownBy(() -> accountService.getManageableAccounts(null))
        .isInstanceOf(AccountForbiddenException.class);

    verify(accountRepository, never()).findAllAccounts();
  }

  @Test
  @DisplayName("Manage accounts: should reject missing targets")
  void should_reject_missing_manage_target() {
    given(accountRepository.findAccountById(404L)).willReturn(Optional.empty());

    assertThatThrownBy(
            () -> accountService.archiveAccount(new JwtAuthenticatedUser(1L, Role.OWNER), 404L))
        .isInstanceOf(AccountNotFoundException.class);

    verify(accountRepository, never()).updateAccount(any());
  }

  @Test
  @DisplayName("Manage accounts: owner cannot manage owner or self")
  void should_forbid_owner_from_managing_owner_or_self() {
    Account otherOwner = account(2L, "other_owner", Role.OWNER, Status.NORMAL, false, "hashed");
    Account self = account(1L, "owner", Role.ADMIN, Status.NORMAL, false, "hashed-owner");
    given(accountRepository.findAccountById(2L)).willReturn(Optional.of(otherOwner));
    given(accountRepository.findAccountById(1L)).willReturn(Optional.of(self));

    assertThatThrownBy(
            () -> accountService.archiveAccount(new JwtAuthenticatedUser(1L, Role.OWNER), 2L))
        .isInstanceOf(AccountForbiddenException.class);
    assertThatThrownBy(
            () -> accountService.archiveAccount(new JwtAuthenticatedUser(1L, Role.OWNER), 1L))
        .isInstanceOf(AccountForbiddenException.class);

    verify(accountRepository, never()).updateAccount(any());
  }

  @Test
  @DisplayName("Manage accounts: archived targets cannot be changed")
  void should_reject_management_operations_on_archived_targets() {
    Account archivedUser = account(3L, "archived_user", Role.USER, Status.ARCHIVED, false, "hash");
    Account archivedAdmin =
        account(4L, "archived_admin", Role.ADMIN, Status.ARCHIVED, false, "hash");
    given(accountRepository.findAccountById(3L)).willReturn(Optional.of(archivedUser));
    given(accountRepository.findAccountById(4L)).willReturn(Optional.of(archivedAdmin));

    assertDomainError(
        () -> accountService.suspendAccount(new JwtAuthenticatedUser(1L, Role.OWNER), 3L),
        AccountStateException.class,
        ApiErrorCode.ACCOUNT_STATE_ARCHIVED);
    assertDomainError(
        () -> accountService.reactivateAccount(new JwtAuthenticatedUser(1L, Role.OWNER), 3L),
        AccountStateException.class,
        ApiErrorCode.ACCOUNT_STATE_ARCHIVED);
    assertDomainError(
        () -> accountService.promoteAccount(new JwtAuthenticatedUser(1L, Role.OWNER), 3L),
        AccountStateException.class,
        ApiErrorCode.ACCOUNT_STATE_ARCHIVED);
    assertDomainError(
        () -> accountService.demoteAccount(new JwtAuthenticatedUser(1L, Role.OWNER), 4L),
        AccountStateException.class,
        ApiErrorCode.ACCOUNT_STATE_ARCHIVED);
    assertDomainError(
        () -> accountService.archiveAccount(new JwtAuthenticatedUser(1L, Role.OWNER), 3L),
        AccountStateException.class,
        ApiErrorCode.ACCOUNT_STATE_ARCHIVED);

    verify(accountRepository, never()).updateAccount(any());
  }

  private <T extends DomainException> void assertDomainError(
      ThrowingCallable callable, Class<T> exceptionType, ApiErrorCode errorCode) {
    assertThatThrownBy(callable)
        .isInstanceOf(exceptionType)
        .satisfies(
            error -> assertThat(((DomainException) error).getErrorCode()).isEqualTo(errorCode));
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
