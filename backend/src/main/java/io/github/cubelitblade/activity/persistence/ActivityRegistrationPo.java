package io.github.cubelitblade.activity.persistence;

import java.time.Instant;
import lombok.Builder;

@Builder
public record ActivityRegistrationPo(Long activityId, Long accountId, Instant createdAt) {}
