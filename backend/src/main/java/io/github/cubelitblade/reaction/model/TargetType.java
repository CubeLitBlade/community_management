package io.github.cubelitblade.reaction.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public enum TargetType {
  POST("post"),
  COMMENT("comment"),
  ACTIVITY("activity");

  private static final Map<String, TargetType> map =
      Arrays.stream(TargetType.values()).collect(Collectors.toMap(TargetType::getValue, v -> v));

  private final String value;

  TargetType(String value) {
    this.value = value;
  }

  public static TargetType from(String value) {
    TargetType targetType = map.get(value);

    if (targetType == null) {
      throw new IllegalArgumentException("Unknown target type: " + value);
    }

    return targetType;
  }
}
