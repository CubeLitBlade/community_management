package io.github.cubelitblade.comment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.github.cubelitblade.account.model.Role;
import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.comment.dto.CreateCommentRequest;
import io.github.cubelitblade.comment.model.Comment;
import io.github.cubelitblade.comment.model.TargetType;
import io.github.cubelitblade.comment.persistence.CommentQueryRepository;
import io.github.cubelitblade.comment.persistence.CommentRepository;
import io.github.cubelitblade.comment.persistence.query.CommentWithAuthorVo;
import io.github.cubelitblade.common.exception.ValidationException;
import io.github.cubelitblade.common.id.SnowflakeIdGenerator;
import io.github.cubelitblade.common.time.TimeProvider;
import io.github.cubelitblade.notification.application.NotificationService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
  private static final Instant NOW = Instant.parse("2026-04-20T08:00:00Z");

  @Mock private CommentRepository commentRepository;
  @Mock private CommentQueryRepository commentQueryRepository;
  @Mock private SnowflakeIdGenerator idGenerator;
  @Mock private TimeProvider timeProvider;
  @Mock private NotificationService notificationService;

  private CommentService commentService;

  @BeforeEach
  void setUp() {
    commentService =
        new CommentService(
            commentRepository,
            commentQueryRepository,
            idGenerator,
            timeProvider,
            notificationService);
  }

  @Test
  @DisplayName("Create: should persist comment and notify post author")
  void should_create_comment_and_notify() {
    given(idGenerator.nextId()).willReturn(99L);
    given(timeProvider.now()).willReturn(NOW);

    Long commentId =
        commentService.createComment(
            new JwtAuthenticatedUser(7L, Role.USER),
            new CreateCommentRequest("post", 101L, 55L, "Nice post"));

    ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
    verify(commentRepository).createComment(captor.capture());
    verify(notificationService).notifyPostComment(captor.getValue());
    assertThat(commentId).isEqualTo(99L);
    assertThat(captor.getValue().getTargetType()).isEqualTo(TargetType.POST);
    assertThat(captor.getValue().getTargetId()).isEqualTo(101L);
    assertThat(captor.getValue().getAccountId()).isEqualTo(7L);
    assertThat(captor.getValue().getParentId()).isEqualTo(55L);
  }

  @Test
  @DisplayName("Create: should reject missing target type")
  void should_reject_missing_target_type() {
    assertThatThrownBy(
            () ->
                commentService.createComment(
                    new JwtAuthenticatedUser(7L, Role.USER),
                    new CreateCommentRequest(" ", 101L, null, "Nice post")))
        .isInstanceOf(ValidationException.class);

    verify(commentRepository, never()).createComment(any());
    verify(notificationService, never()).notifyPostComment(any());
  }

  @Test
  @DisplayName("Create: should reject missing target id")
  void should_reject_missing_target_id() {
    assertThatThrownBy(
            () ->
                commentService.createComment(
                    new JwtAuthenticatedUser(7L, Role.USER),
                    new CreateCommentRequest("post", null, null, "Nice post")))
        .isInstanceOf(ValidationException.class);

    verify(commentRepository, never()).createComment(any());
    verify(notificationService, never()).notifyPostComment(any());
  }

  @Test
  @DisplayName("Create: should reject blank content")
  void should_reject_blank_content() {
    assertThatThrownBy(
            () ->
                commentService.createComment(
                    new JwtAuthenticatedUser(7L, Role.USER),
                    new CreateCommentRequest("post", 101L, null, " ")))
        .isInstanceOf(ValidationException.class);

    verify(commentRepository, never()).createComment(any());
    verify(notificationService, never()).notifyPostComment(any());
  }

  @Test
  @DisplayName("Create: should reject unknown target type")
  void should_reject_unknown_target_type() {
    assertThatThrownBy(
            () ->
                commentService.createComment(
                    new JwtAuthenticatedUser(7L, Role.USER),
                    new CreateCommentRequest("unknown", 101L, null, "Nice post")))
        .isInstanceOf(ValidationException.class);

    verify(commentRepository, never()).createComment(any());
    verify(notificationService, never()).notifyPostComment(any());
  }

  @Test
  @DisplayName("List: should return comments mapped from query repository")
  void should_list_comments() {
    given(commentQueryRepository.getCommentsByTarget(TargetType.POST, 101L))
        .willReturn(
            List.of(
                new CommentWithAuthorVo(
                    1L,
                    "post",
                    101L,
                    7L,
                    null,
                    "Top level",
                    "normal",
                    NOW.minusSeconds(30),
                    null,
                    "alice",
                    "Alice",
                    null,
                    null,
                    null),
                new CommentWithAuthorVo(
                    2L,
                    "post",
                    101L,
                    8L,
                    1L,
                    "Reply",
                    "normal",
                    NOW.minusSeconds(10),
                    null,
                    "bob",
                    "Bob",
                    7L,
                    "alice",
                    "Alice")));

    var response = commentService.getComments("post", 101L);

    assertThat(response.items()).hasSize(2);
    assertThat(response.items().getFirst().authorNickname()).isEqualTo("Alice");
    assertThat(response.items().get(1).replyToAccountId()).isEqualTo(7L);
    assertThat(response.items().get(1).parentId()).isEqualTo(1L);
  }
}
