package io.github.cubelitblade.comment.persistence;

import io.github.cubelitblade.comment.model.Comment;
import io.github.cubelitblade.comment.model.Status;
import io.github.cubelitblade.comment.model.TargetType;
import java.time.Instant;
import lombok.Builder;

@Builder
public record CommentPo(
    Long id,
    String targetType,
    Long targetId,
    Long accountId,
    Long parentId,
    String content,
    String status,
    Instant createdAt,
    Instant updatedAt) {
  public static CommentPo of(Comment comment) {
    if (comment == null) {
      return null;
    }

    return CommentPo.builder()
        .id(comment.getId())
        .targetType(comment.getTargetType().getValue())
        .targetId(comment.getTargetId())
        .accountId(comment.getAccountId())
        .parentId(comment.getParentId())
        .content(comment.getContent())
        .status(comment.getStatus().getValue())
        .createdAt(comment.getCreatedAt())
        .updatedAt(comment.getUpdatedAt())
        .build();
  }

  public Comment toComment() {
    Comment.Snapshot snapshot =
        Comment.Snapshot.builder()
            .id(this.id)
            .targetType(TargetType.from(this.targetType))
            .targetId(this.targetId)
            .accountId(this.accountId)
            .parentId(this.parentId)
            .content(this.content)
            .status(Status.from(this.status))
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();

    return Comment.reconstitute(snapshot);
  }
}
