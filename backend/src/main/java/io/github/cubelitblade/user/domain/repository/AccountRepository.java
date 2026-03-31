package io.github.cubelitblade.user.domain.repository;

import io.github.cubelitblade.user.domain.model.Account;

public interface AccountRepository {
    Account getAccountById(Long id);
    Account register(Account account);
    boolean existsUserByUsername(String username);
    // Account existsUserByEmail(String email);
}
