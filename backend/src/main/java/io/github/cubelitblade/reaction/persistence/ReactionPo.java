package io.github.cubelitblade.reaction.persistence;

import io.github.cubelitblade.reaction.model.Reaction;
import io.github.cubelitblade.reaction.model.ReactionType;
import io.github.cubelitblade.reaction.model.TargetType;
import java.time.Instant;

public record ReactionPo(
    Long id,
    Long accountId,
    String targetType,
    Long targetId,
    String reactionType,
    Instant createdAt,
    Instant updatedAt) {
  public Reaction toReaction() {
    return new Reaction(
        id,
        accountId,
        TargetType.from(targetType),
        targetId,
        ReactionType.from(reactionType),
        createdAt,
        updatedAt);
  }

  public static ReactionPo of(Reaction reaction) {
    return new ReactionPo(
        reaction.getId(),
        reaction.getAccountId(),
        reaction.getTargetType().getValue(),
        reaction.getTargetId(),
        reaction.getReactionType().getValue(),
        reaction.getCreatedAt(),
        reaction.getUpdatedAt());
  }
}
