package io.github.cubelitblade.event.worker;

import io.github.cubelitblade.configuration.RetryConfig;
import io.github.cubelitblade.event.Event;
import io.github.cubelitblade.event.EventService;
import io.github.cubelitblade.event.payload.DemoEventPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "spring.docker.compose.file=../compose.yaml",
        "worker.retry.base-delay=0s",
        "worker.retry.max-delay=0s",
        "worker.retry.max-retries=3"
})
class DemoWorkerIntegrationTest {

    @Autowired
    private Worker worker;

    @Autowired
    private EventService eventService;

    @Autowired
    private RetryConfig retryConfig;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTable() {
        jdbcTemplate.execute("TRUNCATE TABLE event RESTART IDENTITY");
    }

    @Test
    @DisplayName("Process: should mark event as SUCCEEDED")
    void should_mark_event_as_succeeded() {
        // Given
        Event event = enqueueDemoEvent(new DemoEventPayload("ok", 0L, 0, true));

        // When
        worker.run();

        // Then
        Event updated = reload(event);
        assertThat(updated)
                .extracting(Event::getStatus, Event::getNextRunAt)
                .containsExactly(Event.EventStatus.SUCCEEDED, null);
    }

    @Test
    @DisplayName("Decision: should mark event as FAILED when payload indicates failure")
    void should_mark_event_as_failed_when_payload_requires_failure() {
        // Given
        Event event = enqueueDemoEvent(new DemoEventPayload("fail", 0L, 0, false));

        // When
        worker.run();

        // Then
        Event updated = reload(event);
        assertThat(updated)
                .extracting(Event::getStatus, Event::getNextRunAt)
                .containsExactly(Event.EventStatus.FAILED, null);
    }

    @Test
    @DisplayName("Retry: should reschedule once then succeed")
    void should_reschedule_once_then_succeed() {
        // Given
        Event event = enqueueDemoEvent(new DemoEventPayload("retry", 0L, 1, true));

        // When
        worker.run();

        // Then
        Event afterFirstRun = reload(event);
        assertThat(afterFirstRun.getStatus()).isEqualTo(Event.EventStatus.WAITING);
        assertThat(afterFirstRun.getRetryCount()).isEqualTo(1);
        assertThat(afterFirstRun.getNextRunAt()).isNotNull();

        // When
        worker.run();

        // Then
        Event afterSecondRun = reload(event);
        assertThat(afterSecondRun)
                .extracting(Event::getStatus, Event::getRetryCount, Event::getNextRunAt)
                .containsExactly(Event.EventStatus.SUCCEEDED, 1, null);
    }

    @Test
    @DisplayName("Retry: should mark event as DEAD after max retries")
    void should_mark_event_as_dead_after_max_retries() {
        // Given
        int requiredFailures = retryConfig.getMaxRetries() + 1;
        Event event = enqueueDemoEvent(new DemoEventPayload("dead", 0L, requiredFailures, true));

        // When
        for (int i = 0; i < requiredFailures; i++) {
            worker.run();
        }

        // Then
        Event updated = reload(event);
        assertThat(updated)
                .extracting(Event::getStatus, Event::getRetryCount, Event::getNextRunAt)
                .containsExactly(Event.EventStatus.DEAD, retryConfig.getMaxRetries(), null);
    }

    @Test
    @DisplayName("Schedule: should skip event scheduled in the future")
    void should_skip_event_scheduled_in_future() {
        // Given
        Event event = enqueueDemoEvent(new DemoEventPayload("future", 0L, 0, true));
        event.await(Instant.now(), Instant.now().plusSeconds(60));
        eventService.updateEvent(event);

        // When
        worker.run();

        // Then
        Event updated = reload(event);
        assertThat(updated)
                .extracting(Event::getStatus, Event::getRetryCount)
                .containsExactly(Event.EventStatus.WAITING, 0);
    }

    private Event enqueueDemoEvent(DemoEventPayload payload) {
        return eventService.enqueueEvent(Event.EventType.DEMO_EVENT, payload);
    }

    private Event reload(Event event) {
        return eventService.find(event.getId());
    }

    @TestConfiguration
    static class SyncExecutorConfig {
        @Bean("workerExecutor")
        @Primary
        Executor syncExecutor() {
            return Runnable::run;
        }
    }
}
