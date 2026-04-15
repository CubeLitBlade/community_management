package io.github.cubelitblade.comment.dto;

public record CreateCommentRequest(
    String targetType, Long targetId, Long parentId, String content) {}
