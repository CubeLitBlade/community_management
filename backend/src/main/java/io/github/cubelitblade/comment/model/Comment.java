package io.github.cubelitblade.comment.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Comment {
  private Long id;
  private TargetType targetType;
  private Long targetId;
  private Long accountId;
  private Long parentId;
  private String content;
  private Status status;
  private Instant createdAt;
  private Instant updatedAt;

  public static Comment create(
      Long id,
      TargetType targetType,
      Long targetId,
      Long accountId,
      Long parentId,
      String content,
      Instant now) {
    Comment comment = new Comment();

    comment.id = id;
    comment.targetType = targetType;
    comment.targetId = targetId;
    comment.accountId = accountId;
    comment.parentId = parentId;
    comment.content = content;
    comment.status = Status.NORMAL;
    comment.createdAt = now;

    return comment;
  }

  public void edit(String content, Instant now) {
    if (content != null && !content.isBlank()) {
      this.content = content;
    }

    this.touch(now);
  }

  public void archive(Instant now) {
    if (status == Status.ARCHIVED) {
      return;
    }

    status = Status.ARCHIVED;
    this.touch(now);
  }

  public static Comment reconstitute(Snapshot snapshot) {
    if (snapshot == null) {
      return null;
    }

    Comment comment = new Comment();
    comment.id = snapshot.id;
    comment.targetType = snapshot.targetType;
    comment.targetId = snapshot.targetId;
    comment.accountId = snapshot.accountId;
    comment.parentId = snapshot.parentId;
    comment.content = snapshot.content;
    comment.status = snapshot.status;
    comment.createdAt = snapshot.createdAt;
    comment.updatedAt = snapshot.updatedAt;

    return comment;
  }

  private void touch(Instant now) {
    this.updatedAt = now;
  }

  @Builder
  public record Snapshot(
      Long id,
      TargetType targetType,
      Long targetId,
      Long accountId,
      Long parentId,
      String content,
      Status status,
      Instant createdAt,
      Instant updatedAt) {}
}
