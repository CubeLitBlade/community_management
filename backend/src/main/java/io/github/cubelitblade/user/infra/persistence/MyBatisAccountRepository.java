package io.github.cubelitblade.user.infra.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.cubelitblade.user.domain.model.Account;
import io.github.cubelitblade.user.domain.repository.AccountRepository;
import io.github.cubelitblade.user.infra.persistence.converter.AccountConvertor;
import io.github.cubelitblade.user.infra.persistence.mapper.AccountMapper;
import io.github.cubelitblade.user.infra.persistence.po.AccountPo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MyBatisAccountRepository implements AccountRepository {
    private final AccountMapper accountMapper;

    @Override
    public Account getAccountById(Long id) {
        AccountPo accountPo = accountMapper.selectById(id);
        return AccountConvertor.toDomain(accountPo);
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
