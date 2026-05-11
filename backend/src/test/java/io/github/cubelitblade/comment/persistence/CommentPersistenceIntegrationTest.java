package io.github.cubelitblade.comment.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cubelitblade.comment.model.Comment;
import io.github.cubelitblade.comment.model.Status;
import io.github.cubelitblade.comment.model.TargetType;
import io.github.cubelitblade.comment.persistence.query.CommentWithAuthorVo;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class CommentPersistenceIntegrationTest {
  private static final Instant NOW = Instant.parse("2026-04-20T08:00:00Z");

  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private CommentRepository commentRepository;
  @Autowired private CommentQueryRepository commentQueryRepository;

  @BeforeEach
  void setUp() {
    insertAccount(9201L, "comment_author", "Comment Author");
    insertAccount(9202L, "reply_author", "Reply Author");
  }

  @Test
  @DisplayName("CommentRepository: should create and read normal comment")
  void should_create_and_read_normal_comment() {
    Comment comment = Comment.create(9301L, TargetType.POST, 9101L, 9201L, null, "Top level", NOW);

    commentRepository.createComment(comment);

    assertThat(commentRepository.getComment(9301L))
        .hasValueSatisfying(
            persisted -> {
              assertThat(persisted.getTargetType()).isEqualTo(TargetType.POST);
              assertThat(persisted.getTargetId()).isEqualTo(9101L);
              assertThat(persisted.getAccountId()).isEqualTo(9201L);
              assertThat(persisted.getContent()).isEqualTo("Top level");
              assertThat(persisted.getStatus()).isEqualTo(Status.NORMAL);
            });
  }

  @Test
  @DisplayName("CommentRepository: should update comment and hide archived comment")
  void should_update_comment_and_hide_archived_comment() {
    Comment comment = Comment.create(9302L, TargetType.POST, 9101L, 9201L, null, "Original", NOW);
    commentRepository.createComment(comment);
    comment.edit("Updated", NOW.plusSeconds(60));
    commentRepository.updateComment(comment);

    assertThat(commentRepository.getComment(9302L))
        .hasValueSatisfying(
            persisted -> {
              assertThat(persisted.getContent()).isEqualTo("Updated");
              assertThat(persisted.getUpdatedAt()).isEqualTo(NOW.plusSeconds(60));
            });

    comment.archive(NOW.plusSeconds(120));
    commentRepository.updateComment(comment);

    assertThat(commentRepository.getComment(9302L)).isEmpty();
  }

  @Test
  @DisplayName("CommentQueryRepository: should filter by target, normal status, and order by id")
  void should_filter_comments_by_target_status_and_order_by_id() {
    insertComment(9303L, "post", 9101L, 9201L, null, "Second", "normal", NOW.plusSeconds(2), null);
    insertComment(9301L, "post", 9101L, 9201L, null, "First", "normal", NOW, null);
    insertComment(9304L, "post", 9101L, 9202L, null, "Archived", "archived", NOW, null);
    insertComment(9305L, "post", 9102L, 9202L, null, "Other target", "normal", NOW, null);

    List<CommentWithAuthorVo> comments =
        commentQueryRepository.getCommentsByTarget(TargetType.POST, 9101L);

    assertThat(comments).extracting(CommentWithAuthorVo::id).containsExactly(9301L, 9303L);
    assertThat(comments)
        .extracting(CommentWithAuthorVo::content)
        .containsExactly("First", "Second");
    assertThat(comments).extracting(CommentWithAuthorVo::username).containsOnly("comment_author");
  }

  @Test
  @DisplayName("CommentQueryRepository: should join parent author for replies")
  void should_join_parent_author_for_replies() {
    insertComment(9310L, "post", 9101L, 9201L, null, "Parent", "normal", NOW, null);
    insertComment(9311L, "post", 9101L, 9202L, 9310L, "Reply", "normal", NOW.plusSeconds(1), null);

    List<CommentWithAuthorVo> comments =
        commentQueryRepository.getCommentsByTarget(TargetType.POST, 9101L);

    CommentWithAuthorVo reply = comments.get(1);
    assertThat(reply.id()).isEqualTo(9311L);
    assertThat(reply.username()).isEqualTo("reply_author");
    assertThat(reply.replyToAccountId()).isEqualTo(9201L);
    assertThat(reply.replyToUsername()).isEqualTo("comment_author");
    assertThat(reply.replyToNickname()).isEqualTo("Comment Author");
  }

  @Test
  @DisplayName("CommentQueryRepository: should return empty list for empty ids")
  void should_return_empty_list_for_empty_comment_ids() {
    assertThat(commentQueryRepository.getCommentsByIds(List.of())).isEmpty();
  }

  private void insertAccount(Long id, String username, String nickname) {
    jdbcTemplate.update(
        """
        insert into accounts(id, username, nickname, password_hash, status, role, created_at, updated_at)
        values (?, ?, ?, 'hash', 'normal', 'user', ?, ?)
        """,
        id,
        username,
        nickname,
        ts(NOW),
        ts(NOW));
  }

  private void insertComment(
      Long id,
      String targetType,
      Long targetId,
      Long accountId,
      Long parentId,
      String content,
      String status,
      Instant createdAt,
      Instant updatedAt) {
    jdbcTemplate.update(
        """
        insert into comments(id, target_type, target_id, account_id, parent_id, content, status, created_at, updated_at)
        values (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        id,
        targetType,
        targetId,
        accountId,
        parentId,
        content,
        status,
        ts(createdAt),
        ts(updatedAt));
  }

  private Timestamp ts(Instant instant) {
    return instant == null ? null : Timestamp.from(instant);
  }
}
