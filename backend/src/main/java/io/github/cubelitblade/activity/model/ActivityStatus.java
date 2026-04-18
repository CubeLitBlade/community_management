package io.github.cubelitblade.activity.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public enum ActivityStatus {
  PENDING("pending"),
  APPROVED("approved"),
  REJECTED("rejected"),
  ARCHIVED("archived");

  private static final Map<String, ActivityStatus> map =
      Arrays.stream(ActivityStatus.values())
          .collect(Collectors.toMap(ActivityStatus::getValue, value -> value));

  private final String value;

  ActivityStatus(String value) {
    this.value = value;
  }

  public static ActivityStatus from(String value) {
    ActivityStatus status = map.get(value);
    if (status == null) {
      throw new IllegalArgumentException("Unknown activity status: " + value);
    }

    return status;
  }
}
