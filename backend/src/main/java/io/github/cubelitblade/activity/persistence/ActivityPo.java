package io.github.cubelitblade.activity.persistence;

import io.github.cubelitblade.activity.model.Activity;
import io.github.cubelitblade.activity.model.ActivityStatus;
import java.time.Instant;
import lombok.Builder;

@Builder
public record ActivityPo(
    Long id,
    Long creatorAccountId,
    String title,
    String description,
    String location,
    Instant registrationDeadline,
    Instant startTime,
    Instant endTime,
    String status,
    Long approvedBy,
    Instant approvedAt,
    Long rejectedBy,
    Instant rejectedAt,
    String rejectionReason,
    Instant createdAt,
    Instant updatedAt) {
  public static ActivityPo of(Activity activity) {
    if (activity == null) {
      return null;
    }

    return ActivityPo.builder()
        .id(activity.getId())
        .creatorAccountId(activity.getCreatorAccountId())
        .title(activity.getTitle())
        .description(activity.getDescription())
        .location(activity.getLocation())
        .registrationDeadline(activity.getRegistrationDeadline())
        .startTime(activity.getStartTime())
        .endTime(activity.getEndTime())
        .status(activity.getStatus().getValue())
        .approvedBy(activity.getApprovedBy())
        .approvedAt(activity.getApprovedAt())
        .rejectedBy(activity.getRejectedBy())
        .rejectedAt(activity.getRejectedAt())
        .rejectionReason(activity.getRejectionReason())
        .createdAt(activity.getCreatedAt())
        .updatedAt(activity.getUpdatedAt())
        .build();
  }

  public Activity toActivity() {
    return Activity.reconstitute(
        Activity.Snapshot.builder()
            .id(id)
            .creatorAccountId(creatorAccountId)
            .title(title)
            .description(description)
            .location(location)
            .registrationDeadline(registrationDeadline)
            .startTime(startTime)
            .endTime(endTime)
            .status(ActivityStatus.from(status))
            .approvedBy(approvedBy)
            .approvedAt(approvedAt)
            .rejectedBy(rejectedBy)
            .rejectedAt(rejectedAt)
            .rejectionReason(rejectionReason)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build());
  }
}
