package io.github.cubelitblade.reaction.application;

public record SetReactionResult(Long reactionId, Outcome outcome) {

  public static SetReactionResult created(Long reactionId) {
    return new SetReactionResult(reactionId, Outcome.CREATED);
  }

  public static SetReactionResult updated(Long reactionId) {
    return new SetReactionResult(reactionId, Outcome.UPDATED);
  }

  public static SetReactionResult removed(Long reactionId) {
    return new SetReactionResult(reactionId, Outcome.REMOVED);
  }

  public static SetReactionResult unchanged() {
    return new SetReactionResult(null, Outcome.UNCHANGED);
  }

  public enum Outcome {
    CREATED,
    UPDATED,
    REMOVED,
    UNCHANGED
  }
}
