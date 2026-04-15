package io.github.cubelitblade.reaction.persistence;

import static org.mybatis.dynamic.sql.SqlBuilder.*;

import io.github.cubelitblade.reaction.application.ReactionCountCache;
import io.github.cubelitblade.reaction.model.TargetType;
import io.github.cubelitblade.reaction.persistence.query.PostReactionCountVo;
import io.github.cubelitblade.reaction.persistence.query.ReactionCountVo;
import io.github.cubelitblade.reaction.persistence.query.UserTargetReactionVo;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReactionQueryRepository {

  private final ReactionMapper reactionMapper;
  private final ReactionCountCache reactionCountCache;

  public Map<Long, List<ReactionCountVo>> selectReactionsByTargets(
      TargetType targetType, List<Long> targetIds) {
    if (targetType == TargetType.POST) {
      return reactionCountCache.getPostReactionCounts(
          targetIds, ids -> queryReactionCountsByTargets(targetType, ids));
    }

    return queryReactionCountsByTargets(targetType, targetIds);
  }

  public Map<Long, String> findUserReactionsByTargets(
      Long accountId, TargetType targetType, List<Long> targetIds) {
    if (targetIds.isEmpty()) {
      return Collections.emptyMap();
    }

    SelectStatementProvider selectStatement =
        select(ReactionDynamicSqlSupport.targetId, ReactionDynamicSqlSupport.reactionType)
            .from(ReactionDynamicSqlSupport.reactions)
            .where(ReactionDynamicSqlSupport.accountId, isEqualTo(accountId))
            .and(ReactionDynamicSqlSupport.targetType, isEqualTo(targetType.getValue()))
            .and(ReactionDynamicSqlSupport.targetId, isIn(targetIds))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    List<UserTargetReactionVo> userReactions =
        reactionMapper.selectUserTargetReactions(selectStatement);

    return userReactions.stream()
        .collect(
            Collectors.toMap(UserTargetReactionVo::targetId, UserTargetReactionVo::reactionType));
  }

  private Map<Long, List<ReactionCountVo>> queryReactionCountsByTargets(
      TargetType targetType, List<Long> targetIds) {
    if (targetIds.isEmpty()) {
      return Collections.emptyMap();
    }

    SelectStatementProvider selectStatement =
        select(
                ReactionDynamicSqlSupport.targetId,
                ReactionDynamicSqlSupport.reactionType,
                count().as("count"))
            .from(ReactionDynamicSqlSupport.reactions)
            .where(ReactionDynamicSqlSupport.targetType, isEqualTo(targetType.getValue()))
            .and(ReactionDynamicSqlSupport.targetId, isIn(targetIds))
            .groupBy(ReactionDynamicSqlSupport.targetId, ReactionDynamicSqlSupport.reactionType)
            .build()
            .render(RenderingStrategies.MYBATIS3);

    List<PostReactionCountVo> reactionCounts =
        reactionMapper.selectPostReactionCounts(selectStatement);

    return reactionCounts.stream()
        .collect(
            Collectors.groupingBy(
                PostReactionCountVo::targetId,
                Collectors.mapping(
                    reaction -> new ReactionCountVo(reaction.reactionType(), reaction.count()),
                    Collectors.toList())));
  }
}
