package io.github.cubelitblade.post.dto;

import io.github.cubelitblade.post.model.Post;
import java.time.Instant;

public record PostResponse(
    Long id, Long authorId, String title, String content, Instant createdAt, Instant updatedAt) {
  public static PostResponse from(Post post) {
    return new PostResponse(
        post.getId(),
        post.getAuthorId(),
        post.getTitle(),
        post.getContent(),
        post.getCreatedAt(),
        post.getUpdatedAt());
  }
}
