package io.github.cubelitblade.comment.persistence;

import io.github.cubelitblade.comment.persistence.query.CommentWithAuthorVo;
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

public interface CommentQueryMapper {

  @SelectProvider(type = SqlProviderAdapter.class, method = "select")
  @Results(id = "CommentWithAuthorResult")
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
        jdbcType = JdbcType.TIMESTAMP_WITH_TIMEZONE),
    @Arg(column = "username", javaType = String.class, jdbcType = JdbcType.VARCHAR),
    @Arg(column = "nickname", javaType = String.class, jdbcType = JdbcType.VARCHAR),
    @Arg(column = "reply_to_account_id", javaType = Long.class, jdbcType = JdbcType.BIGINT),
    @Arg(column = "reply_to_username", javaType = String.class, jdbcType = JdbcType.VARCHAR),
    @Arg(column = "reply_to_nickname", javaType = String.class, jdbcType = JdbcType.VARCHAR)
  })
  List<CommentWithAuthorVo> selectCommentsWith(SelectStatementProvider selectStatement);

  @SelectProvider(type = SqlProviderAdapter.class, method = "select")
  @ResultMap("CommentWithAuthorResult")
  Optional<CommentWithAuthorVo> selectCommentWith(SelectStatementProvider selectStatement);
}
