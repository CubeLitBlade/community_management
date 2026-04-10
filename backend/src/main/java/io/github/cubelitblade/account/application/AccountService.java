package io.github.cubelitblade.account.application;

import static io.github.cubelitblade.account.common.AccountErrorCode.ACCOUNT_STATE_ARCHIVED;
import static io.github.cubelitblade.account.common.AccountErrorCode.ACCOUNT_STATE_SUSPENDED;

import io.github.cubelitblade.account.application.validation.*;
import io.github.cubelitblade.account.common.AccountErrorCode;
import io.github.cubelitblade.account.dto.AccountLoginRequest;
import io.github.cubelitblade.account.dto.AccountRegisterRequest;
import io.github.cubelitblade.account.dto.RegisterFieldsCheckRequest;
import io.github.cubelitblade.account.dto.RegisterFieldsCheckResponse;
import io.github.cubelitblade.account.dto.TokenResponse;
import io.github.cubelitblade.account.exception.AccountStateException;
import io.github.cubelitblade.account.exception.ConflictFieldsException;
import io.github.cubelitblade.account.exception.InputValidationException;
import io.github.cubelitblade.account.exception.LoginFailedException;
import io.github.cubelitblade.account.model.Account;
import io.github.cubelitblade.account.model.Username;
import io.github.cubelitblade.account.persistence.AccountRepository;
import io.github.cubelitblade.account.security.JwtTokenProvider;
import io.github.cubelitblade.common.time.TimeProvider;
import java.net.InetAddress;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {
  private final AccountRepository accountRepository;
  private final PasswordHasher passwordHasher;
  private final TimeProvider timeProvider;
  private final JwtTokenProvider jwtTokenProvider;

  private static final Predicate<String> SKIP_UNIQUENESS_CHECK = _ -> false;

  @Transactional
  public Account register(AccountRegisterRequest request) {
    Instant now = timeProvider.now();

    // Fast-fail for required fields.
    // The engine skips nulls, so mandatory blanks must be caught early.
    if (request.username() == null || request.username().isBlank()) {
      throw new InputValidationException(AccountErrorCode.INPUT_USERNAME_BLANK);
    }
    if (request.password() == null || request.password().isBlank()) {
      throw new InputValidationException(AccountErrorCode.INPUT_PASSWORD_BLANK);
    }
    if (nullIfBlank(request.email()) == null && nullIfBlank(request.phone()) == null) {
      throw new InputValidationException(AccountErrorCode.INPUT_NO_CONTACT);
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
        Account.register(Username.of(request.username()), request.password(), passwordHasher, now);
    account.updateContactInfo(request.email(), request.phone(), now);

    accountRepository.register(account);
    return account;
  }

  @Transactional
  public TokenResponse login(AccountLoginRequest request, InetAddress clientIpAddress) {
    Instant now = timeProvider.now();
    Account candidate = accountRepository.findByUsername(request.username());

    // Fail securely with a generic error to prevent user enumeration.
    if (candidate == null) {
      throw new LoginFailedException(AccountErrorCode.LOGIN_FAILED_INVALID_CREDENTIALS);
    }

    if (!candidate.passwordMatches(request.password(), passwordHasher)) {
      throw new LoginFailedException(AccountErrorCode.LOGIN_FAILED_INVALID_CREDENTIALS);
    }

    // Translate domain state exceptions into API-friendly login failures.
    try {
      candidate.requireNormalStatus();
    } catch (AccountStateException e) {
      switch (e.getErrorCode()) {
        case ACCOUNT_STATE_ARCHIVED ->
            throw new LoginFailedException(AccountErrorCode.LOGIN_FAILED_ARCHIVED);
        case ACCOUNT_STATE_SUSPENDED ->
            throw new LoginFailedException(AccountErrorCode.LOGIN_FAILED_SUSPENDED);
        default -> throw e;
      }
    }

    candidate.recordLoginSuccess(clientIpAddress, now);
    accountRepository.updateAccount(candidate);

    return new TokenResponse(
        jwtTokenProvider.generateToken(candidate.getId(), candidate.getRole(), now));
  }

  @Transactional(readOnly = true)
  public Optional<Account> findAccount(Long accountId) {
    return accountRepository.findAccountById(accountId);
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
            .map(AccountErrorCode::name)
            .toList();

    return new RegisterFieldsCheckResponse(reasons.isEmpty(), reasons);
  }

  /**
   * Evaluates a field against format and uniqueness rules. Uses {@code instanceof} to dynamically
   * probe checker capabilities, keeping the validation flow unified.
   */
  private Optional<AccountErrorCode> evaluateFieldRule(
      String value, FormatChecker formatChecker, Predicate<String> existenceChecker) {
    if (value == null) {
      return Optional.empty();
    }

    if (formatChecker instanceof NotBlankChecker notBlankChecker) {
      if (value.isBlank()) {
        return Optional.of(notBlankChecker.blankErrorCode());
      }
    }

    Optional<AccountErrorCode> formatError = formatChecker.checkFormat(value);
    if (formatError.isPresent()) return formatError;

    if (formatChecker instanceof UniqueChecker uniqueChecker) {
      if (existenceChecker.test(value)) {
        return Optional.of(uniqueChecker.conflictErrorCode());
      }
    }

    return Optional.empty();
  }

  private void requireValid(Optional<AccountErrorCode> result) {
    result.ifPresent(
        error -> {
          switch (error.getCategory()) {
            case INPUT_VALIDATION -> throw new InputValidationException(error);
            case CONFLICT -> throw new ConflictFieldsException(error);
            default ->
                throw new AssertionError(
                    "Validation engine returned unexpected error type: " + error.getCategory());
          }
        });
  }

  private static String nullIfBlank(String value) {
    return (value == null || value.isBlank()) ? null : value;
  }
}
