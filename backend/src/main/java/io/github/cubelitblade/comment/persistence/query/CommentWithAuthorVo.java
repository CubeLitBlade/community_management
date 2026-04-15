package io.github.cubelitblade.comment.persistence.query;

import java.time.Instant;

public record CommentWithAuthorVo(
    // from comments
    Long id,
    String targetType,
    Long targetId,
    Long accountId,
    Long parentId,
    String content,
    String status,
    Instant createdAt,
    Instant updatedAt,

    // from comment author
    String username,
    String nickname,

    // from parent comment author
    Long replyToAccountId,
    String replyToUsername,
    String replyToNickname) {}
