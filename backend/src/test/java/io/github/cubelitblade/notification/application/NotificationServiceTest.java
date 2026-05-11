package io.github.cubelitblade.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import io.github.cubelitblade.account.model.Account;
import io.github.cubelitblade.account.model.PasswordHash;
import io.github.cubelitblade.account.model.Role;
import io.github.cubelitblade.account.model.Status;
import io.github.cubelitblade.account.model.Username;
import io.github.cubelitblade.account.persistence.AccountRepository;
import io.github.cubelitblade.activity.model.Activity;
import io.github.cubelitblade.activity.model.ActivityStatus;
import io.github.cubelitblade.activity.persistence.ActivityRepository;
import io.github.cubelitblade.comment.model.Comment;
import io.github.cubelitblade.common.id.SnowflakeIdGenerator;
import io.github.cubelitblade.common.time.TimeProvider;
import io.github.cubelitblade.event.application.EventService;
import io.github.cubelitblade.event.infra.sse.SseService;
import io.github.cubelitblade.event.model.Type;
import io.github.cubelitblade.event.model.payload.EventPayloadMapper;
import io.github.cubelitblade.event.model.payload.NotificationDeliveryEventPayload;
import io.github.cubelitblade.notification.dto.NotificationListResponse;
import io.github.cubelitblade.notification.dto.NotificationResponse;
import io.github.cubelitblade.notification.dto.NotificationUnreadCountResponse;
import io.github.cubelitblade.notification.exception.NotificationNotFoundException;
import io.github.cubelitblade.notification.model.Notification;
import io.github.cubelitblade.notification.model.NotificationScope;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.node.JsonNodeFactory;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
  private static final Instant NOW = Instant.parse("2026-04-15T08:00:00Z");

  @Mock private NotificationRepository notificationRepository;
  @Mock private AccountRepository accountRepository;
  @Mock private ActivityRepository activityRepository;
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
            accountRepository,
            activityRepository,
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
  @DisplayName("Create: should ignore non-post comments and missing posts")
  void should_ignore_non_post_comments_and_missing_posts() {
    Comment activityComment =
        Comment.create(
            11L,
            io.github.cubelitblade.comment.model.TargetType.ACTIVITY,
            55L,
            2L,
            null,
            "hello",
            NOW);
    Comment postComment =
        Comment.create(
            12L, io.github.cubelitblade.comment.model.TargetType.POST, 99L, 2L, null, "hello", NOW);
    given(postRepository.getPost(99L)).willReturn(Optional.empty());

    notificationService.notifyPostComment(activityComment);
    notificationService.notifyPostComment(postComment);

    verify(notificationRepository, never()).save(any());
    verify(eventService, never()).createEvent(eq(Type.NOTIFICATION_DELIVERY.getValue()), any());
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
  @DisplayName("Create: should ignore non-post reactions and self reactions")
  void should_ignore_non_post_or_self_reactions() {
    Reaction activityReaction =
        Reaction.create(
            22L,
            2L,
            io.github.cubelitblade.reaction.model.TargetType.ACTIVITY,
            88L,
            ReactionType.LIKE,
            NOW);
    Reaction selfReaction =
        Reaction.create(
            23L,
            1L,
            io.github.cubelitblade.reaction.model.TargetType.POST,
            89L,
            ReactionType.LIKE,
            NOW);
    Post post =
        Post.reconstitute(Post.Snapshot.builder().id(89L).authorId(1L).content("post").build());
    given(postRepository.getPost(89L)).willReturn(Optional.of(post));

    notificationService.notifyPostReaction(activityReaction);
    notificationService.notifyPostReaction(selfReaction);

    verify(notificationRepository, never()).save(any());
    verify(eventService, never()).createEvent(eq(Type.NOTIFICATION_DELIVERY.getValue()), any());
  }

  @Test
  @DisplayName("Create: should save notification even when delivery event enqueue fails")
  void should_save_notification_when_delivery_event_enqueue_fails() {
    Activity activity = activity(44L, "社区跑步", "体育馆", 1L);
    given(idGenerator.nextId()).willReturn(102L);
    given(eventPayloadMapper.toJsonNode(any(NotificationDeliveryEventPayload.class)))
        .willReturn(JsonNodeFactory.instance.objectNode());
    org.mockito.Mockito.doThrow(new IllegalStateException("queue unavailable"))
        .when(eventService)
        .createEvent(eq(Type.NOTIFICATION_DELIVERY.getValue()), any());

    notificationService.notifyActivityApproved(activity, 9L);

    ArgumentCaptor<NotificationPo> notificationCaptor =
        ArgumentCaptor.forClass(NotificationPo.class);
    verify(notificationRepository).save(notificationCaptor.capture());
    assertThat(notificationCaptor.getValue().id()).isEqualTo(102L);
    assertThat(notificationCaptor.getValue().recipientAccountId()).isEqualTo(1L);
    assertThat(notificationCaptor.getValue().content()).isEqualTo("你的活动已通过审核");
  }

  @Test
  @DisplayName("List: should return only mapped notifications")
  void should_return_notification_list() {
    Notification notification =
        Notification.create(
            100L, 1L, 2L, NotificationType.POST_COMMENT, "post", 55L, "New comment", NOW);
    given(notificationRepository.findByRecipientAccountIdAndTypes(eq(1L), anyList()))
        .willReturn(List.of(NotificationPo.of(notification)));

    NotificationListResponse response = notificationService.getNotifications(1L);

    assertThat(response.notifications()).hasSize(1);
    assertThat(response.notifications().getFirst().id()).isEqualTo(100L);
    assertThat(response.notifications().getFirst().isRead()).isFalse();
  }

  @Test
  @DisplayName("List: should include actor and post context")
  void should_return_notification_with_actor_and_post_context() {
    String longContent = "x".repeat(80);
    Notification notification =
        Notification.create(
            110L, 1L, 2L, NotificationType.POST_COMMENT, "post", 55L, "New comment", NOW);
    Post post =
        Post.reconstitute(
            Post.Snapshot.builder()
                .id(55L)
                .authorId(1L)
                .title("  帖子标题  ")
                .content(longContent)
                .build());
    given(
            notificationRepository.findByRecipientAccountIdAndTypes(
                1L, NotificationScope.ALL.typeValues()))
        .willReturn(List.of(NotificationPo.of(notification)));
    given(accountRepository.findAccountById(2L))
        .willReturn(Optional.of(account(2L, "actor", "演员")));
    given(postRepository.getPost(55L)).willReturn(Optional.of(post));

    NotificationResponse response =
        notificationService.getNotifications(1L).notifications().getFirst();

    assertThat(response.actorDisplayName()).isEqualTo("演员");
    assertThat(response.postTitle()).isEqualTo("帖子标题");
    assertThat(response.postSummary()).hasSize(75).endsWith("...");
  }

  @Test
  @DisplayName("List: should fall back actor display name and summarize untitled post")
  void should_fallback_actor_display_name_and_summarize_untitled_post() {
    Notification notification =
        Notification.create(111L, 1L, 2L, NotificationType.POST_REACTION, "post", 56L, "like", NOW);
    Post post =
        Post.reconstitute(
            Post.Snapshot.builder().id(56L).authorId(1L).title(" ").content("  短内容  ").build());
    given(
            notificationRepository.findByRecipientAccountIdAndTypes(
                1L, NotificationScope.REACTIONS.typeValues()))
        .willReturn(List.of(NotificationPo.of(notification)));
    given(accountRepository.findAccountById(2L)).willReturn(Optional.of(account(2L, "actor", " ")));
    given(postRepository.getPost(56L)).willReturn(Optional.of(post));

    NotificationResponse response =
        notificationService
            .getNotifications(1L, NotificationScope.REACTIONS)
            .notifications()
            .getFirst();

    assertThat(response.actorDisplayName()).isEqualTo("actor");
    assertThat(response.postTitle()).isNull();
    assertThat(response.postSummary()).isEqualTo("短内容");
  }

  @Test
  @DisplayName("Unread count: should return unread count for account")
  void should_return_unread_count() {
    given(notificationRepository.countUnreadByRecipientAccountIdAndTypes(eq(1L), anyList()))
        .willReturn(3L);

    NotificationUnreadCountResponse response = notificationService.getUnreadCount(1L);

    assertThat(response.count()).isEqualTo(3L);
  }

  @Test
  @DisplayName("Unread count: should pass requested scope")
  void should_return_unread_count_for_scope() {
    given(
            notificationRepository.countUnreadByRecipientAccountIdAndTypes(
                1L, NotificationScope.REPLIES.typeValues()))
        .willReturn(2L);

    NotificationUnreadCountResponse response =
        notificationService.getUnreadCount(1L, NotificationScope.REPLIES);

    assertThat(response.count()).isEqualTo(2L);
  }

  @Test
  @DisplayName("List: should include dedicated activity notifications in notifications scope")
  void should_return_activity_notifications_for_notification_scope() {
    Notification notification =
        Notification.create(
            101L, 1L, 9L, NotificationType.ACTIVITY_UPDATE, "activity", 55L, "approved", NOW);
    Activity activity =
        Activity.reconstitute(
            Activity.Snapshot.builder()
                .id(55L)
                .creatorAccountId(1L)
                .title("社区跑步")
                .location("体育馆")
                .build());
    given(notificationRepository.findByRecipientAccountIdAndTypes(eq(1L), anyList()))
        .willReturn(List.of(NotificationPo.of(notification)));
    given(activityRepository.findById(55L)).willReturn(Optional.of(activity));

    NotificationListResponse response =
        notificationService.getNotifications(
            1L, io.github.cubelitblade.notification.model.NotificationScope.NOTIFICATIONS);

    assertThat(response.notifications()).hasSize(1);
    assertThat(response.notifications().getFirst().type())
        .isEqualTo(NotificationType.ACTIVITY_UPDATE.getValue());
    assertThat(response.notifications().getFirst().activityTitle()).isEqualTo("社区跑步");
  }

  @Test
  @DisplayName("List: should summarize activity using title, location, or both")
  void should_summarize_activity_context() {
    Notification titleAndLocation =
        Notification.create(
            120L, 1L, 9L, NotificationType.ACTIVITY_UPDATE, "activity", 60L, "approved", NOW);
    Notification locationOnly =
        Notification.create(
            121L, 1L, 9L, NotificationType.ACTIVITY_REMINDER, "activity", 61L, "reminder", NOW);
    given(
            notificationRepository.findByRecipientAccountIdAndTypes(
                1L, NotificationScope.NOTIFICATIONS.typeValues()))
        .willReturn(List.of(NotificationPo.of(titleAndLocation), NotificationPo.of(locationOnly)));
    given(activityRepository.findById(60L))
        .willReturn(Optional.of(activity(60L, "  社区跑步  ", "  体育馆  ", 1L)));
    given(activityRepository.findById(61L))
        .willReturn(Optional.of(activity(61L, " ", "  操场  ", 1L)));

    NotificationListResponse response =
        notificationService.getNotifications(1L, NotificationScope.NOTIFICATIONS);

    assertThat(response.notifications().get(0).activityTitle()).isEqualTo("社区跑步");
    assertThat(response.notifications().get(0).activitySummary()).isEqualTo("社区跑步 · 体育馆");
    assertThat(response.notifications().get(1).activityTitle()).isNull();
    assertThat(response.notifications().get(1).activitySummary()).isEqualTo("操场");
  }

  @Test
  @DisplayName("Create: should create rejected and reminder activity notifications")
  void should_create_activity_rejected_and_reminder_notifications() {
    Activity rejected =
        Activity.reconstitute(
            Activity.Snapshot.builder()
                .id(70L)
                .creatorAccountId(1L)
                .title("活动")
                .location("操场")
                .rejectionReason("时间冲突")
                .build());
    Activity reminder = activity(71L, "活动", "操场", 8L);
    given(idGenerator.nextId()).willReturn(130L, 131L, 132L);
    given(eventPayloadMapper.toJsonNode(any(NotificationDeliveryEventPayload.class)))
        .willReturn(JsonNodeFactory.instance.objectNode());

    notificationService.notifyActivityRejected(rejected, 9L);
    notificationService.notifyActivityReminder(reminder, List.of(3L, 4L));

    ArgumentCaptor<NotificationPo> notificationCaptor =
        ArgumentCaptor.forClass(NotificationPo.class);
    verify(notificationRepository, org.mockito.Mockito.times(3)).save(notificationCaptor.capture());
    assertThat(notificationCaptor.getAllValues())
        .extracting(NotificationPo::content)
        .containsExactly("你的活动未通过审核：时间冲突", "你报名的活动即将开始", "你报名的活动即将开始");
    assertThat(notificationCaptor.getAllValues())
        .extracting(NotificationPo::recipientAccountId)
        .containsExactly(1L, 3L, 4L);
  }

  @Test
  @DisplayName("Create: should create rejected activity notification without reason")
  void should_create_rejected_activity_notification_without_reason() {
    Activity rejected = activity(72L, "活动", "操场", 1L);
    given(idGenerator.nextId()).willReturn(133L);
    given(eventPayloadMapper.toJsonNode(any(NotificationDeliveryEventPayload.class)))
        .willReturn(JsonNodeFactory.instance.objectNode());

    notificationService.notifyActivityRejected(rejected, 9L);

    ArgumentCaptor<NotificationPo> notificationCaptor =
        ArgumentCaptor.forClass(NotificationPo.class);
    verify(notificationRepository).save(notificationCaptor.capture());
    assertThat(notificationCaptor.getValue().content()).isEqualTo("你的活动未通过审核");
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

  @Test
  @DisplayName("Get notification: should return owned notification or not found")
  void should_get_owned_notification_or_throw() {
    Notification notification =
        Notification.create(
            140L, 1L, 2L, NotificationType.POST_COMMENT, "post", 55L, "New comment", NOW);
    given(notificationRepository.findOwnedById(140L, 1L))
        .willReturn(Optional.of(NotificationPo.of(notification)));
    given(notificationRepository.findOwnedById(404L, 1L)).willReturn(Optional.empty());

    assertThat(notificationService.getNotification(140L, 1L).id()).isEqualTo(140L);
    assertThatThrownBy(() -> notificationService.getNotification(404L, 1L))
        .isInstanceOf(NotificationNotFoundException.class);
  }

  @Test
  @DisplayName("SSE: should subscribe and emit notification")
  void should_subscribe_and_emit_notification() {
    SseEmitter emitter = new SseEmitter();
    Notification notification =
        Notification.create(
            150L, 1L, 2L, NotificationType.POST_COMMENT, "post", 55L, "New comment", NOW);
    given(sseService.subscribe(1L)).willReturn(emitter);
    given(notificationRepository.findOwnedById(150L, 1L))
        .willReturn(Optional.of(NotificationPo.of(notification)));

    assertThat(notificationService.subscribe(1L)).isSameAs(emitter);
    notificationService.emitNotification(150L, 1L);

    verify(sseService).sendToUser(eq(1L), any(NotificationResponse.class));
  }

  private Account account(Long id, String username, String nickname) {
    return Account.reconstitute(
        Account.Snapshot.builder()
            .id(id)
            .username(Username.of(username))
            .passwordHash(new PasswordHash("hashed-" + username))
            .nickname(nickname)
            .role(Role.USER)
            .status(Status.NORMAL)
            .createdAt(NOW)
            .updatedAt(NOW)
            .build());
  }

  private Activity activity(Long id, String title, String location, Long creatorAccountId) {
    return Activity.reconstitute(
        Activity.Snapshot.builder()
            .id(id)
            .creatorAccountId(creatorAccountId)
            .title(title)
            .description("社区活动")
            .location(location)
            .registrationDeadline(NOW.plusSeconds(3600))
            .startTime(NOW.plusSeconds(7200))
            .endTime(NOW.plusSeconds(10800))
            .status(ActivityStatus.APPROVED)
            .createdAt(NOW)
            .updatedAt(NOW)
            .build());
  }
}
