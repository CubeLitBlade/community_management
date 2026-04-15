package io.github.cubelitblade.comment.dto;

import io.github.cubelitblade.comment.persistence.query.CommentWithAuthorVo;
import java.time.Instant;

public record CommentDetailView(
    Long id,
    String targetType,
    Long targetId,
    Long accountId,
    String authorUsername,
    String authorNickname,
    Long parentId,
    Long replyToAccountId,
    String replyToUsername,
    String replyToNickname,
    String content,
    Instant createdAt,
    Instant updatedAt) {
  public static CommentDetailView from(CommentWithAuthorVo commentWithAuthorVo) {
    return new CommentDetailView(
        commentWithAuthorVo.id(),
        commentWithAuthorVo.targetType(),
        commentWithAuthorVo.targetId(),
        commentWithAuthorVo.accountId(),
        commentWithAuthorVo.username(),
        commentWithAuthorVo.nickname(),
        commentWithAuthorVo.parentId(),
        commentWithAuthorVo.replyToAccountId(),
        commentWithAuthorVo.replyToUsername(),
        commentWithAuthorVo.replyToNickname(),
        commentWithAuthorVo.content(),
        commentWithAuthorVo.createdAt(),
        commentWithAuthorVo.updatedAt());
  }
}
