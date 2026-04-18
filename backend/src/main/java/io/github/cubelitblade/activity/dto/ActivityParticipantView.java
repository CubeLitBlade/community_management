package io.github.cubelitblade.activity.dto;

import java.time.Instant;

public record ActivityParticipantView(Long accountId, String displayName, Instant registeredAt) {}
