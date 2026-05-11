package io.github.cubelitblade.account.persistence;

import io.github.cubelitblade.account.model.Profile;
import io.github.cubelitblade.common.typehandler.InetAddressTypeHandler;
import io.github.cubelitblade.common.typehandler.JsonbTypeHandler;
import java.sql.JDBCType;
import org.mybatis.dynamic.sql.AliasableSqlTable;
import org.mybatis.dynamic.sql.SqlColumn;

public class AccountDynamicSqlSupport {
  public static final Accounts accounts = new Accounts();
  public static final SqlColumn<Long> id = accounts.id;
  public static final SqlColumn<String> username = accounts.username;
  public static final SqlColumn<String> passwordHash = accounts.passwordHash;
  public static final SqlColumn<Boolean> mustChangePassword = accounts.mustChangePassword;
  public static final SqlColumn<String> nickname = accounts.nickname;
  public static final SqlColumn<String> email = accounts.email;
  public static final SqlColumn<String> phone = accounts.phone;
  public static final SqlColumn<Profile> profile = accounts.profile;
  public static final SqlColumn<String> role = accounts.role;
  public static final SqlColumn<String> status = accounts.status;
  public static final SqlColumn<java.time.Instant> createdAt = accounts.createdAt;
  public static final SqlColumn<java.time.Instant> updatedAt = accounts.updatedAt;
  public static final SqlColumn<java.time.Instant> lastLoginAt = accounts.lastLoginAt;
  public static final SqlColumn<java.net.InetAddress> lastLoginIp = accounts.lastLoginIp;

  public static final class Accounts extends AliasableSqlTable<Accounts> {
    public final SqlColumn<Long> id = column("id", JDBCType.BIGINT);
    public final SqlColumn<String> username = column("username", JDBCType.VARCHAR);
    public final SqlColumn<String> passwordHash = column("password_hash", JDBCType.VARCHAR);
    public final SqlColumn<Boolean> mustChangePassword =
        column("must_change_password", JDBCType.BOOLEAN);
    public final SqlColumn<String> nickname = column("nickname", JDBCType.VARCHAR);
    public final SqlColumn<String> email = column("email", JDBCType.VARCHAR);
    public final SqlColumn<String> phone = column("phone", JDBCType.VARCHAR);
    public final SqlColumn<Profile> profile =
        column("profile", JDBCType.OTHER)
            .withTypeHandler(JsonbTypeHandler.class.getName())
            .withJavaType(Profile.class);
    public final SqlColumn<String> role = column("role", JDBCType.VARCHAR);
    public final SqlColumn<String> status = column("status", JDBCType.VARCHAR);
    public final SqlColumn<java.time.Instant> createdAt =
        column("created_at", JDBCType.TIMESTAMP_WITH_TIMEZONE);
    public final SqlColumn<java.time.Instant> updatedAt =
        column("updated_at", JDBCType.TIMESTAMP_WITH_TIMEZONE);
    public final SqlColumn<java.time.Instant> lastLoginAt =
        column("last_login_at", JDBCType.TIMESTAMP_WITH_TIMEZONE);
    public final SqlColumn<java.net.InetAddress> lastLoginIp =
        column("last_login_ip", JDBCType.OTHER)
            .withTypeHandler(InetAddressTypeHandler.class.getName())
            .withJavaType(java.net.InetAddress.class);

    Accounts() {
      super("accounts", Accounts::new);
    }
  }
}
