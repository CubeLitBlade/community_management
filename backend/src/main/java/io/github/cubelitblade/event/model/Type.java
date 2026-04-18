package io.github.cubelitblade.event.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public enum Type {
  EVENT("event"),
  DEMO_EVENT("demo"),
  NOTIFICATION_DELIVERY("notification_delivery"),
  ACTIVITY_REMINDER("activity_reminder");

  private static final Map<String, Type> map =
      Arrays.stream(Type.values()).collect(Collectors.toMap(Type::getValue, v -> v));

  private final String value;

  Type(String value) {
    this.value = value;
  }

  public static Type from(String type) {
    Type eventType = map.get(type);
    if (eventType == null) {
      throw new IllegalArgumentException("Unknown event type: " + type);
    }
    return eventType;
  }
}
