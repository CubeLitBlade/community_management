package io.github.cubelitblade.event.worker;

import io.github.cubelitblade.configuration.RetryConfig;
import io.github.cubelitblade.event.Event;
import io.github.cubelitblade.event.EventService;
import io.github.cubelitblade.event.handler.DemoEventHandler;
import io.github.cubelitblade.event.payload.DemoEventPayload;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "worker.retry.base-delay=0s",
        "worker.retry.max-delay=0s",
        "worker.retry.max-retries=3"
})
@Transactional
public class DemoWorkerIntegrationTest {
    @Autowired
    Worker worker;

    @Autowired
    private EventService eventService;

    @Autowired
    private DemoEventHandler demoEventHandler;

    @Autowired
    private RetryConfig retryConfig;

    @Test
    void shouldProcessEventSuccessfully() {
        Event event = createEvent(DemoEventPayload.builder()
                .shouldSucceed(true)
                .build()
        );

        worker.run();

        event = reloadEvent(event);
        assertEquals(Event.EventStatus.SUCCEEDED, event.getStatus());
        assertNull(event.getNextRunAt());
    }

    @Test
    void shouldMarkEventAsFailed() {
        Event event = createEvent(DemoEventPayload.builder()
                .shouldSucceed(false)
                .build()
        );

        worker.run();

        event = reloadEvent(event);
        assertEquals(Event.EventStatus.FAILED, event.getStatus());
        assertNull(event.getNextRunAt());
    }

    @Test
    void shouldRetryEventAndEventuallySucceed() {
        Event event = createEvent(DemoEventPayload.builder()
                .requiredRetries(1)
                .shouldSucceed(true)
                .build()
        );

        worker.run();

        event = reloadEvent(event);
        assertEquals(Event.EventStatus.WAITING, event.getStatus());
        assertNotNull(event.getNextRunAt());

        worker.run();

        event = reloadEvent(event);
        assertEquals(Event.EventStatus.SUCCEEDED, event.getStatus());
        assertEquals(1, event.getRetryCount());
        assertNull(event.getNextRunAt());
    }

    @Test
    void shouldRetryEventAndEventuallyMarkAsDead() {
        int retries = retryConfig.getMaxRetries() + 1;
        Event event = createEvent(DemoEventPayload.builder()
                .requiredRetries(retries)
                .shouldSucceed(true)
                .build()
        );

        for (int i = 0; i < retries; i++) {
            worker.run();
        }

        event = reloadEvent(event);
        assertEquals(Event.EventStatus.DEAD, event.getStatus());
        assertEquals(retryConfig.getMaxRetries(), event.getRetryCount());
        assertNull(event.getNextRunAt());
    }

    @Test
    void shouldSkipEventScheduledInFuture() {
        Event event = createEvent(DemoEventPayload.builder()
                .shouldSucceed(true)
                .build()
        );
        event.setNextRunAt(Instant.now().plusSeconds(60));
        eventService.updateEvent(event);

        worker.run();

        event = reloadEvent(event);
        assertEquals(Event.EventStatus.WAITING, event.getStatus());
    }

    private Event createEvent(DemoEventPayload payload) {
        Event event = Event.builder()
                .type(Event.EventType.DEMO_EVENT)
                .status(Event.EventStatus.WAITING)
                .build();

        event.setPayload(demoEventHandler.serializePayload(payload));
        eventService.enqueueEvent(event);

        return event;
    }

    private Event reloadEvent(Event event) {
        return eventService.find(event.getId());
    }

    @TestConfiguration
    public static class SyncExecutorConfig {

        @Bean("workerExecutor")
        @Primary
        public Executor syncExecutor() {
            return Runnable::run;
        }
    }
}
