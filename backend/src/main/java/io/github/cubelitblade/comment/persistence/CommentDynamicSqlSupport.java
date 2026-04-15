package io.github.cubelitblade.comment.persistence;

import java.sql.JDBCType;
import java.time.Instant;
import org.mybatis.dynamic.sql.AliasableSqlTable;
import org.mybatis.dynamic.sql.SqlColumn;

public final class CommentDynamicSqlSupport {
  public static final Comments comments = new Comments();
  public static final SqlColumn<Long> id = comments.id;
  public static final SqlColumn<String> targetType = comments.targetType;
  public static final SqlColumn<Long> targetId = comments.targetId;
  public static final SqlColumn<Long> accountId = comments.accountId;
  public static final SqlColumn<Long> parentId = comments.parentId;
  public static final SqlColumn<String> content = comments.content;
  public static final SqlColumn<String> status = comments.status;
  public static final SqlColumn<Instant> createdAt = comments.createdAt;
  public static final SqlColumn<Instant> updatedAt = comments.updatedAt;

  public static final class Comments extends AliasableSqlTable<Comments> {
    public final SqlColumn<Long> id = column("id", JDBCType.BIGINT);
    public final SqlColumn<String> targetType = column("target_type", JDBCType.VARCHAR);
    public final SqlColumn<Long> targetId = column("target_id", JDBCType.BIGINT);
    public final SqlColumn<Long> accountId = column("account_id", JDBCType.BIGINT);
    public final SqlColumn<Long> parentId = column("parent_id", JDBCType.BIGINT);
    public final SqlColumn<String> content = column("content", JDBCType.VARCHAR);
    public final SqlColumn<String> status = column("status", JDBCType.VARCHAR);
    public final SqlColumn<Instant> createdAt =
        column("created_at", JDBCType.TIMESTAMP_WITH_TIMEZONE);
    public final SqlColumn<Instant> updatedAt =
        column("updated_at", JDBCType.TIMESTAMP_WITH_TIMEZONE);

    Comments() {
      super("comments", Comments::new);
    }
  }
}
