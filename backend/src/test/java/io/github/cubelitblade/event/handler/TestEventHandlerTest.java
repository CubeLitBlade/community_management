package io.github.cubelitblade.event.handler;

import io.github.cubelitblade.entity.Event;
import io.github.cubelitblade.event.payload.TestEventPayload;
import io.github.cubelitblade.service.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestEventHandlerTest {

    @Mock
    EventService eventService;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    TestEventHandler testEventHandler;

    @Test
    void handleEvent_shouldMarkEventAsSucceeded_whenPayloadIndicatesSuccess() {
        Event event = new Event();
        event.setId(1L);
        event.setRetryCount(0);

        TestEventPayload testEventPayload = new TestEventPayload();
        testEventPayload.setMessage("This is a debug event to succeed. ");
        testEventPayload.setRequiredRetries(0);
        testEventPayload.setShouldSucceed(true);

        when(objectMapper.convertValue(any(), eq(TestEventPayload.class))).thenReturn(testEventPayload);
        testEventHandler.handleEvent(event);

        assertEquals(Event.EventStatus.SUCCEEDED, event.getStatus());
        assertNull(event.getNextRunAt());

        verify(eventService).updateById(event);
    }

    @Test
    void handleEvent_shouldMarkEventAsSucceeded_whenPayloadIndicatesFailure() {
        Event event = new Event();
        event.setId(2L);
        event.setRetryCount(0);

        TestEventPayload testEventPayload = new TestEventPayload();
        testEventPayload.setMessage("This is a debug event to fail. ");
        testEventPayload.setRequiredRetries(0);
        testEventPayload.setShouldSucceed(false);

        when(objectMapper.convertValue(any(), eq(TestEventPayload.class))).thenReturn(testEventPayload);
        testEventHandler.handleEvent(event);

        assertEquals(Event.EventStatus.FAILED, event.getStatus());
        assertNull(event.getNextRunAt());

        verify(eventService).updateById(event);
    }

    @Test
    void handleEvent_shouldScheduleRetry_whenPayloadRequiresRetries() {
        Event event = new Event();
        event.setId(3L);
        event.setRetryCount(0);

        TestEventPayload payload = new TestEventPayload();
        payload.setMessage("This is a debug event to retry.");
        payload.setRequiredRetries(1);
        payload.setRetryDelaySeconds(5L);
        payload.setShouldSucceed(true);

        when(objectMapper.convertValue(any(), eq(TestEventPayload.class)))
                .thenReturn(payload);

        testEventHandler.handleEvent(event);

        assertEquals(Event.EventStatus.WAITING, event.getStatus());
        assertEquals(1, event.getRetryCount());
        assertNotNull(event.getNextRunAt());
        assertTrue(event.getNextRunAt().isAfter(LocalDateTime.now()));

        verify(eventService).updateById(event);
    }
}