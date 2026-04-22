package io.github.cubelitblade.notification.persistence;

import io.github.cubelitblade.notification.model.Notification;
import io.github.cubelitblade.notification.model.NotificationType;
import java.time.Instant;
import lombok.Builder;

@Builder
public record NotificationPo(
    Long id,
    Long recipientAccountId,
    Long actorAccountId,
    String type,
    String targetType,
    Long targetId,
    String content,
    Boolean isRead,
    Instant readAt,
    Instant createdAt) {
  public static NotificationPo of(Notification notification) {
    if (notification == null) {
      return null;
    }

    return NotificationPo.builder()
        .id(notification.getId())
        .recipientAccountId(notification.getRecipientAccountId())
        .actorAccountId(notification.getActorAccountId())
        .type(notification.getType().getValue())
        .targetType(notification.getTargetType())
        .targetId(notification.getTargetId())
        .content(notification.getContent())
        .isRead(notification.isRead())
        .readAt(notification.getReadAt())
        .createdAt(notification.getCreatedAt())
        .build();
  }

  public Notification toNotification() {
    Notification.Snapshot snapshot =
        Notification.Snapshot.builder()
            .id(id)
            .recipientAccountId(recipientAccountId)
            .actorAccountId(actorAccountId)
            .type(NotificationType.from(type))
            .targetType(targetType)
            .targetId(targetId)
            .content(content)
            .read(Boolean.TRUE.equals(isRead))
            .readAt(readAt)
            .createdAt(createdAt)
            .build();

    return Notification.reconstitute(snapshot);
  }
}
