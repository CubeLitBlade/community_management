package io.github.cubelitblade.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import io.github.cubelitblade.comment.model.Comment;
import io.github.cubelitblade.common.id.SnowflakeIdGenerator;
import io.github.cubelitblade.common.time.TimeProvider;
import io.github.cubelitblade.event.application.EventService;
import io.github.cubelitblade.event.infra.sse.SseService;
import io.github.cubelitblade.event.model.Type;
import io.github.cubelitblade.event.model.payload.EventPayloadMapper;
import io.github.cubelitblade.event.model.payload.NotificationDeliveryEventPayload;
import io.github.cubelitblade.notification.dto.NotificationListResponse;
import io.github.cubelitblade.notification.dto.NotificationUnreadCountResponse;
import io.github.cubelitblade.notification.exception.NotificationNotFoundException;
import io.github.cubelitblade.notification.model.Notification;
import io.github.cubelitblade.notification.model.NotificationType;
import io.github.cubelitblade.notification.persistence.NotificationPo;
import io.github.cubelitblade.notification.persistence.NotificationRepository;
import io.github.cubelitblade.post.model.Post;
import io.github.cubelitblade.post.persistence.PostRepository;
import io.github.cubelitblade.reaction.model.Reaction;
import io.github.cubelitblade.reaction.model.ReactionType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.node.JsonNodeFactory;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
  private static final Instant NOW = Instant.parse("2026-04-15T08:00:00Z");

  @Mock private NotificationRepository notificationRepository;
  @Mock private PostRepository postRepository;
  @Mock private SnowflakeIdGenerator idGenerator;
  @Mock private TimeProvider timeProvider;
  @Mock private EventService eventService;
  @Mock private EventPayloadMapper eventPayloadMapper;
  @Mock private SseService sseService;

  private NotificationService notificationService;

  @BeforeEach
  void setUp() {
    notificationService =
        new NotificationService(
            notificationRepository,
            postRepository,
            idGenerator,
            timeProvider,
            eventService,
            eventPayloadMapper,
            sseService);
    lenient().when(timeProvider.now()).thenReturn(NOW);
  }

  @Test
  @DisplayName("Create: should create comment notification and queue delivery event")
  void should_create_comment_notification_and_queue_delivery_event() {
    Comment comment =
        Comment.create(
            10L, io.github.cubelitblade.comment.model.TargetType.POST, 55L, 2L, null, "hello", NOW);
    Post post =
        Post.reconstitute(Post.Snapshot.builder().id(55L).authorId(1L).content("post").build());
    given(postRepository.getPost(55L)).willReturn(Optional.of(post));
    given(idGenerator.nextId()).willReturn(100L);
    given(eventPayloadMapper.toJsonNode(any(NotificationDeliveryEventPayload.class)))
        .willReturn(JsonNodeFactory.instance.objectNode());

    notificationService.notifyPostComment(comment);

    ArgumentCaptor<NotificationPo> notificationCaptor =
        ArgumentCaptor.forClass(NotificationPo.class);
    verify(notificationRepository).save(notificationCaptor.capture());
    assertThat(notificationCaptor.getValue().recipientAccountId()).isEqualTo(1L);
    assertThat(notificationCaptor.getValue().actorAccountId()).isEqualTo(2L);
    assertThat(notificationCaptor.getValue().type())
        .isEqualTo(NotificationType.POST_COMMENT.getValue());

    verify(eventService).createEvent(eq(Type.NOTIFICATION_DELIVERY.getValue()), any());
  }

  @Test
  @DisplayName("Create: should not create self notification for comment")
  void should_not_create_self_comment_notification() {
    Comment comment =
        Comment.create(
            10L, io.github.cubelitblade.comment.model.TargetType.POST, 55L, 1L, null, "hello", NOW);
    Post post =
        Post.reconstitute(Post.Snapshot.builder().id(55L).authorId(1L).content("post").build());
    given(postRepository.getPost(55L)).willReturn(Optional.of(post));

    notificationService.notifyPostComment(comment);

    verifyNoInteractions(notificationRepository, eventService);
  }

  @Test
  @DisplayName("Create: should create reaction notification and queue delivery event")
  void should_create_reaction_notification_and_queue_delivery_event() {
    Reaction reaction =
        Reaction.create(
            21L,
            2L,
            io.github.cubelitblade.reaction.model.TargetType.POST,
            88L,
            ReactionType.LIKE,
            NOW);
    Post post =
        Post.reconstitute(Post.Snapshot.builder().id(88L).authorId(1L).content("post").build());
    given(postRepository.getPost(88L)).willReturn(Optional.of(post));
    given(idGenerator.nextId()).willReturn(101L);
    given(eventPayloadMapper.toJsonNode(any(NotificationDeliveryEventPayload.class)))
        .willReturn(JsonNodeFactory.instance.objectNode());

    notificationService.notifyPostReaction(reaction);

    ArgumentCaptor<NotificationPo> notificationCaptor =
        ArgumentCaptor.forClass(NotificationPo.class);
    verify(notificationRepository).save(notificationCaptor.capture());
    assertThat(notificationCaptor.getValue().recipientAccountId()).isEqualTo(1L);
    assertThat(notificationCaptor.getValue().actorAccountId()).isEqualTo(2L);
    assertThat(notificationCaptor.getValue().type())
        .isEqualTo(NotificationType.POST_REACTION.getValue());
    verify(eventService).createEvent(eq(Type.NOTIFICATION_DELIVERY.getValue()), any());
  }

  @Test
  @DisplayName("List: should return only mapped notifications")
  void should_return_notification_list() {
    Notification notification =
        Notification.create(
            100L, 1L, 2L, NotificationType.POST_COMMENT, "post", 55L, "New comment", NOW);
    given(notificationRepository.findByRecipientAccountId(1L))
        .willReturn(List.of(NotificationPo.of(notification)));

    NotificationListResponse response = notificationService.getNotifications(1L);

    assertThat(response.notifications()).hasSize(1);
    assertThat(response.notifications().getFirst().id()).isEqualTo(100L);
    assertThat(response.notifications().getFirst().isRead()).isFalse();
  }

  @Test
  @DisplayName("Unread count: should return unread count for account")
  void should_return_unread_count() {
    given(notificationRepository.countUnreadByRecipientAccountId(1L)).willReturn(3L);

    NotificationUnreadCountResponse response = notificationService.getUnreadCount(1L);

    assertThat(response.count()).isEqualTo(3L);
  }

  @Test
  @DisplayName("Mark read: should update owned notification")
  void should_mark_owned_notification_read() {
    Notification notification =
        Notification.create(
            100L, 1L, 2L, NotificationType.POST_COMMENT, "post", 55L, "New comment", NOW);
    given(notificationRepository.findOwnedById(100L, 1L))
        .willReturn(Optional.of(NotificationPo.of(notification)));

    notificationService.markRead(1L, 100L);

    ArgumentCaptor<NotificationPo> notificationCaptor =
        ArgumentCaptor.forClass(NotificationPo.class);
    verify(notificationRepository).updateReadStatus(notificationCaptor.capture());
    assertThat(notificationCaptor.getValue().isRead()).isTrue();
    assertThat(notificationCaptor.getValue().readAt()).isEqualTo(NOW);
  }

  @Test
  @DisplayName("Mark read: should reject foreign notification")
  void should_reject_foreign_notification_mark_read() {
    given(notificationRepository.findOwnedById(100L, 1L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> notificationService.markRead(1L, 100L))
        .isInstanceOf(NotificationNotFoundException.class);
  }

  @Test
  @DisplayName("Mark all read: should update all unread notifications for account")
  void should_mark_all_read() {
    notificationService.markAllRead(1L);

    verify(notificationRepository).markAllRead(1L, NOW);
  }
}
