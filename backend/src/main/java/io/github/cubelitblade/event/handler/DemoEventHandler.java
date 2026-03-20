package io.github.cubelitblade.event.handler;

import io.github.cubelitblade.configuration.RetryConfig;
import io.github.cubelitblade.entity.Event;
import io.github.cubelitblade.event.payload.DemoEventPayload;
import io.github.cubelitblade.service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class DemoEventHandler extends EventHandler {
    public DemoEventHandler(EventService eventService, ObjectMapper objectMapper, RetryConfig retryConfig) {
        super(eventService, objectMapper, retryConfig);
    }

    @Override
    public Event.EventType getEventType() {
        return Event.EventType.DEMO_EVENT;
    }

    @Override
    public void process(Event event) {
        DemoEventPayload payload = objectMapper.convertValue(event.getPayload(), DemoEventPayload.class);
        Long eventId = event.getId();

        //
        log.debug("[Event #{}] DebugEvent begins. payload = {}. ",
                eventId,
                objectMapper.writeValueAsString(payload)
        );

        if (payload.getExecutionDelayMilliseconds() != null && payload.getExecutionDelayMilliseconds() > 0) {
            simulateWork(eventId, payload.getExecutionDelayMilliseconds());
        }

        if (payload.getMessage() != null) {
            log.info("[Event #{}] {}", eventId, payload.getMessage());
        }

        int currentRetry = event.getRetryCount();
        int targetRetries = payload.getRequiredRetries();

        if (targetRetries > currentRetry) {
            this.scheduleRetry(event);
        } else {
            Event.EventStatus finalStatus = payload.getShouldSucceed()
                    ? Event.EventStatus.SUCCEEDED
                    : Event.EventStatus.FAILED;

            event.setStatus(finalStatus);
            event.setNextRunAt(null);

            log.info("[Event #{}] DebugEvent finished with status = {}. ",
                    eventId, finalStatus);
        }
    }

    private void simulateWork(Long eventId, Long executionDelayMilliseconds) {
        try {
            log.debug("[Event #{}] Simulating workload for {} ms...", eventId, executionDelayMilliseconds);
            Thread.sleep(executionDelayMilliseconds);
            log.debug("[Event #{}] Workload simulation finished.", eventId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[Event #{}] Workload simulation was interrupted!", eventId);
        }
    }
}
