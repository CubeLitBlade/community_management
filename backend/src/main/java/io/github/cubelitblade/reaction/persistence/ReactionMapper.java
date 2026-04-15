package io.github.cubelitblade.reaction.persistence;

import io.github.cubelitblade.reaction.persistence.query.PostReactionCountVo;
import io.github.cubelitblade.reaction.persistence.query.UserTargetReactionVo;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import org.mybatis.dynamic.sql.util.mybatis3.*;

public interface ReactionMapper
    extends CommonCountMapper,
        CommonDeleteMapper,
        CommonInsertMapper<ReactionPo>,
        CommonUpdateMapper {

  @SelectProvider(type = SqlProviderAdapter.class, method = "select")
  @Results(id = "ReactionResult")
  @ConstructorArgs({
    @Arg(column = "id", javaType = Long.class, jdbcType = JdbcType.BIGINT, id = true),
    @Arg(column = "account_id", javaType = Long.class, jdbcType = JdbcType.BIGINT),
    @Arg(column = "target_type", javaType = String.class, jdbcType = JdbcType.VARCHAR),
    @Arg(column = "target_id", javaType = Long.class, jdbcType = JdbcType.BIGINT),
    @Arg(column = "reaction_type", javaType = String.class, jdbcType = JdbcType.VARCHAR),
    @Arg(
        column = "created_at",
        javaType = java.time.Instant.class,
        jdbcType = JdbcType.TIMESTAMP_WITH_TIMEZONE),
    @Arg(
        column = "updated_at",
        javaType = java.time.Instant.class,
        jdbcType = JdbcType.TIMESTAMP_WITH_TIMEZONE)
  })
  List<ReactionPo> selectMany(SelectStatementProvider selectStatement);

  @SelectProvider(type = SqlProviderAdapter.class, method = "select")
  @ResultMap("ReactionResult")
  Optional<ReactionPo> selectOne(SelectStatementProvider selectStatement);

  @SelectProvider(type = SqlProviderAdapter.class, method = "select")
  @Results(id = "PostReactionCountResult")
  @ConstructorArgs({
    @Arg(column = "target_id", javaType = Long.class, jdbcType = JdbcType.BIGINT),
    @Arg(column = "reaction_type", javaType = String.class, jdbcType = JdbcType.VARCHAR),
    @Arg(column = "count", javaType = long.class, jdbcType = JdbcType.BIGINT)
  })
  List<PostReactionCountVo> selectPostReactionCounts(SelectStatementProvider selectStatement);

  @SelectProvider(type = SqlProviderAdapter.class, method = "select")
  @Results(id = "UserTargetReactionResult")
  @ConstructorArgs({
    @Arg(column = "target_id", javaType = Long.class, jdbcType = JdbcType.BIGINT),
    @Arg(column = "reaction_type", javaType = String.class, jdbcType = JdbcType.VARCHAR)
  })
  List<UserTargetReactionVo> selectUserTargetReactions(SelectStatementProvider selectStatement);

  default int insert(ReactionPo record) {
    return MyBatis3Utils.insert(
        this::insert,
        record,
        ReactionDynamicSqlSupport.reactions,
        c ->
            c.withMappedColumn(ReactionDynamicSqlSupport.id)
                .withMappedColumn(ReactionDynamicSqlSupport.accountId)
                .withMappedColumn(ReactionDynamicSqlSupport.targetType)
                .withMappedColumn(ReactionDynamicSqlSupport.targetId)
                .withMappedColumn(ReactionDynamicSqlSupport.reactionType)
                .withMappedColumn(ReactionDynamicSqlSupport.createdAt)
                .withMappedColumn(ReactionDynamicSqlSupport.updatedAt));
  }

  default int insertMultiple(Collection<ReactionPo> records) {
    return MyBatis3Utils.insertMultiple(
        this::insertMultiple,
        records,
        ReactionDynamicSqlSupport.reactions,
        c ->
            c.withMappedColumn(ReactionDynamicSqlSupport.id)
                .withMappedColumn(ReactionDynamicSqlSupport.accountId)
                .withMappedColumn(ReactionDynamicSqlSupport.targetType)
                .withMappedColumn(ReactionDynamicSqlSupport.targetId)
                .withMappedColumn(ReactionDynamicSqlSupport.reactionType)
                .withMappedColumn(ReactionDynamicSqlSupport.createdAt)
                .withMappedColumn(ReactionDynamicSqlSupport.updatedAt));
  }
}
