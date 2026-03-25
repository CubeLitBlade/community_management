package io.github.cubelitblade.event.handler;

import io.github.cubelitblade.common.exception.FatalEventException;
import io.github.cubelitblade.common.exception.TransientEventException;
import io.github.cubelitblade.event.Event;
import io.github.cubelitblade.event.payload.EventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class EventHandler<PayloadType extends EventPayload> {
    protected final EventWorkflow workflow;

    /**
     * Returns the concrete class of the payload type.
     *
     * @return the {@link Class} object of {@code PayloadType}
     */
    public abstract Class<PayloadType> getPayloadType();

    /**
     * Returns the type of event this handler processes.
     *
     * @return the {@link io.github.cubelitblade.event.Event.EventType} enum value
     */
    public abstract Event.EventType getEventType();

    /**
     * The core business logic method to be implemented by subclasses.
     * <p>
     * This method is called by {@link #handleEvent(Event)} when the event status is {@code RUNNING}.
     * Implementations should perform business operations and update the event entity state
     * </p>
     * <p>
     * <b>Note:</b> If this method throws an exception, the transaction in {@link #handleEvent(Event)}
     * might roll back depending on the caller's configuration, and the event may not be updated automatically.
     * Consider handling exceptions internally or letting them propagate to a global exception handler.
     * </p>
     *
     * @param event the event to process
     */
    public abstract void process(Event event);

    /**
     * Main entry point for handling an event.
     * <p>
     * Checks if the event is in {@code RUNNING} state, delegates to {@link #process(Event)},
     * and persists the updated event state to the database.
     * </p>
     *
     * @param event the event to handle
     */
    public void handleEvent(Event event) {
        if (event.getStatus() != Event.EventStatus.RUNNING) {
            return;
        }
        try {
            process(event);
            if (event.getStatus() == Event.EventStatus.RUNNING) {
                event.setStatus(Event.EventStatus.SUCCEEDED);
            }
        } catch (FatalEventException e) {
            workflow.die(event, e.getMessage());
            log.error(e.getMessage(), e);
        } catch (TransientEventException e) {
            workflow.scheduleRetry(event, e.getMessage());
            log.error(e.getMessage(), e);
        } catch (Exception e) {
            workflow.fail(event, e.getMessage());
            log.error(e.getMessage(), e);
        } finally {
            workflow.commit(event);
        }
    }
}
