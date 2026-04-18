package io.github.cubelitblade.activity.model;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Activity {
  private Long id;
  private Long creatorAccountId;
  private String title;
  private String description;
  private String location;
  private Instant registrationDeadline;
  private Instant startTime;
  private Instant endTime;
  private ActivityStatus status;
  private Long approvedBy;
  private Instant approvedAt;
  private Long rejectedBy;
  private Instant rejectedAt;
  private String rejectionReason;
  private Instant createdAt;
  private Instant updatedAt;

  public static Activity create(
      Long id,
      Long creatorAccountId,
      String title,
      String description,
      String location,
      Instant registrationDeadline,
      Instant startTime,
      Instant endTime,
      Instant now) {
    Activity activity = new Activity();
    activity.id = id;
    activity.creatorAccountId = creatorAccountId;
    activity.title = title;
    activity.description = description;
    activity.location = location;
    activity.registrationDeadline = registrationDeadline;
    activity.startTime = startTime;
    activity.endTime = endTime;
    activity.status = ActivityStatus.PENDING;
    activity.createdAt = now;
    activity.updatedAt = now;
    return activity;
  }

  public void approve(Long moderatorAccountId, Instant now) {
    this.status = ActivityStatus.APPROVED;
    this.approvedBy = moderatorAccountId;
    this.approvedAt = now;
    this.rejectedBy = null;
    this.rejectedAt = null;
    this.rejectionReason = null;
    this.updatedAt = now;
  }

  public void reject(Long moderatorAccountId, String reason, Instant now) {
    this.status = ActivityStatus.REJECTED;
    this.rejectedBy = moderatorAccountId;
    this.rejectedAt = now;
    this.rejectionReason = reason;
    this.approvedBy = null;
    this.approvedAt = null;
    this.updatedAt = now;
  }

  public static Activity reconstitute(Snapshot snapshot) {
    if (snapshot == null) {
      return null;
    }

    Activity activity = new Activity();
    activity.id = snapshot.id;
    activity.creatorAccountId = snapshot.creatorAccountId;
    activity.title = snapshot.title;
    activity.description = snapshot.description;
    activity.location = snapshot.location;
    activity.registrationDeadline = snapshot.registrationDeadline;
    activity.startTime = snapshot.startTime;
    activity.endTime = snapshot.endTime;
    activity.status = snapshot.status;
    activity.approvedBy = snapshot.approvedBy;
    activity.approvedAt = snapshot.approvedAt;
    activity.rejectedBy = snapshot.rejectedBy;
    activity.rejectedAt = snapshot.rejectedAt;
    activity.rejectionReason = snapshot.rejectionReason;
    activity.createdAt = snapshot.createdAt;
    activity.updatedAt = snapshot.updatedAt;
    return activity;
  }

  @Builder
  public record Snapshot(
      Long id,
      Long creatorAccountId,
      String title,
      String description,
      String location,
      Instant registrationDeadline,
      Instant startTime,
      Instant endTime,
      ActivityStatus status,
      Long approvedBy,
      Instant approvedAt,
      Long rejectedBy,
      Instant rejectedAt,
      String rejectionReason,
      Instant createdAt,
      Instant updatedAt) {}
}
