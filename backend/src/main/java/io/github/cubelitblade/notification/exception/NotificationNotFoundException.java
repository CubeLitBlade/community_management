package io.github.cubelitblade.notification.exception;

import io.github.cubelitblade.common.exception.ApiErrorCode;
import io.github.cubelitblade.common.exception.DomainException;

public class NotificationNotFoundException extends DomainException {
  private NotificationNotFoundException(String detail) {
    super(ApiErrorCode.NOTIFICATION_NOT_FOUND, detail);
  }

  public static NotificationNotFoundException notFound() {
    return new NotificationNotFoundException("Notification not found");
  }
}
