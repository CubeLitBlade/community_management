package io.github.cubelitblade.reaction.persistence;

import io.github.cubelitblade.reaction.model.Reaction;
import io.github.cubelitblade.reaction.model.TargetType;
import io.github.cubelitblade.reaction.persistence.query.ReactionCountVo;
import lombok.RequiredArgsConstructor;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.mybatis.dynamic.sql.SqlBuilder.*;

@Repository
@RequiredArgsConstructor
public class ReactionRepository {

  private final ReactionMapper reactionMapper;

  public void createReaction(Reaction reaction) {
    ReactionPo reactionPo = ReactionPo.of(reaction);
    reactionMapper.insert(reactionPo);
  }

  public List<ReactionCountVo> selectReactions(TargetType targetType, Long targetId) {
    SelectStatementProvider selectStatement =
      select(ReactionDynamicSqlSupport.reactionType, count().as("count"))
        .from(ReactionDynamicSqlSupport.reactions)
        .where(ReactionDynamicSqlSupport.targetType, isEqualTo(targetType.getValue()))
        .and(ReactionDynamicSqlSupport.targetId, isEqualTo(targetId))
        .groupBy(ReactionDynamicSqlSupport.reactionType)
        .build()
        .render(RenderingStrategies.MYBATIS3);

    return reactionMapper.selectReactionCounts(selectStatement);
  }
}
