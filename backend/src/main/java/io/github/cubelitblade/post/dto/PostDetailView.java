package io.github.cubelitblade.post.dto;

import io.github.cubelitblade.post.persistence.query.PostWithAuthorVo;
import java.time.Instant;
import java.util.List;

public record PostDetailView(
    Long id,
    Long authorId,
    String authorUsername,
    String authorNickname,
    String title,
    String content,
    List<PostReactionView> reactions,
    String viewerReaction,
    Instant createdAt,
    Instant updatedAt) {
  public static PostDetailView from(
      PostWithAuthorVo postWithAuthorVo, List<PostReactionView> reactions, String viewerReaction) {
    return new PostDetailView(
        postWithAuthorVo.id(),
        postWithAuthorVo.authorId(),
        postWithAuthorVo.username(),
        postWithAuthorVo.nickname(),
        postWithAuthorVo.title(),
        postWithAuthorVo.content(),
        reactions,
        viewerReaction,
        postWithAuthorVo.createdAt(),
        postWithAuthorVo.updatedAt());
  }
}
