package io.github.cubelitblade.post.dto;

import io.github.cubelitblade.post.persistence.query.PostWithAuthorVo;
import java.time.Instant;

public record PostDetailView(
    Long id,
    Long authorId,
    String authorUsername,
    String authorNickname,
    String title,
    String content,
    Instant createdAt,
    Instant updatedAt) {
  public static PostDetailView from(PostWithAuthorVo postWithAuthorVo) {
    return new PostDetailView(
        postWithAuthorVo.id(),
        postWithAuthorVo.authorId(),
        postWithAuthorVo.username(),
        postWithAuthorVo.nickname(),
        postWithAuthorVo.title(),
        postWithAuthorVo.content(),
        postWithAuthorVo.createdAt(),
        postWithAuthorVo.updatedAt());
  }
}
