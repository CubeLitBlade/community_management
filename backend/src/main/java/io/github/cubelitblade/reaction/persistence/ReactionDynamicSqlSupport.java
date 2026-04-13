package io.github.cubelitblade.reaction.persistence;

import java.sql.JDBCType;
import java.time.Instant;
import org.mybatis.dynamic.sql.AliasableSqlTable;
import org.mybatis.dynamic.sql.SqlColumn;

public class ReactionDynamicSqlSupport {
  public static final Reactions reactions = new Reactions();
  public static final SqlColumn<Long> id = reactions.id;
  public static final SqlColumn<Long> accountId = reactions.accountId;
  public static final SqlColumn<String> targetType = reactions.targetType;
  public static final SqlColumn<Long> targetId = reactions.targetId;
  public static final SqlColumn<String> reactionType = reactions.reactionType;
  public static final SqlColumn<Instant> createdAt = reactions.createdAt;
  public static final SqlColumn<Instant> updatedAt = reactions.updatedAt;

  public static final class Reactions extends AliasableSqlTable<Reactions> {
    public final SqlColumn<Long> id = column("id", JDBCType.BIGINT).withJavaProperty("id");
    public final SqlColumn<Long> accountId =
        column("account_id", JDBCType.BIGINT).withJavaProperty("accountId");
    public final SqlColumn<String> targetType =
        column("target_type", JDBCType.VARCHAR).withJavaProperty("targetType");
    public final SqlColumn<Long> targetId =
        column("target_id", JDBCType.BIGINT).withJavaProperty("targetId");
    public final SqlColumn<String> reactionType =
        column("reaction_type", JDBCType.VARCHAR).withJavaProperty("reactionType");
    public final SqlColumn<Instant> createdAt =
        column("created_at", JDBCType.TIMESTAMP_WITH_TIMEZONE).withJavaProperty("createdAt");
    public final SqlColumn<Instant> updatedAt =
        column("updated_at", JDBCType.TIMESTAMP_WITH_TIMEZONE).withJavaProperty("updatedAt");

    Reactions() {
      super("reactions", Reactions::new);
    }
  }
}
