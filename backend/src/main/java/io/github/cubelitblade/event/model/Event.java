package io.github.cubelitblade.event.model;

import com.baomidou.mybatisplus.annotation.*;
import io.github.cubelitblade.common.typehandler.JsonbTypeHandler;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

@Slf4j
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@TableName(autoResultMap = true)
public class Event {
  @TableId(type = IdType.AUTO)
  @Setter(AccessLevel.NONE)
  private Long id;

  private Type type;

  @TableField(typeHandler = JsonbTypeHandler.class)
  private JsonNode payload;

  private Status status = Status.WAITING;

  private Integer retryCount = 0;

  private String errorMsg;

  private Instant createdAt;

  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private Instant nextRunAt;

  private Instant updatedAt;

  private String currentStep;

  public static Event create(Type type, JsonNode payload, Clock clock) {
    Instant now = Instant.now(clock);

    Event event = new Event();
    event.type = type;
    event.payload = payload;
    event.status = Status.WAITING;
    event.createdAt = now;
    event.nextRunAt = now;
    event.updatedAt = now;

    return event;
  }

  public static Event create(String type, JsonNode payload, Clock clock) {
    Instant now = Instant.now(clock);

    Event event = new Event();
    event.type = Type.from(type);
    event.payload = payload;
    event.status = Status.WAITING;
    event.createdAt = now;
    event.nextRunAt = now;
    event.updatedAt = now;

    return event;
  }

  public void revive(Instant now, Instant nextRunAt) {
    this.status = Status.WAITING;
    this.errorMsg = null;
    this.nextRunAt = nextRunAt;
    this.retryCount = 0;
    this.touch(now);
  }

  public void succeed(Instant now) {
    requireNonTerminalStatus();

    this.status = Status.SUCCEEDED;
    this.errorMsg = null;
    this.nextRunAt = null;
    this.touch(now);
  }

  public void fail(String reason, Instant now) {
    if (this.status == Status.FAILED) {
      return;
    }

    requireNonTerminalStatus();

    this.status = Status.FAILED;
    this.errorMsg = reason;
    this.nextRunAt = null;
    this.touch(now);
  }

  public void die(String reason, Instant now) {
    if (this.status == Status.DEAD) {
      return;
    }

    requireNonTerminalStatus();

    this.status = Status.DEAD;
    this.errorMsg = reason;
    this.nextRunAt = null;
    this.touch(now);
  }

  public void run(Instant now) {
    requireStatus(Status.WAITING);

    this.status = Status.RUNNING;
    this.nextRunAt = null;
    this.touch(now);
  }

  public void retry(Instant nextRunAt, String reason, Instant now) {
    requireStatus(Status.RUNNING);

    this.status = Status.WAITING;
    retryCount = retryCount + 1;
    this.nextRunAt = nextRunAt;
    this.errorMsg = reason;
    this.currentStep = null; // Clear progress to a clean retry
    this.touch(now);
  }

  public void advanceTo(String currentStep, Instant now) {
    requireStatus(Status.RUNNING);

    this.currentStep = currentStep;
    this.touch(now);
  }

  private void touch(Instant now) {
    this.updatedAt = now;
  }

  private void requireStatus(Status status) {
    if (this.status != status) {
      throw new IllegalStateException(
          buildErrorMessage("Event status expected " + status + ", but was " + this.status));
    }
  }

  private void requireNonTerminalStatus() {
    switch (this.status) {
      case SUCCEEDED, FAILED, DEAD ->
          throw new IllegalStateException(
              buildErrorMessage("Already in terminal status: " + this.status));
    }
  }

  private String buildErrorMessage(String reason) {
    String prefix = "[Event #" + this.id + "] ";
    return prefix + Objects.requireNonNullElse(reason, "Encountered an unexpected error");
  }
}
