package io.github.cubelitblade.event.application.handler;

import io.github.cubelitblade.event.exception.DownstreamTimeoutException;
import io.github.cubelitblade.event.exception.RejectedEventException;
import io.github.cubelitblade.event.infra.sse.SseService;
import io.github.cubelitblade.event.model.Event;
import io.github.cubelitblade.event.model.Type;
import io.github.cubelitblade.event.model.payload.DemoEventPayload;
import io.github.cubelitblade.event.model.payload.EventPayloadMapper;
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

  public DemoEventHandler(
      EventLifecycleManager workflow,
      SseService sseService,
      TransactionTemplate transactionTemplate,
      EventPayloadMapper eventPayloadMapper) {
    super(workflow);
    this.sseService = sseService;
    this.transactionTemplate = transactionTemplate;
    this.eventPayloadMapper = eventPayloadMapper;
  }

  @Override
  public Type getEventType() {
    return Type.DEMO_EVENT;
  }

  @Override
  public Class<DemoEventPayload> getPayloadType() {
    return DemoEventPayload.class;
  }

  @Override
  public void process(Event event) {
    Long eventId = event.getId();

    DemoEventPayload payload =
        eventPayloadMapper.fromJsonNode(event.getPayload(), getPayloadType());

    // Step 1: Initialize event and perform lightweight tasks
    if (event.getCurrentStep() == null) {
      log.debug(
          "[Event #{}] Starting DemoEvent. Payload = {}",
          eventId,
          eventPayloadMapper.toJsonString(payload));

      broadcastMessage(payload);
      stepper.advanceEventToStep(event, STEP_INIT); // save the progress
    } else {
      log.debug(
          "[Event #{}] Resuming DemoEvent from step '{}'. Payload = {}",
          eventId,
          event.getCurrentStep(),
          eventPayloadMapper.toJsonString(payload));
    }

    // Step 2: Perform heavy, time-consuming work atomically
    if (event.getCurrentStep().equals(STEP_INIT)) {
      doTimeConsumingWork(event, payload);
      stepper.advanceEventToStep(event, STEP_TIME_CONSUMING_WORK_DONE); // save the progress
    }

    // Step 3: Execute transactional operations
    if (event.getCurrentStep().equals(STEP_TIME_CONSUMING_WORK_DONE)) {
      transactionTemplate.executeWithoutResult(_ -> validateRetryCondition(event, payload));
      stepper.advanceEventToStep(event, STEP_TX_VALIDATED); // save the progress
    }

    // Step 4: Finalize the event outcome
    if (event.getCurrentStep().equals(STEP_TX_VALIDATED)) {
      decideResult(payload);
    }
  }

  /**
   * Simulate heavy work that may take time.
   *
   * <p>Assumptions: - Side effects occur only after successful completion. - Work is idempotent or
   * automatically reversible if interrupted. Otherwise, resuming a partially completed work must
   * fail the event, possibly after restoring the previous state.
   *
   * <p>Checkpoints should be used to persist progress safely and allow idempotent retries.
   *
   * @param event Event
   * @param payload Event payload
   */
  private void doTimeConsumingWork(Event event, DemoEventPayload payload) {
    Long delay = payload.durationMs();
    if (delay == null || delay <= 0) {
      log.info(
          "[Event #{}] No execution delay specified, skipping time-consuming work.", event.getId());
      return;
    }
    try {
      log.debug(
          "[Event #{}] Heavy work begins. The system will simulate a task that takes {} ms...",
          event.getId(),
          delay);
      Thread.sleep(delay);
      log.debug("[Event #{}] Heavy work completed.", event);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn(
          "[Event #{}] Heavy work was unexpectedly interrupted! Any side effects are assumed reverted or safe to retry. ",
          event.getId());
      throw new IllegalStateException("Simulation of heavy work was unexpectedly interrupted");
    }
  }

  private void broadcastMessage(DemoEventPayload payload) {
    if (payload.message() == null) {
      log.info("No message provided, skipping broadcast.");
      return;
    }
    sseService.broadcast(payload.message());
  }

  private void validateRetryCondition(Event event, DemoEventPayload payload) {
    int currentRetry = event.getRetryCount();
    int requiredRetries = payload.failures();
    if (requiredRetries > currentRetry) {
      throw new DownstreamTimeoutException(
          String.format(
              "Simulated downstream timeout. Retry count: %d/%d.", currentRetry, requiredRetries));
    }
  }

  private void decideResult(DemoEventPayload payload) {
    if (!payload.expectSuccess()) {
      throw new RejectedEventException("Simulated rejection based on payload condition.");
    }
  }
}
