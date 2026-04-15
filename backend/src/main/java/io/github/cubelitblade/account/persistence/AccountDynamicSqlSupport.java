package io.github.cubelitblade.account.persistence;

import java.sql.JDBCType;
import org.mybatis.dynamic.sql.AliasableSqlTable;
import org.mybatis.dynamic.sql.SqlColumn;

public class AccountDynamicSqlSupport {
  public static final Accounts accounts = new Accounts();
  public static final SqlColumn<Long> id = accounts.id;
  public static final SqlColumn<String> username = accounts.username;
  public static final SqlColumn<String> nickname = accounts.nickname;

  public static final class Accounts extends AliasableSqlTable<Accounts> {
    public final SqlColumn<Long> id = column("id", JDBCType.BIGINT);
    public final SqlColumn<String> username = column("username", JDBCType.VARCHAR);
    public final SqlColumn<String> nickname = column("nickname", JDBCType.VARCHAR);

    Accounts() {
      super("accounts", Accounts::new);
    }
  }
}
