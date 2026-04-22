package io.github.cubelitblade.account.persistence;

import static org.mybatis.dynamic.sql.SqlBuilder.insert;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.isIn;
import static org.mybatis.dynamic.sql.SqlBuilder.isNotEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.select;
import static org.mybatis.dynamic.sql.SqlBuilder.update;

import io.github.cubelitblade.account.model.Account;
import io.github.cubelitblade.account.model.Status;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class AccountRepository {
  private static final BasicColumn[] SELECT_COLUMNS = {
    AccountDynamicSqlSupport.id,
    AccountDynamicSqlSupport.username,
    AccountDynamicSqlSupport.passwordHash,
    AccountDynamicSqlSupport.mustChangePassword,
    AccountDynamicSqlSupport.nickname,
    AccountDynamicSqlSupport.email,
    AccountDynamicSqlSupport.phone,
    AccountDynamicSqlSupport.profile,
    AccountDynamicSqlSupport.role,
    AccountDynamicSqlSupport.status,
    AccountDynamicSqlSupport.createdAt,
    AccountDynamicSqlSupport.updatedAt,
    AccountDynamicSqlSupport.lastLoginAt,
    AccountDynamicSqlSupport.lastLoginIp
  };

  private final AccountMapper accountMapper;

  @Transactional(readOnly = true)
  public Optional<Account> findAccountById(Long id) {
    return selectOneBy(AccountDynamicSqlSupport.id, id).map(AccountPo::toDomain);
  }

  @Transactional(readOnly = true)
  public List<Account> findAllAccounts() {
    SelectStatementProvider selectStatement =
        select(
                AccountDynamicSqlSupport.id,
                AccountDynamicSqlSupport.username,
                AccountDynamicSqlSupport.passwordHash,
                AccountDynamicSqlSupport.mustChangePassword,
                AccountDynamicSqlSupport.nickname,
                AccountDynamicSqlSupport.email,
                AccountDynamicSqlSupport.phone,
                AccountDynamicSqlSupport.profile,
                AccountDynamicSqlSupport.role,
                AccountDynamicSqlSupport.status,
                AccountDynamicSqlSupport.createdAt,
                AccountDynamicSqlSupport.updatedAt,
                AccountDynamicSqlSupport.lastLoginAt,
                AccountDynamicSqlSupport.lastLoginIp)
            .from(AccountDynamicSqlSupport.accounts)
            .orderBy(AccountDynamicSqlSupport.id)
            .build()
            .render(RenderingStrategies.MYBATIS3);

    return accountMapper.selectMany(selectStatement).stream().map(AccountPo::toDomain).toList();
  }

  @Transactional(readOnly = true)
  public List<Account> findNormalContactsExcluding(Long accountId) {
    SelectStatementProvider selectStatement =
        select(SELECT_COLUMNS)
            .from(AccountDynamicSqlSupport.accounts)
            .where(AccountDynamicSqlSupport.status, isEqualTo(Status.NORMAL.getValue()))
            .and(AccountDynamicSqlSupport.id, isNotEqualTo(accountId))
            .orderBy(AccountDynamicSqlSupport.username)
            .build()
            .render(RenderingStrategies.MYBATIS3);

    return accountMapper.selectMany(selectStatement).stream().map(AccountPo::toDomain).toList();
  }

  @Transactional(readOnly = true)
  public List<Account> findAccountsByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }

    SelectStatementProvider selectStatement =
        select(SELECT_COLUMNS)
            .from(AccountDynamicSqlSupport.accounts)
            .where(AccountDynamicSqlSupport.id, isIn(ids))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    return accountMapper.selectMany(selectStatement).stream().map(AccountPo::toDomain).toList();
  }

  @Transactional
  public void register(Account account) {
    AccountPo accountPo = AccountPo.fromDomain(account);
    InsertStatementProvider<AccountPo> insertStatement =
        insert(accountPo)
            .into(AccountDynamicSqlSupport.accounts)
            .map(AccountDynamicSqlSupport.id)
            .toProperty("id")
            .map(AccountDynamicSqlSupport.username)
            .toProperty("username")
            .map(AccountDynamicSqlSupport.passwordHash)
            .toProperty("passwordHash")
            .map(AccountDynamicSqlSupport.mustChangePassword)
            .toProperty("mustChangePassword")
            .map(AccountDynamicSqlSupport.nickname)
            .toProperty("nickname")
            .map(AccountDynamicSqlSupport.email)
            .toProperty("email")
            .map(AccountDynamicSqlSupport.phone)
            .toProperty("phone")
            .map(AccountDynamicSqlSupport.profile)
            .toProperty("profile")
            .map(AccountDynamicSqlSupport.role)
            .toProperty("role")
            .map(AccountDynamicSqlSupport.status)
            .toProperty("status")
            .map(AccountDynamicSqlSupport.createdAt)
            .toProperty("createdAt")
            .map(AccountDynamicSqlSupport.updatedAt)
            .toProperty("updatedAt")
            .map(AccountDynamicSqlSupport.lastLoginAt)
            .toProperty("lastLoginAt")
            .map(AccountDynamicSqlSupport.lastLoginIp)
            .toProperty("lastLoginIp")
            .build()
            .render(RenderingStrategies.MYBATIS3);

    accountMapper.insert(insertStatement);
  }

  @Transactional(readOnly = true)
  public boolean existsUserByUsername(String username) {
    return selectOneBy(AccountDynamicSqlSupport.username, username).isPresent();
  }

  @Transactional(readOnly = true)
  public boolean existsUserByEmail(String email) {
    return selectOneBy(AccountDynamicSqlSupport.email, email).isPresent();
  }

  @Transactional(readOnly = true)
  public boolean existsUserByPhone(String phone) {
    return selectOneBy(AccountDynamicSqlSupport.phone, phone).isPresent();
  }

  @Transactional(readOnly = true)
  public Optional<Account> findByUsername(String username) {
    return selectOneBy(AccountDynamicSqlSupport.username, username).map(AccountPo::toDomain);
  }

  @Transactional
  public void updateAccount(Account account) {
    AccountPo accountPo = AccountPo.fromDomain(account);
    UpdateStatementProvider updateStatement =
        update(AccountDynamicSqlSupport.accounts)
            .set(AccountDynamicSqlSupport.username)
            .equalTo(accountPo::username)
            .set(AccountDynamicSqlSupport.passwordHash)
            .equalTo(accountPo::passwordHash)
            .set(AccountDynamicSqlSupport.mustChangePassword)
            .equalTo(accountPo::mustChangePassword)
            .set(AccountDynamicSqlSupport.nickname)
            .equalTo(accountPo::nickname)
            .set(AccountDynamicSqlSupport.email)
            .equalTo(accountPo::email)
            .set(AccountDynamicSqlSupport.phone)
            .equalTo(accountPo::phone)
            .set(AccountDynamicSqlSupport.profile)
            .equalTo(accountPo::profile)
            .set(AccountDynamicSqlSupport.role)
            .equalTo(accountPo::role)
            .set(AccountDynamicSqlSupport.status)
            .equalTo(accountPo::status)
            .set(AccountDynamicSqlSupport.createdAt)
            .equalTo(accountPo::createdAt)
            .set(AccountDynamicSqlSupport.updatedAt)
            .equalTo(accountPo::updatedAt)
            .set(AccountDynamicSqlSupport.lastLoginAt)
            .equalTo(accountPo::lastLoginAt)
            .set(AccountDynamicSqlSupport.lastLoginIp)
            .equalTo(accountPo::lastLoginIp)
            .where(AccountDynamicSqlSupport.id, isEqualTo(account.getId()))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    accountMapper.update(updateStatement);
  }

  private <T> SelectStatementProvider selectAllColumnsWhere(
      org.mybatis.dynamic.sql.SqlColumn<T> column, T value) {
    return select(SELECT_COLUMNS)
        .from(AccountDynamicSqlSupport.accounts)
        .where(column, isEqualTo(value))
        .build()
        .render(RenderingStrategies.MYBATIS3);
  }

  private <T> Optional<AccountPo> selectOneBy(
      org.mybatis.dynamic.sql.SqlColumn<T> column, T value) {
    return accountMapper.selectOne(selectAllColumnsWhere(column, value));
  }
}
