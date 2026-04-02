package io.github.cubelitblade.account.infra.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.cubelitblade.account.domain.model.Account;
import io.github.cubelitblade.account.domain.repository.AccountRepository;
import io.github.cubelitblade.account.infra.persistence.converter.AccountConvertor;
import io.github.cubelitblade.account.infra.persistence.mapper.AccountMapper;
import io.github.cubelitblade.account.infra.persistence.po.AccountPo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MyBatisAccountRepository implements AccountRepository {
    private final AccountMapper accountMapper;

    @Override
    public Optional<Account> findAccountById(Long id) {
        AccountPo accountPo = accountMapper.selectById(id);
        return Optional.ofNullable(AccountConvertor.toDomain(accountPo));
    }

    @Override
    public Account register(Account account) {
        AccountPo accountPo = AccountConvertor.toPo(account);

        accountMapper.insert(accountPo);
        account.initIdentifier(accountPo.getId());

        return account;
    }

    @Override
    public boolean existsUserByUsername(String username) {
        LambdaQueryWrapper<AccountPo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(AccountPo::getUsername, username);

        AccountPo accountPo = accountMapper.selectOne(queryWrapper);
        return accountPo != null;
    }

    @Override
    public Account findByUsername(String username) {
        LambdaQueryWrapper<AccountPo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(AccountPo::getUsername, username);

        AccountPo accountPo = accountMapper.selectOne(queryWrapper);
        return AccountConvertor.toDomain(accountPo);
    }

    @Override
    public void updateAccount(Account account) {
        accountMapper.updateById(AccountConvertor.toPo(account));
    }
}
