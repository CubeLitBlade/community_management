package io.github.cubelitblade.notification.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Notification {
  private Long id;
  private Long recipientAccountId;
  private Long actorAccountId;
  private NotificationType type;
  private String targetType;
  private Long targetId;
  private String content;
  private boolean read;
  private Instant readAt;
  private Instant createdAt;

  public static Notification create(
      Long id,
      Long recipientAccountId,
      Long actorAccountId,
      NotificationType type,
      String targetType,
      Long targetId,
      String content,
      Instant now) {
    Notification notification = new Notification();

    notification.id = id;
    notification.recipientAccountId = recipientAccountId;
    notification.actorAccountId = actorAccountId;
    notification.type = type;
    notification.targetType = targetType;
    notification.targetId = targetId;
    notification.content = content;
    notification.read = false;
    notification.createdAt = now;

    return notification;
  }

  public void markRead(Instant now) {
    if (read) {
      return;
    }

    read = true;
    readAt = now;
  }

  public static Notification reconstitute(Snapshot snapshot) {
    if (snapshot == null) {
      return null;
    }

    Notification notification = new Notification();
    notification.id = snapshot.id;
    notification.recipientAccountId = snapshot.recipientAccountId;
    notification.actorAccountId = snapshot.actorAccountId;
    notification.type = snapshot.type;
    notification.targetType = snapshot.targetType;
    notification.targetId = snapshot.targetId;
    notification.content = snapshot.content;
    notification.read = snapshot.read;
    notification.readAt = snapshot.readAt;
    notification.createdAt = snapshot.createdAt;

    return notification;
  }

  @Builder
  public record Snapshot(
      Long id,
      Long recipientAccountId,
      Long actorAccountId,
      NotificationType type,
      String targetType,
      Long targetId,
      String content,
      boolean read,
      Instant readAt,
      Instant createdAt) {}
}
