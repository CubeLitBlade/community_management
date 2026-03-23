package io.github.cubelitblade.event.handler;

import io.github.cubelitblade.configuration.RetryConfig;
import io.github.cubelitblade.event.EventService;
import io.github.cubelitblade.event.payload.EventPayloadMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

@Data
@Component
@RequiredArgsConstructor
public class EventHandlerContext {
    private final EventService eventService;
    private final ObjectMapper objectMapper;
    private final RetryConfig retryConfig;
    private final EventPayloadMapper eventPayloadMapper;
    private final TransactionTemplate transactionTemplate;
}
