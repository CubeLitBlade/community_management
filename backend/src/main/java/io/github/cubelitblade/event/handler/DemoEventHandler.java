package io.github.cubelitblade.event.handler;

import io.github.cubelitblade.event.sse.SseService;
import org.springframework.stereotype.Component;

import io.github.cubelitblade.event.Event;
import io.github.cubelitblade.event.payload.DemoEventPayload;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DemoEventHandler extends EventHandler<DemoEventPayload> {
    private final SseService sseService;

    public DemoEventHandler(EventHandlerContext context, SseService sseService) {
        super(context);
        this.sseService = sseService;
    }

    @Override
    public Event.EventType getEventType() {
        return Event.EventType.DEMO_EVENT;
    }

    @Override
    public Class<DemoEventPayload> getPayloadType() {
        return DemoEventPayload.class;
    }

    @Override
    public void process(Event event) {
        DemoEventPayload payload = parsePayload(event);
        Long eventId = event.getId();

        log.debug("[Event #{}] DebugEvent begins. payload = {}. ",
                eventId,
                payloadToString(payload)
        );

        if (payload.getExecutionDelayMilliseconds() != null && payload.getExecutionDelayMilliseconds() > 0) {
            simulateWork(eventId, payload.getExecutionDelayMilliseconds());
        }

        if (payload.getMessage() != null) {
            log.debug("[Event #{}] {}", eventId, payload.getMessage());
            sseService.broadcast(payload.getMessage());
        }

        int currentRetry = event.getRetryCount();
        int targetRetries = payload.getRequiredRetries();

        if (targetRetries > currentRetry) {
            this.scheduleRetry(event);
        } else {
            if (payload.getShouldSucceed()) {
                success(event);
            } else {
                fail(event, "The payload indicates this event to fail. ");
            }

            log.info("[Event #{}] DebugEvent finished with status = {}. ",
                    eventId, event.getStatus());
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
