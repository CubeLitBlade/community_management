package io.github.cubelitblade.notification.model;

import java.util.Arrays;
import java.util.List;

public enum NotificationScope {
  ALL,
  REPLIES,
  REACTIONS,
  NOTIFICATIONS;

  public static NotificationScope from(String value) {
    if (value == null || value.isBlank()) {
      return ALL;
    }

    return NotificationScope.valueOf(value.trim().toUpperCase());
  }

  public List<String> typeValues() {
    return switch (this) {
      case ALL -> Arrays.stream(NotificationType.values()).map(NotificationType::getValue).toList();
      case REPLIES -> List.of(NotificationType.POST_COMMENT.getValue());
      case REACTIONS -> List.of(NotificationType.POST_REACTION.getValue());
      case NOTIFICATIONS -> List.of();
    };
  }
}
