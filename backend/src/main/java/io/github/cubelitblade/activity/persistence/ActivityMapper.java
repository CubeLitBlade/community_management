package io.github.cubelitblade.activity.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import org.mybatis.dynamic.sql.util.mybatis3.CommonCountMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonDeleteMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonInsertMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper;

public interface ActivityMapper
    extends CommonCountMapper,
        CommonDeleteMapper,
        CommonInsertMapper<ActivityPo>,
        CommonUpdateMapper {

  @SelectProvider(type = SqlProviderAdapter.class, method = "select")
  @Results(id = "ActivityResult")
  @ConstructorArgs({
    @Arg(column = "id", javaType = Long.class, jdbcType = JdbcType.BIGINT, id = true),
    @Arg(column = "creator_account_id", javaType = Long.class, jdbcType = JdbcType.BIGINT),
    @Arg(column = "title", javaType = String.class, jdbcType = JdbcType.VARCHAR),
    @Arg(column = "description", javaType = String.class, jdbcType = JdbcType.VARCHAR),
    @Arg(column = "location", javaType = String.class, jdbcType = JdbcType.VARCHAR),
    @Arg(
        column = "registration_deadline",
        javaType = Instant.class,
        jdbcType = JdbcType.TIMESTAMP_WITH_TIMEZONE),
    @Arg(column = "start_time", javaType = Instant.class, jdbcType = JdbcType.TIMESTAMP_WITH_TIMEZONE),
    @Arg(column = "end_time", javaType = Instant.class, jdbcType = JdbcType.TIMESTAMP_WITH_TIMEZONE),
    @Arg(column = "status", javaType = String.class, jdbcType = JdbcType.VARCHAR),
    @Arg(column = "approved_by", javaType = Long.class, jdbcType = JdbcType.BIGINT),
    @Arg(
        column = "approved_at",
        javaType = Instant.class,
        jdbcType = JdbcType.TIMESTAMP_WITH_TIMEZONE),
    @Arg(column = "rejected_by", javaType = Long.class, jdbcType = JdbcType.BIGINT),
    @Arg(
        column = "rejected_at",
        javaType = Instant.class,
        jdbcType = JdbcType.TIMESTAMP_WITH_TIMEZONE),
    @Arg(column = "rejection_reason", javaType = String.class, jdbcType = JdbcType.VARCHAR),
    @Arg(
        column = "created_at",
        javaType = Instant.class,
        jdbcType = JdbcType.TIMESTAMP_WITH_TIMEZONE),
    @Arg(
        column = "updated_at",
        javaType = Instant.class,
        jdbcType = JdbcType.TIMESTAMP_WITH_TIMEZONE)
  })
  List<ActivityPo> selectMany(SelectStatementProvider selectStatement);

  @SelectProvider(type = SqlProviderAdapter.class, method = "select")
  @ResultMap("ActivityResult")
  Optional<ActivityPo> selectOne(SelectStatementProvider selectStatement);
}
