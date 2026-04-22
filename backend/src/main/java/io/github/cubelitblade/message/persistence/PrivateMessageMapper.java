package io.github.cubelitblade.message.persistence;

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
import org.mybatis.dynamic.sql.util.mybatis3.CommonInsertMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper;

public interface PrivateMessageMapper
    extends CommonCountMapper, CommonInsertMapper<PrivateMessagePo>, CommonUpdateMapper {

  @SelectProvider(type = SqlProviderAdapter.class, method = "select")
  @Results(id = "PrivateMessageResult")
  @ConstructorArgs({
    @Arg(column = "id", javaType = Long.class, jdbcType = JdbcType.BIGINT, id = true),
    @Arg(column = "sender_account_id", javaType = Long.class, jdbcType = JdbcType.BIGINT),
    @Arg(column = "recipient_account_id", javaType = Long.class, jdbcType = JdbcType.BIGINT),
    @Arg(column = "content", javaType = String.class, jdbcType = JdbcType.VARCHAR),
    @Arg(column = "is_read", javaType = Boolean.class, jdbcType = JdbcType.BOOLEAN),
    @Arg(column = "read_at", javaType = Instant.class, jdbcType = JdbcType.TIMESTAMP_WITH_TIMEZONE),
    @Arg(
        column = "created_at",
        javaType = Instant.class,
        jdbcType = JdbcType.TIMESTAMP_WITH_TIMEZONE)
  })
  List<PrivateMessagePo> selectMany(SelectStatementProvider selectStatement);

  @SelectProvider(type = SqlProviderAdapter.class, method = "select")
  @ResultMap("PrivateMessageResult")
  Optional<PrivateMessagePo> selectOne(SelectStatementProvider selectStatement);
}
