package io.github.cubelitblade.activity.exception;

import io.github.cubelitblade.common.exception.ApiErrorCode;
import io.github.cubelitblade.common.exception.DomainException;

public class ActivityNotFoundException extends DomainException {
  private ActivityNotFoundException(String detail) {
    super(ApiErrorCode.ACTIVITY_NOT_FOUND, detail);
  }

  public static ActivityNotFoundException notFound() {
    return new ActivityNotFoundException("Activity not found");
  }
}
