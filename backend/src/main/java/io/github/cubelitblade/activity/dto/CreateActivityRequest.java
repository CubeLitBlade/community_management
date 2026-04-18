package io.github.cubelitblade.activity.dto;

import java.time.Instant;

public record CreateActivityRequest(
    String title,
    String description,
    String location,
    Instant registrationDeadline,
    Instant startTime,
    Instant endTime) {}
