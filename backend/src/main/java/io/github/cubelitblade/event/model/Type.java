package io.github.cubelitblade.event.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public enum Type {
  EVENT("event"),
  DEMO_EVENT("demo");

  private static final Map<String, Type> map =
      Arrays.stream(Type.values()).collect(Collectors.toMap(Type::getType, v -> v));
  @EnumValue private final String type;

  Type(String type) {
    this.type = type;
  }

  public static Type from(String type) {
    Type eventType = map.get(type);
    if (eventType == null) {
      throw new IllegalArgumentException("Unknown event type: " + type);
    }
    return eventType;
  }
}
