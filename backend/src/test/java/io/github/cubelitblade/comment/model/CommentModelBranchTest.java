package io.github.cubelitblade.comment.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class CommentModelBranchTest {
  private static final Instant NOW = Instant.parse("2026-04-20T08:00:00Z");

  @Test
  void should_edit_only_non_blank_content() {
    Comment comment = comment(Status.NORMAL);

    comment.edit(null, NOW);
    assertThat(comment.getContent()).isEqualTo("Content");

    comment.edit(" ", NOW.plusSeconds(1));
    assertThat(comment.getContent()).isEqualTo("Content");

    comment.edit("Updated", NOW.plusSeconds(2));
    assertThat(comment.getContent()).isEqualTo("Updated");
    assertThat(comment.getUpdatedAt()).isEqualTo(NOW.plusSeconds(2));
  }

  @Test
  void should_archive_idempotently_and_reconstitute_null() {
    Comment archived = comment(Status.ARCHIVED);
    archived.archive(NOW);

    assertThat(archived.getStatus()).isEqualTo(Status.ARCHIVED);
    assertThat(Comment.reconstitute(null)).isNull();
  }

  @Test
  void should_parse_status_values() {
    assertThat(Status.from("normal")).isEqualTo(Status.NORMAL);
    assertThat(Status.from("archived")).isEqualTo(Status.ARCHIVED);
    assertThatThrownBy(() -> Status.from("missing")).isInstanceOf(IllegalArgumentException.class);
  }

  private Comment comment(Status status) {
    return Comment.reconstitute(
        Comment.Snapshot.builder()
            .id(1L)
            .targetType(TargetType.POST)
            .targetId(2L)
            .accountId(3L)
            .content("Content")
            .status(status)
            .createdAt(NOW.minusSeconds(60))
            .updatedAt(NOW.minusSeconds(30))
            .build());
  }
}
