package io.github.cubelitblade.reaction.persistence;

import io.github.cubelitblade.reaction.model.Reaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReactionRepository {

  private final ReactionMapper reactionMapper;

  public void createReaction(Reaction reaction) {
    ReactionPo reactionPo = ReactionPo.of(reaction);
    reactionMapper.insert(reactionPo);
  }
}
