package io.github.cubelitblade.event.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public enum Status {
  WAITING("waiting"),
  PENDING("pending"),
  RUNNING("running"),
  SUCCEEDED("succeeded"),
  FAILED("failed"),
  DEAD("dead");

  private static final Map<String, Status> map =
      Arrays.stream(Status.values()).collect(Collectors.toMap(Status::getValue, v -> v));

  private final String value;

  Status(String value) {
    this.value = value;
  }

  public static Status from(String statusName) {
    Status status = map.get(statusName);
    if (status == null) {
      throw new IllegalArgumentException("Unknown event status: " + statusName);
    }
    return status;
  }
}
