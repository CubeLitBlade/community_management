package io.github.cubelitblade.post.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class PostModelBranchTest {
  private static final Instant NOW = Instant.parse("2026-04-20T08:00:00Z");

  @Test
  void should_edit_only_non_blank_fields() {
    Post post = post(Status.NORMAL);

    post.edit(null, " ", NOW);
    assertThat(post.getTitle()).isEqualTo("Title");
    assertThat(post.getContent()).isEqualTo("Content");

    post.edit("New title", "New content", NOW.plusSeconds(1));
    assertThat(post.getTitle()).isEqualTo("New title");
    assertThat(post.getContent()).isEqualTo("New content");
    assertThat(post.getUpdatedAt()).isEqualTo(NOW.plusSeconds(1));
  }

  @Test
  void should_archive_idempotently_and_reconstitute_null() {
    Post archived = post(Status.ARCHIVED);
    archived.archive(NOW);

    assertThat(archived.getStatus()).isEqualTo(Status.ARCHIVED);
    assertThat(Post.reconstitute(null)).isNull();
  }

  @Test
  void should_parse_status_values() {
    assertThat(Status.from("normal")).isEqualTo(Status.NORMAL);
    assertThat(Status.from("archived")).isEqualTo(Status.ARCHIVED);
    assertThatThrownBy(() -> Status.from("missing")).isInstanceOf(IllegalArgumentException.class);
  }

  private Post post(Status status) {
    return Post.reconstitute(
        Post.Snapshot.builder()
            .id(1L)
            .authorId(2L)
            .title("Title")
            .content("Content")
            .status(status)
            .createdAt(NOW.minusSeconds(60))
            .updatedAt(NOW.minusSeconds(30))
            .build());
  }
}
