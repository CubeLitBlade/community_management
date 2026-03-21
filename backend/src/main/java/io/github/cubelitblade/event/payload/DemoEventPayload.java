package io.github.cubelitblade.event.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DemoEventPayload {
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
