package io.github.cubelitblade.activity.exception;

import io.github.cubelitblade.common.exception.ApiErrorCode;
import io.github.cubelitblade.common.exception.DomainException;

public class ActivityForbiddenException extends DomainException {
  private ActivityForbiddenException(String detail) {
    super(ApiErrorCode.ACTIVITY_FORBIDDEN, detail);
  }

  public static ActivityForbiddenException notVisible() {
    return new ActivityForbiddenException("You are not allowed to view this activity");
  }

  public static ActivityForbiddenException moderationRequired() {
    return new ActivityForbiddenException("You are not allowed to moderate activities");
  }
}
