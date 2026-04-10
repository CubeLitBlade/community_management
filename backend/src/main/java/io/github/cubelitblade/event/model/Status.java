package io.github.cubelitblade.event.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum Status {
  WAITING("waiting"),
  PENDING("pending"),
  RUNNING("running"),
  SUCCEEDED("succeeded"),
  FAILED("failed"),
  DEAD("dead");

  @EnumValue private final String status;

  Status(String status) {
    this.status = status;
  }
}
