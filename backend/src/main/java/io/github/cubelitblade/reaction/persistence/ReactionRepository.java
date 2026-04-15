package io.github.cubelitblade.reaction.persistence;

import static org.mybatis.dynamic.sql.SqlBuilder.*;

import io.github.cubelitblade.reaction.model.Reaction;
import io.github.cubelitblade.reaction.model.TargetType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReactionRepository {

  private final ReactionMapper reactionMapper;

  public void createReaction(Reaction reaction) {
    ReactionPo reactionPo = ReactionPo.of(reaction);
    reactionMapper.insert(reactionPo);
  }

  public Optional<Reaction> findUserReaction(Long accountId, TargetType targetType, Long targetId) {
    SelectStatementProvider selectStatement =
        select(
                ReactionDynamicSqlSupport.id,
                ReactionDynamicSqlSupport.accountId,
                ReactionDynamicSqlSupport.targetType,
                ReactionDynamicSqlSupport.targetId,
                ReactionDynamicSqlSupport.reactionType,
                ReactionDynamicSqlSupport.createdAt,
                ReactionDynamicSqlSupport.updatedAt)
            .from(ReactionDynamicSqlSupport.reactions)
            .where(ReactionDynamicSqlSupport.accountId, isEqualTo(accountId))
            .and(ReactionDynamicSqlSupport.targetType, isEqualTo(targetType.getValue()))
            .and(ReactionDynamicSqlSupport.targetId, isEqualTo(targetId))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    return reactionMapper.selectOne(selectStatement).map(ReactionPo::toReaction);
  }

  public void updateReaction(Reaction reaction) {
    ReactionPo reactionPo = ReactionPo.of(reaction);

    UpdateStatementProvider updateStatement =
        update(ReactionDynamicSqlSupport.reactions)
            .set(ReactionDynamicSqlSupport.reactionType)
            .equalTo(reactionPo::reactionType)
            .set(ReactionDynamicSqlSupport.updatedAt)
            .equalTo(reactionPo::updatedAt)
            .where(ReactionDynamicSqlSupport.id, isEqualTo(reaction.getId()))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    reactionMapper.update(updateStatement);
  }

  public void deleteReaction(Long reactionId) {
    DeleteStatementProvider deleteStatement =
        deleteFrom(ReactionDynamicSqlSupport.reactions)
            .where(ReactionDynamicSqlSupport.id, isEqualTo(reactionId))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    reactionMapper.delete(deleteStatement);
  }
}
