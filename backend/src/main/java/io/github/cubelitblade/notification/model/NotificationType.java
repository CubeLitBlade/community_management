package io.github.cubelitblade.notification.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public enum NotificationType {
  POST_COMMENT("post_comment"),
  POST_REACTION("post_reaction"),
  ACTIVITY_REMINDER("activity_reminder"),
  ACTIVITY_UPDATE("activity_update");

  private static final Map<String, NotificationType> map =
      Arrays.stream(NotificationType.values())
          .collect(Collectors.toMap(NotificationType::getValue, value -> value));

  private final String value;

  NotificationType(String value) {
    this.value = value;
  }

  public static NotificationType from(String value) {
    NotificationType notificationType = map.get(value);
    if (notificationType == null) {
      throw new IllegalArgumentException("Unknown notification type: " + value);
    }

    return notificationType;
  }
}
