package io.github.cubelitblade.event.application.handler;

import io.github.cubelitblade.activity.application.ActivityService;
import io.github.cubelitblade.event.model.Event;
import io.github.cubelitblade.event.model.Type;
import io.github.cubelitblade.event.model.payload.ActivityReminderEventPayload;
import io.github.cubelitblade.event.model.payload.EventPayloadMapper;
import org.springframework.stereotype.Component;

@Component
public class ActivityReminderEventHandler extends EventHandler<ActivityReminderEventPayload> {
  private final EventPayloadMapper eventPayloadMapper;
  private final ActivityService activityService;

  public ActivityReminderEventHandler(
      EventLifecycleManager workflow,
      EventPayloadMapper eventPayloadMapper,
      ActivityService activityService) {
    super(workflow);
    this.eventPayloadMapper = eventPayloadMapper;
    this.activityService = activityService;
  }

  @Override
  public Class<ActivityReminderEventPayload> getPayloadType() {
    return ActivityReminderEventPayload.class;
  }

  @Override
  public Type getEventType() {
    return Type.ACTIVITY_REMINDER;
  }

  @Override
  public void process(Event event) {
    ActivityReminderEventPayload payload =
        eventPayloadMapper.fromJsonNode(event.getPayload(), getPayloadType());
    activityService.sendReminder(payload.activityId());
  }
}
