package io.github.cubelitblade.reaction.persistence.query;

public record PostReactionCountVo(Long targetId, String reactionType, long count) {}
