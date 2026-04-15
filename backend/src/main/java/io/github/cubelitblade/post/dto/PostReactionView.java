package io.github.cubelitblade.post.dto;

import io.github.cubelitblade.reaction.persistence.query.ReactionCountVo;

public record PostReactionView(String reactionType, long count) {
  public static PostReactionView from(ReactionCountVo reactionCountVo) {
    return new PostReactionView(reactionCountVo.reactionType(), reactionCountVo.count());
  }
}
