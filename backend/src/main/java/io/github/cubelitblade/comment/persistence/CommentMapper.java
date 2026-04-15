package io.github.cubelitblade.comment.persistence;

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

public interface CommentMapper
    extends CommonCountMapper,
        CommonDeleteMapper,
        CommonInsertMapper<CommentPo>,
        CommonUpdateMapper {

  @SelectProvider(type = SqlProviderAdapter.class, method = "select")
  @Results(id = "CommentResult")
  @ConstructorArgs({
    @Arg(column = "id", javaType = Long.class, jdbcType = JdbcType.BIGINT, id = true),
    @Arg(column = "target_type", javaType = String.class, jdbcType = JdbcType.VARCHAR),
    @Arg(column = "target_id", javaType = Long.class, jdbcType = JdbcType.BIGINT),
    @Arg(column = "account_id", javaType = Long.class, jdbcType = JdbcType.BIGINT),
    @Arg(column = "parent_id", javaType = Long.class, jdbcType = JdbcType.BIGINT),
    @Arg(column = "content", javaType = String.class, jdbcType = JdbcType.VARCHAR),
    @Arg(column = "status", javaType = String.class, jdbcType = JdbcType.VARCHAR),
    @Arg(
        column = "created_at",
        javaType = Instant.class,
        jdbcType = JdbcType.TIMESTAMP_WITH_TIMEZONE),
    @Arg(
        column = "updated_at",
        javaType = Instant.class,
        jdbcType = JdbcType.TIMESTAMP_WITH_TIMEZONE)
  })
  List<CommentPo> selectMany(SelectStatementProvider selectStatement);

  @SelectProvider(type = SqlProviderAdapter.class, method = "select")
  @ResultMap("CommentResult")
  Optional<CommentPo> selectOne(SelectStatementProvider selectStatement);
}
