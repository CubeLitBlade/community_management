package io.github.cubelitblade.event.handler;

import io.github.cubelitblade.common.exception.TransientEventException;
import io.github.cubelitblade.event.Event;
import io.github.cubelitblade.event.payload.DemoEventPayload;
import io.github.cubelitblade.event.payload.EventPayloadMapper;
import io.github.cubelitblade.event.sse.SseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Component
public class DemoEventHandler extends EventHandler<DemoEventPayload> {
    // Key steps in the event process. Checkpoints are used only here to ensure idempotency.
    private static final String STEP_INIT = "init";
    private static final String STEP_TIME_CONSUMING_WORK_DONE = "time-consuming-work-done";
    private static final String STEP_TX_VALIDATED = "tx-validated";

    private final SseService sseService;
    private final TransactionTemplate transactionTemplate;
    private final EventPayloadMapper eventPayloadMapper;

    public DemoEventHandler(EventWorkflow workflow, SseService sseService, TransactionTemplate transactionTemplate, EventPayloadMapper eventPayloadMapper) {
        super(workflow);
        this.sseService = sseService;
        this.transactionTemplate = transactionTemplate;
        this.eventPayloadMapper = eventPayloadMapper;
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
        Long eventId = event.getId();
        DemoEventPayload payload = eventPayloadMapper.fromJsonNode(event.getPayload(), getPayloadType());

        // Step 1: Initialize event and perform lightweight tasks
        if (event.getCurrentStep() == null) {
            log.debug("[Event #{}] Starting DemoEvent. Payload = {}", eventId, eventPayloadMapper.toJsonString(payload));

            broadcastMessage(payload);
            workflow.checkpoint(event, STEP_INIT);   // save the progress
        } else {
            log.debug("[Event #{}] Resuming DemoEvent from step '{}'. Payload = {}", eventId, event.getCurrentStep(), eventPayloadMapper.toJsonString(payload));
        }

        // Step 2: Perform heavy, time-consuming work atomically
        if (event.getCurrentStep().equals(STEP_INIT)) {
            doTimeConsumingWork(eventId, payload);
            workflow.checkpoint(event, STEP_TIME_CONSUMING_WORK_DONE);  // save the progress
        }

        // Step 3: Execute transactional operations
        if (event.getCurrentStep().equals(STEP_TIME_CONSUMING_WORK_DONE)) {
            transactionTemplate.executeWithoutResult(_ -> scheduleRetry(event, payload));
            workflow.checkpoint(event, STEP_TX_VALIDATED);   // save the progress
        }

        // Step 4: Finalize the event outcome
        if (event.getCurrentStep().equals(STEP_TX_VALIDATED)) {
            decideResult(event, payload);
        }
        log.info("[Event #{}] DemoEvent completed with status = {}", eventId, event.getStatus());
    }

    /**
     * Simulate heavy work that may take time.
     * <p>
     * Assumptions:
     * - Side effects occur only after successful completion.
     * - Work is idempotent or automatically reversible if interrupted.
     * Otherwise, resuming a partially completed work must fail the event,
     * possibly after restoring the previous state.
     * </p>
     * <p>
     * Checkpoints should be used to persist progress safely and allow idempotent retries.
     * </p>
     *
     * @param eventId Event identifier
     * @param payload Event payload
     */
    private void doTimeConsumingWork(long eventId, DemoEventPayload payload) {
        Long delay = payload.getExecutionDelayMilliseconds();
        if (delay == null || delay <= 0) {
            log.info("[Event #{}] No execution delay specified, skipping time-consuming work.", eventId);
            return;
        }
        try {
            log.debug("[Event #{}] Heavy work begins. The system will simulate a task that takes {} ms...", eventId, delay);
            Thread.sleep(delay);
            log.debug("[Event #{}] Heavy work completed.", eventId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[Event #{}] Heavy work was unexpectedly interrupted! Any side effects are assumed reverted or safe to retry. ", eventId);
            throw new TransientEventException("Simulation of heavy work was interrupted");
        }
    }

    /**
     * Broadcast the message via SSE.
     * <p>
     * Lightweight operation without side effects.
     * Checkpoints are not needed here to avoid unnecessary overhead.
     * </p>
     *
     * @param payload Payload of the event.
     */
    private void broadcastMessage(DemoEventPayload payload) {
        if (payload.getMessage() == null) {
            log.info("No message provided, skipping broadcast.");
            return;
        }
        sseService.broadcast(payload.getMessage());
    }

    /**
     * Schedule a retry for the event if required.
     * <p>
     * This operation may modify the database, so it must run inside a transaction.
     * Checkpoints are required after successful execution to ensure idempotency.
     * </p>
     *
     * @param event   Event.
     * @param payload Payload of the event.
     */
    private void scheduleRetry(Event event, DemoEventPayload payload) {
        int currentRetry = event.getRetryCount();
        int requiredRetries = payload.getRequiredRetries();
        if (requiredRetries > currentRetry) {
            throw new TransientEventException(
                    String.format("Retry threshold not reached (%d/%d).", currentRetry, requiredRetries)
            );
        }
    }

    /**
     * Decide the final outcome of the event based on the payload.
     *
     * @param event   Event.
     * @param payload Payload of the event.
     */
    private void decideResult(Event event, DemoEventPayload payload) {
        if (payload.getShouldSucceed()) {
            workflow.succeed(event);
        } else {
            workflow.fail(event, "Payload indicates that this event should fail.");
        }
    }
}
