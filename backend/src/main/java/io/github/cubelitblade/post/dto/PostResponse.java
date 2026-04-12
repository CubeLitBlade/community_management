package io.github.cubelitblade.post.dto;

import io.github.cubelitblade.post.persistence.query.PostWithAuthorVo;
import java.time.Instant;

public record PostResponse(
    Long id,
    Long authorId,
    String authorUsername,
    String authorNickname,
    String title,
    String content,
    Instant createdAt,
    Instant updatedAt) {
  public static PostResponse from(PostWithAuthorVo postWithAuthorVo) {
    return new PostResponse(
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
