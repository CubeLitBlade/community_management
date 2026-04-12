package io.github.cubelitblade.post.persistence;

import io.github.cubelitblade.post.persistence.query.PostWithAuthorVo;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

public interface PostQueryMapper {

  @SelectProvider(type = SqlProviderAdapter.class, method = "select")
  @Results(id = "PostWithAuthorResult")
  @ConstructorArgs({
    @Arg(column = "id", javaType = Long.class, jdbcType = JdbcType.BIGINT, id = true),
    @Arg(column = "author_id", javaType = Long.class, jdbcType = JdbcType.BIGINT),
    @Arg(column = "title", javaType = String.class, jdbcType = JdbcType.VARCHAR),
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
    @Arg(column = "nickname", javaType = String.class, jdbcType = JdbcType.VARCHAR)
  })
  List<PostWithAuthorVo> selectPostsWith(SelectStatementProvider selectStatement);

  @SelectProvider(type = SqlProviderAdapter.class, method = "select")
  @ResultMap("PostWithAuthorResult")
  Optional<PostWithAuthorVo> selectPostWithAuthor(SelectStatementProvider selectStatement);
}
