package io.github.cubelitblade.event.application.handler;

import io.github.cubelitblade.event.exception.EventExecutionException;
import io.github.cubelitblade.event.model.Event;
import io.github.cubelitblade.event.model.Status;
import io.github.cubelitblade.event.model.Type;
import io.github.cubelitblade.event.model.payload.EventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class EventHandler<PayloadType extends EventPayload> {
  protected final EventStepper stepper;

  /**
   * Returns the concrete class of the payload type.
   *
   * @return the {@link Class} object of {@code PayloadType}
   */
  public abstract Class<PayloadType> getPayloadType();

  /**
   * Returns the type of event this handler processes.
   *
   * @return the {@link Type} enum value
   */
  public abstract Type getEventType();

  /**
   * The core business logic method to be implemented by subclasses.
   *
   * <p>This method is called by {@link #handleEvent(Event)} when the event status is {@code
   * RUNNING}. Implementations should perform business operations and update the event entity state
   *
   * <p><b>Note:</b> If this method throws an exception, the transaction in {@link
   * #handleEvent(Event)} might roll back depending on the caller's configuration, and the event may
   * not be updated automatically. Consider handling exceptions internally or letting them propagate
   * to a global exception handler.
   *
   * @param event the event to process
   */
  public abstract void process(Event event) throws Exception;

  /**
   * Main entry point for handling an event.
   *
   * <p>Checks if the event is in {@code RUNNING} state, delegates to {@link #process(Event)}, and
   * persists the updated event state to the database.
   *
   * @param event the event to handle
   */
  public void handleEvent(Event event) {
    if (event.getStatus() != Status.RUNNING) {
      return;
    }
    try {
      process(event);
    } catch (Exception e) {
      throw new EventExecutionException(event.getId(), e);
    }
  }
}
