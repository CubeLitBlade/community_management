package io.github.cubelitblade.event.model.payload;

public record NotificationDeliveryEventPayload(Long notificationId, Long recipientAccountId)
    implements EventPayload {}
