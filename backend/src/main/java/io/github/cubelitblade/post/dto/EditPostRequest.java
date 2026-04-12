package io.github.cubelitblade.post.dto;

public record EditPostRequest(
  Long postId,
  String title,
  String content
) {
}
