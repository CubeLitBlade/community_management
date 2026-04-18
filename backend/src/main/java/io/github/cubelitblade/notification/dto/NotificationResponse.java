package io.github.cubelitblade.notification.dto;

import io.github.cubelitblade.notification.model.Notification;
import java.time.Instant;

public record NotificationResponse(
    Long id,
    Long recipientAccountId,
    Long actorAccountId,
    String actorDisplayName,
    String type,
    String targetType,
    Long targetId,
    String content,
    String postTitle,
    String postSummary,
    String activityTitle,
    String activitySummary,
    boolean isRead,
    Instant readAt,
    Instant createdAt) {
  public static NotificationResponse from(
      Notification notification,
      String actorDisplayName,
      String postTitle,
      String postSummary,
      String activityTitle,
      String activitySummary) {
    return new NotificationResponse(
        notification.getId(),
        notification.getRecipientAccountId(),
        notification.getActorAccountId(),
        actorDisplayName,
        notification.getType().getValue(),
        notification.getTargetType(),
        notification.getTargetId(),
        notification.getContent(),
        postTitle,
        postSummary,
        activityTitle,
        activitySummary,
        notification.isRead(),
        notification.getReadAt(),
        notification.getCreatedAt());
  }
}
