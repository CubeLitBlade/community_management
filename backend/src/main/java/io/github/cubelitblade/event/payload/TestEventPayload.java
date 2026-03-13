package io.github.cubelitblade.event.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestEventPayload {
    private String message;

    @JsonSetter(nulls = Nulls.SKIP)
    private Long executionDelayMilliseconds = 1000L;

    @JsonSetter(nulls = Nulls.SKIP)
    private Integer requiredRetries = 0;

    @JsonSetter(nulls = Nulls.SKIP)
    private Long retryDelaySeconds = 5L;

    @JsonSetter(nulls = Nulls.SKIP)
    private Boolean shouldSucceed = true;
}
