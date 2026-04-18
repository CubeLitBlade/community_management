package io.github.cubelitblade.activity.persistence;

import java.sql.JDBCType;
import java.time.Instant;
import org.mybatis.dynamic.sql.AliasableSqlTable;
import org.mybatis.dynamic.sql.SqlColumn;

public final class ActivityRegistrationDynamicSqlSupport {
  public static final ActivityRegistrations activityRegistrations = new ActivityRegistrations();
  public static final SqlColumn<Long> activityId = activityRegistrations.activityId;
  public static final SqlColumn<Long> accountId = activityRegistrations.accountId;
  public static final SqlColumn<Instant> createdAt = activityRegistrations.createdAt;

  public static final class ActivityRegistrations
      extends AliasableSqlTable<ActivityRegistrations> {
    public final SqlColumn<Long> activityId = column("activity_id", JDBCType.BIGINT);
    public final SqlColumn<Long> accountId = column("account_id", JDBCType.BIGINT);
    public final SqlColumn<Instant> createdAt =
        column("created_at", JDBCType.TIMESTAMP_WITH_TIMEZONE);

    ActivityRegistrations() {
      super("activity_registrations", ActivityRegistrations::new);
    }
  }
}
