package io.github.cubelitblade.activity.dto;

import io.github.cubelitblade.activity.model.Activity;
import java.time.Instant;

public record ActivityView(
    Long id,
    Long creatorAccountId,
    String creatorDisplayName,
    String title,
    String description,
    String location,
    Instant registrationDeadline,
    Instant startTime,
    Instant endTime,
    String status,
    Long participantCount,
    boolean viewerRegistered,
    String rejectionReason,
    Instant approvedAt,
    Instant rejectedAt,
    Instant createdAt,
    Instant updatedAt) {
  public static ActivityView from(
      Activity activity, String creatorDisplayName, long participantCount, boolean viewerRegistered) {
    return new ActivityView(
        activity.getId(),
        activity.getCreatorAccountId(),
        creatorDisplayName,
        activity.getTitle(),
        activity.getDescription(),
        activity.getLocation(),
        activity.getRegistrationDeadline(),
        activity.getStartTime(),
        activity.getEndTime(),
        activity.getStatus().getValue(),
        participantCount,
        viewerRegistered,
        activity.getRejectionReason(),
        activity.getApprovedAt(),
        activity.getRejectedAt(),
        activity.getCreatedAt(),
        activity.getUpdatedAt());
  }
}
