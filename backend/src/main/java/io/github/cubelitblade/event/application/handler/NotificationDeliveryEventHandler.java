package io.github.cubelitblade.event.application.handler;

import io.github.cubelitblade.event.model.Event;
import io.github.cubelitblade.event.model.Type;
import io.github.cubelitblade.event.model.payload.EventPayloadMapper;
import io.github.cubelitblade.event.model.payload.NotificationDeliveryEventPayload;
import io.github.cubelitblade.notification.application.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationDeliveryEventHandler
    extends EventHandler<NotificationDeliveryEventPayload> {
  private final EventPayloadMapper eventPayloadMapper;
  private final NotificationService notificationService;

  public NotificationDeliveryEventHandler(
      EventLifecycleManager workflow,
      EventPayloadMapper eventPayloadMapper,
      NotificationService notificationService) {
    super(workflow);
    this.eventPayloadMapper = eventPayloadMapper;
    this.notificationService = notificationService;
  }

  @Override
  public Class<NotificationDeliveryEventPayload> getPayloadType() {
    return NotificationDeliveryEventPayload.class;
  }

  @Override
  public Type getEventType() {
    return Type.NOTIFICATION_DELIVERY;
  }

  @Override
  public void process(Event event) {
    NotificationDeliveryEventPayload payload =
        eventPayloadMapper.fromJsonNode(event.getPayload(), getPayloadType());
    notificationService.emitNotification(payload.notificationId(), payload.recipientAccountId());
  }
}
