package io.github.cubelitblade.account.application;

import io.github.cubelitblade.account.application.validation.*;
import io.github.cubelitblade.account.dto.AccountLoginRequest;
import io.github.cubelitblade.account.dto.AccountRegisterRequest;
import io.github.cubelitblade.account.dto.AdminAccountListResponse;
import io.github.cubelitblade.account.dto.AdminAccountView;
import io.github.cubelitblade.account.dto.ChangePasswordRequest;
import io.github.cubelitblade.account.dto.ContactAccountListResponse;
import io.github.cubelitblade.account.dto.ContactAccountView;
import io.github.cubelitblade.account.dto.RegisterFieldsCheckRequest;
import io.github.cubelitblade.account.dto.RegisterFieldsCheckResponse;
import io.github.cubelitblade.account.dto.ResetPasswordRequest;
import io.github.cubelitblade.account.dto.TokenResponse;
import io.github.cubelitblade.account.exception.AccountConflictException;
import io.github.cubelitblade.account.exception.AccountForbiddenException;
import io.github.cubelitblade.account.exception.AccountInputException;
import io.github.cubelitblade.account.exception.AccountNotFoundException;
import io.github.cubelitblade.account.exception.AccountStateException;
import io.github.cubelitblade.account.exception.LoginFailedException;
import io.github.cubelitblade.account.model.Account;
import io.github.cubelitblade.account.model.Role;
import io.github.cubelitblade.account.model.Status;
import io.github.cubelitblade.account.model.Username;
import io.github.cubelitblade.account.persistence.AccountRepository;
import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.account.security.JwtTokenProvider;
import io.github.cubelitblade.common.exception.ApiErrorCode;
import io.github.cubelitblade.common.exception.ValidationException;
import io.github.cubelitblade.common.id.SnowflakeIdGenerator;
import io.github.cubelitblade.common.time.TimeProvider;
import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {
  private final AccountRepository accountRepository;
  private final PasswordHasher passwordHasher;
  private final SnowflakeIdGenerator idGenerator;
  private final TimeProvider timeProvider;
  private final JwtTokenProvider jwtTokenProvider;

  private static final Predicate<String> SKIP_UNIQUENESS_CHECK = _ -> false;
  private final StringRedisTemplate stringRedisTemplate;

  @Transactional
  public Account register(AccountRegisterRequest request) {
    Instant now = timeProvider.now();

    // Fast-fail for required fields.
    // The engine skips nulls, so mandatory blanks must be caught early.
    if (request.username() == null || request.username().isBlank()) {
      throw AccountInputException.from(ApiErrorCode.INPUT_USERNAME_BLANK);
    }
    if (request.password() == null || request.password().isBlank()) {
      throw AccountInputException.from(ApiErrorCode.INPUT_PASSWORD_BLANK);
    }
    if (nullIfBlank(request.email()) == null && nullIfBlank(request.phone()) == null) {
      throw AccountInputException.from(ApiErrorCode.INPUT_NO_CONTACT);
    }

    // Unified format and uniqueness validation via the rule engine.
    requireValid(
        evaluateFieldRule(
            request.username(), new UsernameChecker(), accountRepository::existsUserByUsername));
    requireValid(
        evaluateFieldRule(request.password(), new PasswordChecker(), SKIP_UNIQUENESS_CHECK));
    requireValid(
        evaluateFieldRule(
            request.email(), new EmailChecker(), accountRepository::existsUserByEmail));
    requireValid(
        evaluateFieldRule(
            request.phone(), new PhoneChecker(), accountRepository::existsUserByPhone));

    Account account =
        Account.register(
            idGenerator.nextId(),
            Username.of(request.username()),
            request.password(),
            passwordHasher,
            now);
    account.updateContactInfo(request.email(), request.phone(), now);

    accountRepository.register(account);
    return account;
  }

  @Transactional
  public LoginResult login(AccountLoginRequest request, InetAddress clientIpAddress) {
    Instant now = timeProvider.now();
    Account candidate = accountRepository.findByUsername(request.username()).orElse(null);

    // Fail securely with a generic error to prevent user enumeration.
    if (candidate == null) {
      throw LoginFailedException.from(ApiErrorCode.LOGIN_FAILED_INVALID_CREDENTIALS);
    }

    if (!candidate.passwordMatches(request.password(), passwordHasher)) {
      throw LoginFailedException.from(ApiErrorCode.LOGIN_FAILED_INVALID_CREDENTIALS);
    }

    // Translate domain state exceptions into API-friendly login failures.
    try {
      candidate.requireNormalStatus();
    } catch (AccountStateException e) {
      switch (e.getErrorCode()) {
        case ACCOUNT_STATE_ARCHIVED ->
            throw LoginFailedException.from(ApiErrorCode.LOGIN_FAILED_ARCHIVED);
        case ACCOUNT_STATE_SUSPENDED ->
            throw LoginFailedException.from(ApiErrorCode.LOGIN_FAILED_SUSPENDED);
        default -> throw e;
      }
    }

    candidate.recordLoginSuccess(clientIpAddress, now);
    accountRepository.updateAccount(candidate);

    return new LoginResult(
        jwtTokenProvider.generateToken(candidate.getId(), candidate.getRole(), now),
        new TokenResponse(candidate.isMustChangePassword()));
  }

  public void logout(String token) {
    Instant expiration = jwtTokenProvider.getExpirationDate(token).toInstant();
    Duration timeout = Duration.between(timeProvider.now(), expiration);

    if (timeout.isPositive()) {
      String key = "jwt:blacklist:" + token;
      stringRedisTemplate.opsForValue().set(key, "logout", timeout);
    }
  }

  @Transactional
  public void changePassword(Long accountId, ChangePasswordRequest request) {
    Account account =
        accountRepository
            .findAccountById(accountId)
            .orElseThrow(AccountNotFoundException::notFound);

    if (request.currentPassword() == null || request.currentPassword().isBlank()) {
      throw AccountInputException.from(ApiErrorCode.INPUT_PASSWORD_BLANK);
    }

    requireValid(
        evaluateFieldRule(request.newPassword(), new PasswordChecker(), SKIP_UNIQUENESS_CHECK));

    account.changePassword(
        request.currentPassword(), request.newPassword(), passwordHasher, timeProvider.now());
    accountRepository.updateAccount(account);
  }

  @Transactional(readOnly = true)
  public Optional<Account> findAccount(Long accountId) {
    return accountRepository.findAccountById(accountId);
  }

  @Transactional(readOnly = true)
  public ContactAccountListResponse getContacts(Long accountId) {
    return new ContactAccountListResponse(
        accountRepository.findNormalContactsExcluding(accountId).stream()
            .map(ContactAccountView::from)
            .toList());
  }

  @Transactional(readOnly = true)
  public RegisterFieldsCheckResponse checkRegisterFields(RegisterFieldsCheckRequest request) {

    List<String> reasons =
        Stream.of(
                evaluateFieldRule(
                    request.username(),
                    new UsernameChecker(),
                    accountRepository::existsUserByUsername),
                evaluateFieldRule(
                    request.email(), new EmailChecker(), accountRepository::existsUserByEmail),
                evaluateFieldRule(
                    request.phone(), new PhoneChecker(), accountRepository::existsUserByPhone))
            .flatMap(Optional::stream)
            .map(ApiErrorCode::name)
            .toList();

    return new RegisterFieldsCheckResponse(reasons.isEmpty(), reasons);
  }

  @Transactional(readOnly = true)
  public AdminAccountListResponse getManageableAccounts(JwtAuthenticatedUser authenticatedUser) {
    requireAccountManager(authenticatedUser);
    return new AdminAccountListResponse(
        accountRepository.findAllAccounts().stream().map(AdminAccountView::from).toList());
  }

  @Transactional
  public AdminAccountView resetPassword(
      JwtAuthenticatedUser authenticatedUser, Long targetAccountId, ResetPasswordRequest request) {
    requireAccountManager(authenticatedUser);
    requireValid(
        evaluateFieldRule(
            request == null ? null : request.newPassword(),
            new PasswordChecker(),
            SKIP_UNIQUENESS_CHECK));

    Account target = getRequiredAccount(targetAccountId);
    ensureResetPasswordAllowed(authenticatedUser, target);
    target.resetPassword(
        Objects.requireNonNull(request).newPassword(), passwordHasher, timeProvider.now());
    accountRepository.updateAccount(target);
    return AdminAccountView.from(target);
  }

  @Transactional
  public AdminAccountView suspendAccount(
      JwtAuthenticatedUser authenticatedUser, Long targetAccountId) {
    requireAccountManager(authenticatedUser);
    Account target = getRequiredAccount(targetAccountId);
    ensureSuspendAllowed(authenticatedUser, target);
    target.suspend(timeProvider.now());
    accountRepository.updateAccount(target);
    return AdminAccountView.from(target);
  }

  @Transactional
  public AdminAccountView promoteAccount(
      JwtAuthenticatedUser authenticatedUser, Long targetAccountId) {
    requireAccountManager(authenticatedUser);
    Account target = getRequiredAccount(targetAccountId);
    ensurePromoteAllowed(authenticatedUser, target);
    target.assignRole(Role.ADMIN, timeProvider.now());
    accountRepository.updateAccount(target);
    return AdminAccountView.from(target);
  }

  @Transactional
  public AdminAccountView reactivateAccount(
      JwtAuthenticatedUser authenticatedUser, Long targetAccountId) {
    requireAccountManager(authenticatedUser);
    Account target = getRequiredAccount(targetAccountId);
    ensureReactivateAllowed(authenticatedUser, target);
    target.reactivate(timeProvider.now());
    accountRepository.updateAccount(target);
    return AdminAccountView.from(target);
  }

  @Transactional
  public AdminAccountView archiveAccount(
      JwtAuthenticatedUser authenticatedUser, Long targetAccountId) {
    requireAccountManager(authenticatedUser);
    Account target = getRequiredAccount(targetAccountId);
    ensureArchiveAllowed(authenticatedUser, target);
    target.archive(timeProvider.now());
    accountRepository.updateAccount(target);
    return AdminAccountView.from(target);
  }

  @Transactional
  public AdminAccountView demoteAccount(
      JwtAuthenticatedUser authenticatedUser, Long targetAccountId) {
    requireAccountManager(authenticatedUser);
    Account target = getRequiredAccount(targetAccountId);
    ensureDemoteAllowed(authenticatedUser, target);
    target.assignRole(Role.USER, timeProvider.now());
    accountRepository.updateAccount(target);
    return AdminAccountView.from(target);
  }

  /**
   * Evaluates a field against format and uniqueness rules. Uses {@code instanceof} to dynamically
   * probe checker capabilities, keeping the validation flow unified.
   */
  private Optional<ApiErrorCode> evaluateFieldRule(
      String value, FormatChecker formatChecker, Predicate<String> existenceChecker) {
    if (value == null) {
      return Optional.empty();
    }

    if (formatChecker instanceof NotBlankChecker notBlankChecker) {
      if (value.isBlank()) {
        return Optional.of(notBlankChecker.blankErrorCode());
      }
    }

    Optional<ApiErrorCode> formatError = formatChecker.checkFormat(value);
    if (formatError.isPresent()) return formatError;

    if (formatChecker instanceof UniqueChecker uniqueChecker) {
      if (existenceChecker.test(value)) {
        return Optional.of(uniqueChecker.conflictErrorCode());
      }
    }

    return Optional.empty();
  }

  private void requireValid(Optional<ApiErrorCode> result) {
    result.ifPresent(
        error -> {
          switch (error.getCategory()) {
            case INPUT_VALIDATION -> throw AccountInputException.from(error);
            case CONFLICT -> throw AccountConflictException.from(error);
            default ->
                throw new AssertionError(
                    "Validation engine returned unexpected error type: " + error.getCategory());
          }
        });
  }

  private static String nullIfBlank(String value) {
    return (value == null || value.isBlank()) ? null : value;
  }

  private Account getRequiredAccount(Long accountId) {
    return accountRepository
        .findAccountById(accountId)
        .orElseThrow(AccountNotFoundException::notFound);
  }

  private void requireAccountManager(JwtAuthenticatedUser authenticatedUser) {
    if (authenticatedUser == null
        || (authenticatedUser.role() != Role.ADMIN && authenticatedUser.role() != Role.OWNER)) {
      throw AccountForbiddenException.managementRequired();
    }
  }

  private void ensureResetPasswordAllowed(
      JwtAuthenticatedUser authenticatedUser, Account targetAccount) {
    if (!canManage(authenticatedUser, targetAccount)) {
      throw AccountForbiddenException.operationNotAllowed();
    }
  }

  private void ensureSuspendAllowed(JwtAuthenticatedUser authenticatedUser, Account targetAccount) {
    if (!canManage(authenticatedUser, targetAccount)) {
      throw AccountForbiddenException.operationNotAllowed();
    }
    if (targetAccount.getStatus() == Status.ARCHIVED) {
      throw AccountStateException.from(ApiErrorCode.ACCOUNT_STATE_ARCHIVED);
    }
  }

  private void ensurePromoteAllowed(JwtAuthenticatedUser authenticatedUser, Account targetAccount) {
    if (authenticatedUser.role() != Role.OWNER) {
      throw AccountForbiddenException.operationNotAllowed();
    }
    if (authenticatedUser.accountId().equals(targetAccount.getId())
        || targetAccount.getRole() == Role.OWNER) {
      throw AccountForbiddenException.operationNotAllowed();
    }
    if (targetAccount.getRole() != Role.USER) {
      throw new ValidationException(
          ApiErrorCode.INVALID_REQUEST, "Only user accounts can be promoted");
    }
    if (targetAccount.getStatus() == Status.ARCHIVED) {
      throw AccountStateException.from(ApiErrorCode.ACCOUNT_STATE_ARCHIVED);
    }
  }

  private void ensureReactivateAllowed(
      JwtAuthenticatedUser authenticatedUser, Account targetAccount) {
    if (!canManage(authenticatedUser, targetAccount)) {
      throw AccountForbiddenException.operationNotAllowed();
    }
    if (targetAccount.getStatus() == Status.ARCHIVED) {
      throw AccountStateException.from(ApiErrorCode.ACCOUNT_STATE_ARCHIVED);
    }
    if (targetAccount.getStatus() != Status.SUSPENDED) {
      throw new ValidationException(
          ApiErrorCode.INVALID_REQUEST, "Only suspended accounts can be reactivated");
    }
  }

  private void ensureArchiveAllowed(JwtAuthenticatedUser authenticatedUser, Account targetAccount) {
    if (!canManage(authenticatedUser, targetAccount)) {
      throw AccountForbiddenException.operationNotAllowed();
    }
    if (targetAccount.getStatus() == Status.ARCHIVED) {
      throw AccountStateException.from(ApiErrorCode.ACCOUNT_STATE_ARCHIVED);
    }
  }

  private void ensureDemoteAllowed(JwtAuthenticatedUser authenticatedUser, Account targetAccount) {
    if (authenticatedUser.role() != Role.OWNER) {
      throw AccountForbiddenException.operationNotAllowed();
    }
    if (authenticatedUser.accountId().equals(targetAccount.getId())
        || targetAccount.getRole() == Role.OWNER) {
      throw AccountForbiddenException.operationNotAllowed();
    }
    if (targetAccount.getRole() != Role.ADMIN) {
      throw new ValidationException(
          ApiErrorCode.INVALID_REQUEST, "Only admin accounts can be demoted");
    }
    if (targetAccount.getStatus() == Status.ARCHIVED) {
      throw AccountStateException.from(ApiErrorCode.ACCOUNT_STATE_ARCHIVED);
    }
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  private boolean canManage(JwtAuthenticatedUser authenticatedUser, Account targetAccount) {
    if (authenticatedUser.accountId().equals(targetAccount.getId())
        || targetAccount.getRole() == Role.OWNER) {
      return false;
    }
    if (authenticatedUser.role() == Role.OWNER) {
      return targetAccount.getRole() == Role.USER || targetAccount.getRole() == Role.ADMIN;
    }
    return authenticatedUser.role() == Role.ADMIN && targetAccount.getRole() == Role.USER;
  }

  public record LoginResult(String token, TokenResponse response) {}
}
