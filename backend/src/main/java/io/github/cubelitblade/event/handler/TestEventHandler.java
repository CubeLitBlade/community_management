package io.github.cubelitblade.event.handler;

import io.github.cubelitblade.entity.Event;
import io.github.cubelitblade.event.payload.TestEventPayload;
import io.github.cubelitblade.service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Slf4j
@Component
public class TestEventHandler extends EventHandler {
    public TestEventHandler(EventService eventService, ObjectMapper objectMapper) {
        super(eventService, objectMapper);
    }

    @Override
    public Event.EventType getEventType() {
        return Event.EventType.TEST_EVENT;
    }

    @Override
    public void handleEvent(Event event) {
        super.handleEvent(event);

        TestEventPayload payload = objectMapper.convertValue(event.getPayload(), TestEventPayload.class);
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
            int nextRetry = currentRetry + 1;
            log.debug("Event #{}] Retry {}/{} scheduled in {} s.",
                    eventId, currentRetry, targetRetries, payload.getRetryDelaySeconds());

            event.setRetryCount(nextRetry);
            event.setNextRunAt(LocalDateTime.now().plusSeconds(payload.getRetryDelaySeconds()));
            event.setStatus(Event.EventStatus.WAITING);
        } else {
            Event.EventStatus finalStatus = payload.getShouldSucceed()
                    ? Event.EventStatus.SUCCEEDED
                    : Event.EventStatus.FAILED;

            event.setStatus(finalStatus);
            event.setNextRunAt(null);

            log.info("[Event #{}] DebugEvent finished with status = {}. ",
                    eventId, finalStatus);
        }

        eventService.updateById(event);
    }

    private void simulateWork(Long eventId, Long executionDelayMilliseconds) {
        try {
            log.debug("Event #{}] Simulating workload for {} ms...", eventId, executionDelayMilliseconds);
            Thread.sleep(executionDelayMilliseconds);
            log.debug("Event #{}] Workload simulation finished.", eventId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Event #{}] Workload simulation was interrupted!", eventId);
        }
    }
}
