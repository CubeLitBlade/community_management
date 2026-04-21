package io.github.cubelitblade.post.persistence;

import io.github.cubelitblade.post.model.Post;
import io.github.cubelitblade.post.model.Status;
import java.time.Instant;
import lombok.Builder;

@Builder
public record PostPo(
    Long id,
    Long authorId,
    String title,
    String content,
    String status,
    Instant createdAt,
    Instant updatedAt) {

  // 从 Post 扁平化到 PostPo
  public static PostPo of(Post post) {
    if (post == null) {
      return null;
    }

    return PostPo.builder()
        .id(post.getId())
        .authorId(post.getAuthorId())
        .title(post.getTitle())
        .content(post.getContent())
        .status(post.getStatus().getValue())
        .createdAt(post.getCreatedAt())
        .updatedAt(post.getUpdatedAt())
        .build();
  }

  // 从 PostPo 结构化到 Post 快照，再通过 reconstitute 方法重建 Post
  public Post toPost() {
    Post.Snapshot snapshot =
        Post.Snapshot.builder()
            .id(this.id)
            .authorId(this.authorId)
            .title(this.title)
            .content(this.content)
            .status(Status.from(this.status))
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();

    return Post.reconstitute(snapshot);
  }
}
