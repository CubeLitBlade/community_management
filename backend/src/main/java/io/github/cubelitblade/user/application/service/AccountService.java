package io.github.cubelitblade.user.application.service;

import io.github.cubelitblade.configuration.TimeConfig;
import io.github.cubelitblade.user.application.dto.AccountLoginRequest;
import io.github.cubelitblade.user.application.dto.AccountRegisterRequest;
import io.github.cubelitblade.user.application.dto.TokenResponse;
import io.github.cubelitblade.user.domain.exception.InvalidCredentialsException;
import io.github.cubelitblade.user.domain.exception.UsernameAlreadyExistsException;
import io.github.cubelitblade.user.domain.model.Account;
import io.github.cubelitblade.user.domain.model.Username;
import io.github.cubelitblade.user.domain.repository.AccountRepository;
import io.github.cubelitblade.user.domain.service.PasswordService;
import io.github.cubelitblade.user.infra.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final PasswordService passwordService;
    private final TimeConfig timeConfig;
    private final JwtTokenProvider jwtTokenProvider;

    public Account register(AccountRegisterRequest request) {
        if (accountRepository.existsUserByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }

        Account account = Account.register(
                Username.of(request.username()),
                request.password(),
                passwordService,
                timeConfig.now()
        );

        accountRepository.register(account);
        return account;
    }

    public TokenResponse login(AccountLoginRequest request, InetAddress clientIpAddress) {
        Instant now = timeConfig.now();
        Account candidate = accountRepository.findByUsername(request.username());

        if (candidate == null) {
            throw new InvalidCredentialsException();
        }

        if (!candidate.passwordMatches(request.password(), passwordService)) {
            throw new InvalidCredentialsException();
        }

        candidate.requireNormalStatus();

        candidate.recordLoginSuccess(clientIpAddress, now);
        accountRepository.updateAccount(candidate);

        return new TokenResponse(
                jwtTokenProvider.generateToken(candidate.getId(), candidate.getRole(), now)
        );
    }

    @Transactional(readOnly = true)
    public Optional<Account> findAccount(Long accountId) {
        return accountRepository.findAccountById(accountId);
    }
}
