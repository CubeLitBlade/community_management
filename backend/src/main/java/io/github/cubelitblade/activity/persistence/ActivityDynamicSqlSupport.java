package io.github.cubelitblade.activity.persistence;

import java.sql.JDBCType;
import java.time.Instant;
import org.mybatis.dynamic.sql.AliasableSqlTable;
import org.mybatis.dynamic.sql.SqlColumn;

public final class ActivityDynamicSqlSupport {
  public static final Activities activities = new Activities();
  public static final SqlColumn<Long> id = activities.id;
  public static final SqlColumn<Long> creatorAccountId = activities.creatorAccountId;
  public static final SqlColumn<String> title = activities.title;
  public static final SqlColumn<String> description = activities.description;
  public static final SqlColumn<String> location = activities.location;
  public static final SqlColumn<Instant> registrationDeadline = activities.registrationDeadline;
  public static final SqlColumn<Instant> startTime = activities.startTime;
  public static final SqlColumn<Instant> endTime = activities.endTime;
  public static final SqlColumn<String> status = activities.status;
  public static final SqlColumn<Long> approvedBy = activities.approvedBy;
  public static final SqlColumn<Instant> approvedAt = activities.approvedAt;
  public static final SqlColumn<Long> rejectedBy = activities.rejectedBy;
  public static final SqlColumn<Instant> rejectedAt = activities.rejectedAt;
  public static final SqlColumn<String> rejectionReason = activities.rejectionReason;
  public static final SqlColumn<Instant> createdAt = activities.createdAt;
  public static final SqlColumn<Instant> updatedAt = activities.updatedAt;

  public static final class Activities extends AliasableSqlTable<Activities> {
    public final SqlColumn<Long> id = column("id", JDBCType.BIGINT);
    public final SqlColumn<Long> creatorAccountId = column("creator_account_id", JDBCType.BIGINT);
    public final SqlColumn<String> title = column("title", JDBCType.VARCHAR);
    public final SqlColumn<String> description = column("description", JDBCType.VARCHAR);
    public final SqlColumn<String> location = column("location", JDBCType.VARCHAR);
    public final SqlColumn<Instant> registrationDeadline =
        column("registration_deadline", JDBCType.TIMESTAMP_WITH_TIMEZONE);
    public final SqlColumn<Instant> startTime =
        column("start_time", JDBCType.TIMESTAMP_WITH_TIMEZONE);
    public final SqlColumn<Instant> endTime = column("end_time", JDBCType.TIMESTAMP_WITH_TIMEZONE);
    public final SqlColumn<String> status = column("status", JDBCType.VARCHAR);
    public final SqlColumn<Long> approvedBy = column("approved_by", JDBCType.BIGINT);
    public final SqlColumn<Instant> approvedAt =
        column("approved_at", JDBCType.TIMESTAMP_WITH_TIMEZONE);
    public final SqlColumn<Long> rejectedBy = column("rejected_by", JDBCType.BIGINT);
    public final SqlColumn<Instant> rejectedAt =
        column("rejected_at", JDBCType.TIMESTAMP_WITH_TIMEZONE);
    public final SqlColumn<String> rejectionReason = column("rejection_reason", JDBCType.VARCHAR);
    public final SqlColumn<Instant> createdAt =
        column("created_at", JDBCType.TIMESTAMP_WITH_TIMEZONE);
    public final SqlColumn<Instant> updatedAt =
        column("updated_at", JDBCType.TIMESTAMP_WITH_TIMEZONE);

    Activities() {
      super("activities", Activities::new);
    }
  }
}
