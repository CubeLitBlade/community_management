package io.github.cubelitblade.post.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Post {
  private Long id;

  private Long authorId;
  private String title;
  private String content;
  private Status status;
  private Instant createdAt;
  private Instant updatedAt;

  public static Post createPost(Long id, Long authorId, String title, String content, Instant now) {
    Post post = new Post();

    post.id = id;
    post.authorId = authorId;
    post.title = title;
    post.content = content;
    post.status = Status.NORMAL;
    post.createdAt = now;

    return post;
  }

  public void edit(String title, String content, Instant now) {
    if (title != null && !title.isBlank()) {
      this.title = title;
    }
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

  public static Post reconstitute(Snapshot snapshot) {
    if (snapshot == null) return null;

    Post post = new Post();
    post.id = snapshot.id;
    post.authorId = snapshot.authorId;
    post.title = snapshot.title;
    post.content = snapshot.content;
    post.status = snapshot.status;
    post.createdAt = snapshot.createdAt;
    post.updatedAt = snapshot.updatedAt;

    return post;
  }

  private void touch(Instant now) {
    this.updatedAt = now;
  }

  @Builder
  public record Snapshot(
      Long id,
      Long authorId,
      String title,
      String content,
      Status status,
      Instant createdAt,
      Instant updatedAt) {}
}
