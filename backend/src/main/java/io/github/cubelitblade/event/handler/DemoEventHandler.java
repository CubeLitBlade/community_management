package io.github.cubelitblade.event.handler;

import io.github.cubelitblade.event.Event;
import io.github.cubelitblade.event.payload.DemoEventPayload;
import io.github.cubelitblade.event.sse.SseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

        log.debug("[Event #{}] DemoEvent begins. payload = {}. ",
                eventId,
                payloadToString(payload)
        );

        // simulate time-consuming tasks
        if (payload.getExecutionDelayMilliseconds() != null && payload.getExecutionDelayMilliseconds() > 0) {
            doTimeConsumingWork(eventId, payload.getExecutionDelayMilliseconds());
        }

        // broadcast message
        if (payload.getMessage() != null) {
            log.debug("[Event #{}] {}", eventId, payload.getMessage());
            sseService.broadcast(payload.getMessage());
        }

        // use exceptions to allow the parent class to retry (if necessary)
        int currentRetry = event.getRetryCount();
        int targetRetries = payload.getRequiredRetries();
        if (targetRetries > currentRetry) {
            throw new RuntimeException(String.format("The required number of repetitions has not been reached (%d/%d).", currentRetry, targetRetries));
        }

        // determine the result of the event
        if (payload.getShouldSucceed()) {
            success(event);
        } else {
            fail(event, "The payload indicates this event to fail. ");
        }
        log.info("[Event #{}] DemoEvent finished with status = {}. ", eventId, event.getStatus());
    }

    private void doTimeConsumingWork(Long eventId, Long executionDelayMilliseconds) {
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
