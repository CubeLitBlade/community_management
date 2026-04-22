package io.github.cubelitblade.message.persistence;

import java.sql.JDBCType;
import java.time.Instant;
import org.mybatis.dynamic.sql.AliasableSqlTable;
import org.mybatis.dynamic.sql.SqlColumn;

public final class PrivateMessageDynamicSqlSupport {
  public static final PrivateMessages privateMessages = new PrivateMessages();
  public static final SqlColumn<Long> id = privateMessages.id;
  public static final SqlColumn<Long> senderAccountId = privateMessages.senderAccountId;
  public static final SqlColumn<Long> recipientAccountId = privateMessages.recipientAccountId;
  public static final SqlColumn<String> content = privateMessages.content;
  public static final SqlColumn<Boolean> isRead = privateMessages.isRead;
  public static final SqlColumn<Instant> readAt = privateMessages.readAt;
  public static final SqlColumn<Instant> createdAt = privateMessages.createdAt;

  public static final class PrivateMessages extends AliasableSqlTable<PrivateMessages> {
    public final SqlColumn<Long> id = column("id", JDBCType.BIGINT);
    public final SqlColumn<Long> senderAccountId = column("sender_account_id", JDBCType.BIGINT);
    public final SqlColumn<Long> recipientAccountId =
        column("recipient_account_id", JDBCType.BIGINT);
    public final SqlColumn<String> content = column("content", JDBCType.VARCHAR);
    public final SqlColumn<Boolean> isRead = column("is_read", JDBCType.BOOLEAN);
    public final SqlColumn<Instant> readAt = column("read_at", JDBCType.TIMESTAMP_WITH_TIMEZONE);
    public final SqlColumn<Instant> createdAt =
        column("created_at", JDBCType.TIMESTAMP_WITH_TIMEZONE);

    PrivateMessages() {
      super("private_messages", PrivateMessages::new);
    }
  }
}
