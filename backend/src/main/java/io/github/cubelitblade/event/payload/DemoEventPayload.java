package io.github.cubelitblade.event.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class DemoEventPayload implements EventPayload {
    private String message;

    @Builder.Default
    @JsonSetter(nulls = Nulls.SKIP)
    private Long executionDelayMilliseconds = 1000L;

    @Builder.Default
    @JsonSetter(nulls = Nulls.SKIP)
    private Integer requiredRetries = 0;

    @Builder.Default
    @JsonSetter(nulls = Nulls.SKIP)
    private Boolean shouldSucceed = true;
}
