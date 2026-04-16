package io.github.cubelitblade.event.application.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import io.github.cubelitblade.event.model.Event;
import io.github.cubelitblade.event.model.Type;
import io.github.cubelitblade.event.model.payload.EventPayloadMapper;
import io.github.cubelitblade.event.model.payload.NotificationDeliveryEventPayload;
import io.github.cubelitblade.notification.application.NotificationService;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.node.JsonNodeFactory;

@ExtendWith(MockitoExtension.class)
class NotificationDeliveryEventHandlerTest {
  @Mock private EventLifecycleManager workflow;
  @Mock private EventPayloadMapper eventPayloadMapper;
  @Mock private NotificationService notificationService;

  private NotificationDeliveryEventHandler handler;
  private Event event;

  @BeforeEach
  void setUp() {
    handler =
        new NotificationDeliveryEventHandler(workflow, eventPayloadMapper, notificationService);
    event =
        Event.create(
            1L, Type.NOTIFICATION_DELIVERY, JsonNodeFactory.instance.objectNode(), Instant.now());
    event.run(Instant.now());
  }

  @Test
  @DisplayName("Process: should load payload and emit notification")
  void should_emit_notification() {
    given(eventPayloadMapper.fromJsonNode(any(), eq(NotificationDeliveryEventPayload.class)))
        .willReturn(new NotificationDeliveryEventPayload(10L, 20L));

    handler.process(event);

    verify(notificationService).emitNotification(10L, 20L);
  }
}
