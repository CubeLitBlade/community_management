package io.github.cubelitblade.event.handler;

import io.github.cubelitblade.configuration.RetryConfig;
import io.github.cubelitblade.event.Event;
import io.github.cubelitblade.event.EventService;
import io.github.cubelitblade.event.payload.DemoEventPayload;
import io.github.cubelitblade.event.payload.EventPayloadMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemoEventHandlerTest {
    @Mock
    EventService eventService;

    @Mock
    RetryConfig retryConfig;

    @Mock
    EventPayloadMapper eventPayloadMapper;

    @Mock
    EventHandlerContext context;

    @Mock
    JsonNode payloadJson;

    @InjectMocks
    DemoEventHandler demoEventHandler;

    @BeforeEach
    void setUp() {
        when(context.getEventService()).thenReturn(eventService);
        when(context.getEventPayloadMapper()).thenReturn(eventPayloadMapper);
    }

    private void mockRetryConfig() {
        when(context.getRetryConfig()).thenReturn(retryConfig);
        when(retryConfig.getBaseDelay()).thenReturn(Duration.ofSeconds(1));
        when(retryConfig.getMaxDelay()).thenReturn(Duration.ofSeconds(10));
        when(retryConfig.getMaxRetries()).thenReturn(3);
    }

    @Test
    void handleEvent_shouldMarkEventAsSucceeded_whenPayloadIndicatesSuccess() {
        // given: a running event whose payload indicates success
        Event event = Event.builder()
                .id(1L)
                .type(Event.EventType.DEMO_EVENT)
                .status(Event.EventStatus.RUNNING)
                .payload(payloadJson)
                .build();

        DemoEventPayload demoEventPayload = DemoEventPayload.builder()
                .shouldSucceed(true)
                .build();

        when(eventPayloadMapper.fromJsonNode(any(JsonNode.class), eq(DemoEventPayload.class)))
                .thenReturn(demoEventPayload);

        // when: the handler processes the event
        demoEventHandler.handleEvent(event);

        // then: the event should be marked as SUCCEEDED and persisted
        assertEquals(Event.EventStatus.SUCCEEDED, event.getStatus());
        assertNull(event.getNextRunAt());

        verify(eventService).updateEvent(event);
    }

    @Test
    void handleEvent_shouldMarkEventAsFailed_whenPayloadIndicatesFailure() {
        // given: a running event whose payload indicates failure
        Event event = Event.builder()
                .id(2L)
                .type(Event.EventType.DEMO_EVENT)
                .status(Event.EventStatus.RUNNING)
                .payload(payloadJson)
                .build();

        DemoEventPayload demoEventPayload = DemoEventPayload.builder()
                .shouldSucceed(false)
                .build();

        when(eventPayloadMapper.fromJsonNode(any(JsonNode.class), eq(DemoEventPayload.class)))
                .thenReturn(demoEventPayload);

        // when: the handler processes the event
        demoEventHandler.handleEvent(event);

        // then: the event should be marked as FAILED and persisted
        assertEquals(Event.EventStatus.FAILED, event.getStatus());
        assertNull(event.getNextRunAt());

        verify(eventService).updateEvent(event);
    }

    @Test
    void handleEvent_shouldScheduleRetry_whenPayloadRequiresRetries() {
        mockRetryConfig();

        // given: a running event that requires one retry before success
        Event event = Event.builder()
                .id(3L)
                .type(Event.EventType.DEMO_EVENT)
                .status(Event.EventStatus.RUNNING)
                .payload(payloadJson)
                .build();

        DemoEventPayload demoEventPayload = DemoEventPayload.builder()
                .requiredRetries(1)
                .shouldSucceed(true)
                .build();

        when(eventPayloadMapper.fromJsonNode(any(JsonNode.class), eq(DemoEventPayload.class)))
                .thenReturn(demoEventPayload);

        // when: the event is handled for the first time
        event.setStatus(Event.EventStatus.RUNNING);
        Instant before = Instant.now();
        demoEventHandler.handleEvent(event);

        // then: the event should be scheduled for retry

        assertEquals(Event.EventStatus.WAITING, event.getStatus());
        assertEquals(1, event.getRetryCount());
        assertNotNull(event.getNextRunAt());
        assertTrue(event.getNextRunAt().isAfter(before));

        verify(eventService).updateEvent(event);

        // when: the event is handled again
        event.setStatus(Event.EventStatus.RUNNING);
        demoEventHandler.handleEvent(event);

        // then: the event should succeed
        assertEquals(Event.EventStatus.SUCCEEDED, event.getStatus());
        assertEquals(1, event.getRetryCount());
        assertNull(event.getNextRunAt());

        verify(eventService, times(2)).updateEvent(event);
    }


    @Test
    void handleEvent_shouldMarkEventAsDead_whenRetriesExceeded() {
        mockRetryConfig();

        // given: a running event whose required retries exceed the configured max retries
        Event event = Event.builder()
                .id(4L)
                .type(Event.EventType.DEMO_EVENT)
                .status(Event.EventStatus.RUNNING)
                .payload(payloadJson)
                .build();

        DemoEventPayload demoEventPayload = DemoEventPayload.builder()
                .requiredRetries(retryConfig.getMaxRetries() + 1)
                .shouldSucceed(true)
                .build();

        when(eventPayloadMapper.fromJsonNode(any(JsonNode.class), eq(DemoEventPayload.class)))
                .thenReturn(demoEventPayload);

        // when: the handler processes the event repeatedly
        for (int i = 0; i < demoEventPayload.getRequiredRetries(); i++) {
            if (event.getStatus() == Event.EventStatus.WAITING) {
                event.setStatus(Event.EventStatus.RUNNING);
            }
            demoEventHandler.handleEvent(event);
            if (event.getStatus() == Event.EventStatus.DEAD) {
                break;
            }
        }

        // then: the event should eventually be marked as DEAD after reaching the retry limit
        assertEquals(Event.EventStatus.DEAD, event.getStatus());
        assertEquals(retryConfig.getMaxRetries(), event.getRetryCount());
        assertNull(event.getNextRunAt());

        verify(eventService, times(retryConfig.getMaxRetries() + 1)).updateEvent(event);
    }
}