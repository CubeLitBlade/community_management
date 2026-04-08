package io.github.cubelitblade.account.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.cubelitblade.account.model.Account;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AccountRepository {
  private final AccountMapper accountMapper;

  public Optional<Account> findAccountById(Long id) {
    AccountPo accountPo = accountMapper.selectById(id);
    return Optional.ofNullable(accountPo).map(AccountPo::toDomain);
  }

  public Account register(Account account) {
    AccountPo accountPo = AccountPo.fromDomain(account);

    accountMapper.insert(accountPo);
    account.initIdentifier(accountPo.getId());

    return account;
  }

  public boolean existsUserByUsername(String username) {
    LambdaQueryWrapper<AccountPo> queryWrapper = Wrappers.lambdaQuery();
    queryWrapper.eq(AccountPo::getUsername, username);

    AccountPo accountPo = accountMapper.selectOne(queryWrapper);
    return accountPo != null;
  }

  public boolean existsUserByEmail(String email) {
    LambdaQueryWrapper<AccountPo> queryWrapper = Wrappers.lambdaQuery();
    queryWrapper.eq(AccountPo::getEmail, email);

    AccountPo accountPo = accountMapper.selectOne(queryWrapper);
    return accountPo != null;
  }

  public boolean existsUserByPhone(String phone) {
    LambdaQueryWrapper<AccountPo> queryWrapper = Wrappers.lambdaQuery();
    queryWrapper.eq(AccountPo::getPhone, phone);

    AccountPo accountPo = accountMapper.selectOne(queryWrapper);
    return accountPo != null;
  }

  public Account findByUsername(String username) {
    LambdaQueryWrapper<AccountPo> queryWrapper = Wrappers.lambdaQuery();
    queryWrapper.eq(AccountPo::getUsername, username);

    AccountPo accountPo = accountMapper.selectOne(queryWrapper);
    return accountPo == null ? null : accountPo.toDomain();
  }

  public void updateAccount(Account account) {
    accountMapper.updateById(AccountPo.fromDomain(account));
  }
}
