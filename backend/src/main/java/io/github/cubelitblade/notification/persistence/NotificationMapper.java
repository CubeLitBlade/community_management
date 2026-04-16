package io.github.cubelitblade.notification.persistence;

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

public interface NotificationMapper
    extends CommonCountMapper,
        CommonDeleteMapper,
        CommonInsertMapper<NotificationPo>,
        CommonUpdateMapper {

  @SelectProvider(type = SqlProviderAdapter.class, method = "select")
  @Results(id = "NotificationResult")
  @ConstructorArgs({
    @Arg(column = "id", javaType = Long.class, jdbcType = JdbcType.BIGINT, id = true),
    @Arg(column = "recipient_account_id", javaType = Long.class, jdbcType = JdbcType.BIGINT),
    @Arg(column = "actor_account_id", javaType = Long.class, jdbcType = JdbcType.BIGINT),
    @Arg(column = "type", javaType = String.class, jdbcType = JdbcType.VARCHAR),
    @Arg(column = "target_type", javaType = String.class, jdbcType = JdbcType.VARCHAR),
    @Arg(column = "target_id", javaType = Long.class, jdbcType = JdbcType.BIGINT),
    @Arg(column = "content", javaType = String.class, jdbcType = JdbcType.VARCHAR),
    @Arg(column = "is_read", javaType = Boolean.class, jdbcType = JdbcType.BOOLEAN),
    @Arg(column = "read_at", javaType = Instant.class, jdbcType = JdbcType.TIMESTAMP_WITH_TIMEZONE),
    @Arg(
        column = "created_at",
        javaType = Instant.class,
        jdbcType = JdbcType.TIMESTAMP_WITH_TIMEZONE)
  })
  List<NotificationPo> selectMany(SelectStatementProvider selectStatement);

  @SelectProvider(type = SqlProviderAdapter.class, method = "select")
  @ResultMap("NotificationResult")
  Optional<NotificationPo> selectOne(SelectStatementProvider selectStatement);
}
