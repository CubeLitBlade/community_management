package io.github.cubelitblade.account.application;

import io.github.cubelitblade.account.common.AccountError;
import io.github.cubelitblade.account.dto.AccountLoginRequest;
import io.github.cubelitblade.account.dto.AccountRegisterFieldsCheckRequest;
import io.github.cubelitblade.account.dto.AccountRegisterFieldsCheckResponse;
import io.github.cubelitblade.account.dto.AccountRegisterRequest;
import io.github.cubelitblade.account.dto.TokenResponse;
import io.github.cubelitblade.account.exception.AccountStateException;
import io.github.cubelitblade.account.exception.ConflictFieldsException;
import io.github.cubelitblade.account.exception.InputValidationException;
import io.github.cubelitblade.account.exception.LoginFailedException;
import io.github.cubelitblade.account.model.*;
import io.github.cubelitblade.account.persistence.AccountRepository;
import io.github.cubelitblade.account.security.JwtTokenProvider;
import io.github.cubelitblade.configuration.TimeConfig;
import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {
  private final AccountRepository accountRepository;
  private final PasswordHasher passwordHasher;
  private final TimeConfig timeConfig;
  private final JwtTokenProvider jwtTokenProvider;

  @Transactional
  public Account register(AccountRegisterRequest request) {
    Username.check(request.username())
        .ifPresent(
            error -> {
              throw new InputValidationException(error);
            });

    Password.check(request.password())
        .ifPresent(
            error -> {
              throw new InputValidationException(error);
            });

    if (!hasValue(request.email()) && !hasValue(request.phone())) {
      throw new InputValidationException(AccountError.INPUT_WITHOUT_CONTACT);
    }

    if (hasValue(request.email())) {
      Email.check(request.email())
          .ifPresent(
              error -> {
                throw new InputValidationException(error);
              });
    }

    if (hasValue(request.phone())) {
      Phone.check(request.phone())
          .ifPresent(
              error -> {
                throw new InputValidationException(error);
              });
    }

    if (accountRepository.existsUserByUsername(request.username())) {
      throw new ConflictFieldsException(request.username(), AccountError.CONFLICT_USERNAME_EXISTS);
    }

    if (accountRepository.existsUserByEmail(request.email())) {
      throw new ConflictFieldsException(request.email(), AccountError.CONFLICT_EMAIL_EXISTS);
    }

    if (accountRepository.existsUserByPhone(request.phone())) {
      throw new ConflictFieldsException(request.phone(), AccountError.CONFLICT_PHONE_EXISTS);
    }

    Account account =
        Account.register(
            Username.of(request.username()), request.password(), passwordHasher, timeConfig.now());

    accountRepository.register(account);
    return account;
  }

  @Transactional
  public TokenResponse login(AccountLoginRequest request, InetAddress clientIpAddress) {
    Instant now = timeConfig.now();
    Account candidate = accountRepository.findByUsername(request.username());

    if (candidate == null) {
      throw new LoginFailedException(AccountError.LOGIN_FAILED_INVALID_CREDENTIALS);
    }

    if (!candidate.passwordMatches(request.password(), passwordHasher)) {
      throw new LoginFailedException(AccountError.LOGIN_FAILED_INVALID_CREDENTIALS);
    }

    try {
      candidate.requireNormalStatus();
    } catch (AccountStateException e) {
      switch (e.getError()) {
        case ACCOUNT_STATE_ARCHIVED ->
            throw new LoginFailedException(AccountError.LOGIN_FAILED_ARCHIVED);
        case ACCOUNT_STATE_SUSPENDED ->
            throw new LoginFailedException(AccountError.LOGIN_FAILED_SUSPENDED);
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
  public AccountRegisterFieldsCheckResponse checkRegisterFields(
      AccountRegisterFieldsCheckRequest request) {
    AtomicBoolean isAvailable = new AtomicBoolean(true);
    List<String> reasons = new ArrayList<>();

    if (request.username() != null && !request.username().isBlank()) {
      Username.check(request.username())
          .ifPresent(
              error -> {
                isAvailable.set(false);
                reasons.add(error.getCode());
              });

      if (accountRepository.existsUserByUsername(request.username())) {
        isAvailable.set(false);
        reasons.add(AccountError.CONFLICT_USERNAME_EXISTS.getCode());
      }
    }

    if (hasValue(request.email())) {
      Email.check(request.email())
          .ifPresent(
              error -> {
                isAvailable.set(false);
                reasons.add(error.getCode());
              });
    }

    if (hasValue(request.phone())) {
      Phone.check(request.phone())
          .ifPresent(
              error -> {
                isAvailable.set(false);
                reasons.add(error.getCode());
              });
    }

    return new AccountRegisterFieldsCheckResponse(isAvailable.get(), reasons);
  }

  private static boolean hasValue(String value) {
    return value != null && !value.isBlank();
  }
}
