package io.github.cubelitblade.event.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DemoEventPayload(
        String message,
        Long durationMs,
        Integer failures,
        Boolean expectSuccess
) implements EventPayload {
    public DemoEventPayload {
        durationMs = Objects.requireNonNullElse(durationMs, 1000L);
        failures = Objects.requireNonNullElse(failures, 0);
        expectSuccess = Objects.requireNonNullElse(expectSuccess, true);
    }
}

