package io.github.cubelitblade.event.model;

import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event {
  private Long id;
  private Type type;
  private JsonNode payload;
  private Status status;
  private Integer retryCount;
  private String errorMsg;
  private Instant createdAt;
  private Instant nextRunAt;
  private Instant updatedAt;
  private String currentStep;
  private Integer version;

  public static Event create(Long id, Type type, JsonNode payload, Instant now) {
    Event event = new Event();

    event.id = id;
    event.type = type;
    event.payload = payload;
    event.status = Status.WAITING;
    event.retryCount = 0;
    event.createdAt = now;
    event.nextRunAt = now;
    event.updatedAt = now;
    event.version = 0;

    return event;
  }

  public static Event reconstitute(Snapshot snapshot) {
    if (snapshot == null) return null;

    Event event = new Event();
    event.id = snapshot.id;
    event.type = snapshot.type;
    event.payload = snapshot.payload;
    event.status = snapshot.status;
    event.retryCount = snapshot.retryCount;
    event.errorMsg = snapshot.errorMsg;
    event.createdAt = snapshot.createdAt;
    event.nextRunAt = snapshot.nextRunAt;
    event.updatedAt = snapshot.updatedAt;
    event.currentStep = snapshot.currentStep;
    event.version = snapshot.version;

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

  /**
   * Synchronizes the in-memory version after a successful database update.
   *
   * <p><b>Infrastructure Use Only:</b> Must ONLY be called by {@code EventRepository} upon a
   * successful optimistic lock update.
   */
  public void tick() {
    this.version++;
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

  @Builder
  public record Snapshot(
      Long id,
      Type type,
      JsonNode payload,
      Status status,
      Integer retryCount,
      String errorMsg,
      Instant createdAt,
      Instant nextRunAt,
      Instant updatedAt,
      String currentStep,
      Integer version) {}
}
