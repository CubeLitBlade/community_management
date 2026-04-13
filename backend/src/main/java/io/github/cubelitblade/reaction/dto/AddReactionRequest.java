package io.github.cubelitblade.reaction.dto;

public record AddReactionRequest(String targetType, Long targetId, String reactionType) {}
