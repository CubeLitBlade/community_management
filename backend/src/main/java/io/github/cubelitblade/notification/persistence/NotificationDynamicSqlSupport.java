package io.github.cubelitblade.notification.persistence;

import java.sql.JDBCType;
import java.time.Instant;
import org.mybatis.dynamic.sql.AliasableSqlTable;
import org.mybatis.dynamic.sql.SqlColumn;

public final class NotificationDynamicSqlSupport {
  public static final Notifications notifications = new Notifications();
  public static final SqlColumn<Long> id = notifications.id;
  public static final SqlColumn<Long> recipientAccountId = notifications.recipientAccountId;
  public static final SqlColumn<Long> actorAccountId = notifications.actorAccountId;
  public static final SqlColumn<String> type = notifications.type;
  public static final SqlColumn<String> targetType = notifications.targetType;
  public static final SqlColumn<Long> targetId = notifications.targetId;
  public static final SqlColumn<String> content = notifications.content;
  public static final SqlColumn<Boolean> isRead = notifications.isRead;
  public static final SqlColumn<Instant> readAt = notifications.readAt;
  public static final SqlColumn<Instant> createdAt = notifications.createdAt;

  public static final class Notifications extends AliasableSqlTable<Notifications> {
    public final SqlColumn<Long> id = column("id", JDBCType.BIGINT);
    public final SqlColumn<Long> recipientAccountId =
        column("recipient_account_id", JDBCType.BIGINT);
    public final SqlColumn<Long> actorAccountId = column("actor_account_id", JDBCType.BIGINT);
    public final SqlColumn<String> type = column("type", JDBCType.VARCHAR);
    public final SqlColumn<String> targetType = column("target_type", JDBCType.VARCHAR);
    public final SqlColumn<Long> targetId = column("target_id", JDBCType.BIGINT);
    public final SqlColumn<String> content = column("content", JDBCType.VARCHAR);
    public final SqlColumn<Boolean> isRead = column("is_read", JDBCType.BOOLEAN);
    public final SqlColumn<Instant> readAt = column("read_at", JDBCType.TIMESTAMP_WITH_TIMEZONE);
    public final SqlColumn<Instant> createdAt =
        column("created_at", JDBCType.TIMESTAMP_WITH_TIMEZONE);

    Notifications() {
      super("notifications", Notifications::new);
    }
  }
}
