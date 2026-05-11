package io.github.cubelitblade.notification.application;

import io.github.cubelitblade.account.persistence.AccountRepository;
import io.github.cubelitblade.activity.model.Activity;
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
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
  private final NotificationRepository notificationRepository;
  private final AccountRepository accountRepository;
  private final ActivityRepository activityRepository;
  private final PostRepository postRepository;
  private final SnowflakeIdGenerator idGenerator;
  private final TimeProvider timeProvider;
  private final EventService eventService;
  private final EventPayloadMapper eventPayloadMapper;
  private final SseService sseService;

  @Transactional(readOnly = true)
  public NotificationListResponse getNotifications(Long accountId) {
    return getNotifications(accountId, NotificationScope.ALL);
  }

  @Transactional(readOnly = true)
  public NotificationListResponse getNotifications(Long accountId, NotificationScope scope) {
    List<NotificationResponse> notifications =
        notificationRepository
            .findByRecipientAccountIdAndTypes(accountId, scope.typeValues())
            .stream()
            .map(NotificationPo::toNotification)
            .map(this::toResponse)
            .toList();

    return new NotificationListResponse(notifications);
  }

  @Transactional(readOnly = true)
  public NotificationUnreadCountResponse getUnreadCount(Long accountId) {
    return getUnreadCount(accountId, NotificationScope.ALL);
  }

  @Transactional(readOnly = true)
  public NotificationUnreadCountResponse getUnreadCount(Long accountId, NotificationScope scope) {
    return new NotificationUnreadCountResponse(
        notificationRepository.countUnreadByRecipientAccountIdAndTypes(
            accountId, scope.typeValues()));
  }

  @Transactional
  public void markRead(Long accountId, Long notificationId) {
    Notification notification =
        notificationRepository
            .findOwnedById(notificationId, accountId)
            .map(NotificationPo::toNotification)
            .orElseThrow(NotificationNotFoundException::notFound);

    notification.markRead(timeProvider.now());
    notificationRepository.updateReadStatus(NotificationPo.of(notification));
  }

  @Transactional
  public void markAllRead(Long accountId) {
    notificationRepository.markAllRead(accountId, timeProvider.now());
  }

  public SseEmitter subscribe(Long accountId) {
    return sseService.subscribe(accountId);
  }

  public void notifyPostComment(Comment comment) {
    if (!io.github.cubelitblade.comment.model.TargetType.POST.equals(comment.getTargetType())) {
      return;
    }

    postRepository
        .getPost(comment.getTargetId())
        .map(Post::getAuthorId)
        .filter(recipientAccountId -> !recipientAccountId.equals(comment.getAccountId()))
        .ifPresent(
            recipientAccountId ->
                createAndQueueNotification(
                    recipientAccountId,
                    comment.getAccountId(),
                    NotificationType.POST_COMMENT,
                    comment.getTargetType().getValue(),
                    comment.getTargetId(),
                    comment.getContent()));
  }

  public void notifyPostReaction(Reaction reaction) {
    if (!io.github.cubelitblade.reaction.model.TargetType.POST.equals(reaction.getTargetType())) {
      return;
    }

    postRepository
        .getPost(reaction.getTargetId())
        .map(Post::getAuthorId)
        .filter(recipientAccountId -> !recipientAccountId.equals(reaction.getAccountId()))
        .ifPresent(
            recipientAccountId ->
                createAndQueueNotification(
                    recipientAccountId,
                    reaction.getAccountId(),
                    NotificationType.POST_REACTION,
                    reaction.getTargetType().getValue(),
                    reaction.getTargetId(),
                    reaction.getReactionType().getValue()));
  }

  public void notifyActivityApproved(Activity activity, Long actorAccountId) {
    createAndQueueNotification(
        activity.getCreatorAccountId(),
        actorAccountId,
        NotificationType.ACTIVITY_UPDATE,
        "activity",
        activity.getId(),
        "你的活动已通过审核");
  }

  public void notifyActivityRejected(Activity activity, Long actorAccountId) {
    String content =
        activity.getRejectionReason() == null
            ? "你的活动未通过审核"
            : "你的活动未通过审核：" + activity.getRejectionReason();
    createAndQueueNotification(
        activity.getCreatorAccountId(),
        actorAccountId,
        NotificationType.ACTIVITY_UPDATE,
        "activity",
        activity.getId(),
        content);
  }

  public void notifyActivityReminder(Activity activity, List<Long> recipientAccountIds) {
    for (Long recipientAccountId : recipientAccountIds) {
      createAndQueueNotification(
          recipientAccountId,
          activity.getCreatorAccountId(),
          NotificationType.ACTIVITY_REMINDER,
          "activity",
          activity.getId(),
          "你报名的活动即将开始");
    }
  }

  public NotificationResponse findNotification(Long notificationId, Long recipientAccountId) {
    return notificationRepository
        .findOwnedById(notificationId, recipientAccountId)
        .map(NotificationPo::toNotification)
        .map(this::toResponse)
        .orElseThrow(NotificationNotFoundException::notFound);
  }

  @Transactional(readOnly = true)
  public NotificationResponse getNotification(Long notificationId, Long recipientAccountId) {
    return findNotification(notificationId, recipientAccountId);
  }

  public void emitNotification(Long notificationId, Long recipientAccountId) {
    NotificationResponse notification = findNotification(notificationId, recipientAccountId);
    sseService.sendToUser(recipientAccountId, notification);
  }

  private void createAndQueueNotification(
      Long recipientAccountId,
      Long actorAccountId,
      NotificationType type,
      String targetType,
      Long targetId,
      String content) {
    Instant now = timeProvider.now();
    Notification notification =
        Notification.create(
            idGenerator.nextId(),
            recipientAccountId,
            actorAccountId,
            type,
            targetType,
            targetId,
            content,
            now);

    notificationRepository.save(NotificationPo.of(notification));

    NotificationDeliveryEventPayload payload =
        new NotificationDeliveryEventPayload(notification.getId(), recipientAccountId);
    try {
      eventService.createEvent(
          Type.NOTIFICATION_DELIVERY.getValue(), eventPayloadMapper.toJsonNode(payload));
    } catch (RuntimeException e) {
      log.error(
          "Failed to enqueue notification delivery event for notification #{} and recipient #{}.",
          notification.getId(),
          recipientAccountId,
          e);
    }
  }

  private NotificationResponse toResponse(Notification notification) {
    String actorDisplayName =
        accountRepository
            .findAccountById(notification.getActorAccountId())
            .map(
                account -> {
                  String nickname = account.getNickname();
                  if (nickname != null && !nickname.isBlank()) {
                    return nickname;
                  }
                  return account.getUsername() != null ? account.getUsername().value() : null;
                })
            .orElse(null);

    String postTitle = null;
    String postSummary = null;
    String activityTitle = null;
    String activitySummary = null;
    if ("post".equals(notification.getTargetType())) {
      Post post = postRepository.getPost(notification.getTargetId()).orElse(null);
      if (post != null) {
        postTitle = normalize(post.getTitle());
        postSummary = summarizePost(post);
      }
    } else if ("activity".equals(notification.getTargetType())) {
      Activity activity = activityRepository.findById(notification.getTargetId()).orElse(null);
      if (activity != null) {
        activityTitle = normalize(activity.getTitle());
        activitySummary = summarizeActivity(activity);
      }
    }

    return NotificationResponse.from(
        notification, actorDisplayName, postTitle, postSummary, activityTitle, activitySummary);
  }

  private String summarizePost(Post post) {
    String title = normalize(post.getTitle());
    String content = normalize(post.getContent());

    if (content == null) {
      return title;
    }

    if (content.length() > 72) {
      return content.substring(0, 72) + "...";
    }

    return content;
  }

  private String summarizeActivity(Activity activity) {
    String title = normalize(activity.getTitle());
    String location = normalize(activity.getLocation());

    if (location == null) {
      return title;
    }

    return title == null ? location : title + " · " + location;
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }

    String normalized = value.trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
