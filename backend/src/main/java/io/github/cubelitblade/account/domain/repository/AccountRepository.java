package io.github.cubelitblade.account.domain.repository;

import io.github.cubelitblade.account.domain.model.Account;

import java.util.Optional;

public interface AccountRepository {
    Optional<Account> findAccountById(Long id);

    Account register(Account account);

    boolean existsUserByUsername(String username);

    // Account existsUserByEmail(String email);
    Account findByUsername(String username);

    void updateAccount(Account account);
}
