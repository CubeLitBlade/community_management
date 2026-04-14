package io.github.cubelitblade.reaction.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Reaction {
  Long id;
  Long accountId;
  TargetType targetType;
  Long targetId;
  ReactionType reactionType;
  Instant createdAt;
  Instant updatedAt;

  public static Reaction create(
      Long id,
      Long accountId,
      TargetType targetType,
      Long targetId,
      ReactionType reactionType,
      Instant now) {
    Reaction reaction = new Reaction();

    reaction.id = id;
    reaction.accountId = accountId;
    reaction.targetType = targetType;
    reaction.targetId = targetId;
    reaction.reactionType = reactionType;
    reaction.createdAt = now;

    return reaction;
  }

  public void set(ReactionType reactionType, Instant now) {
    this.reactionType = reactionType;
    this.updatedAt = now;
  }
}
