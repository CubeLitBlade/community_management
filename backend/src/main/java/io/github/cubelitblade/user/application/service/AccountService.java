package io.github.cubelitblade.user.application.service;

import io.github.cubelitblade.configuration.TimeConfig;
import io.github.cubelitblade.user.application.dto.AccountRegisterRequest;
import io.github.cubelitblade.user.domain.exception.UsernameAlreadyExistsException;
import io.github.cubelitblade.user.domain.model.Account;
import io.github.cubelitblade.user.domain.model.Username;
import io.github.cubelitblade.user.domain.repository.AccountRepository;
import io.github.cubelitblade.user.domain.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final PasswordService passwordService;
    private final TimeConfig timeConfig;

    public Account register(AccountRegisterRequest request) {
        if (accountRepository.existsUserByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }

        Account account = Account.register(
                Username.of(request.username()),
                request.rawPassword(),
                passwordService,
                timeConfig.now()
        );

        accountRepository.register(account);
        return account;
    }
}
