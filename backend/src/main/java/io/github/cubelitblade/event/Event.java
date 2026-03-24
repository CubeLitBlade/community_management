package io.github.cubelitblade.event;

import com.baomidou.mybatisplus.annotation.*;
import io.github.cubelitblade.common.typehandler.JsonbTypeHandler;
import lombok.*;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(autoResultMap = true)
public class Event {
    @TableId(type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    private EventType type;

    @TableField(typeHandler = JsonbTypeHandler.class)
    private JsonNode payload;

    @Builder.Default
    private EventStatus status = EventStatus.WAITING;

    @Builder.Default
    private Integer retryCount = 0;

    private String errorMsg;

    private Instant createdAt;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Instant nextRunAt;

    private Instant updatedAt;

    private String currentStep;


    @Getter
    public enum EventType {
        DEMO_EVENT("demo");

        @EnumValue
        private final String type;

        private static final Map<String, EventType> map = Arrays.stream(EventType.values()).collect(
                Collectors.toMap(EventType::getType, v -> v)
        );

        EventType(String type) {
            this.type = type;
        }

        public static EventType from(String type) {
            EventType eventType = map.get(type);
            if (eventType == null) {
                throw new IllegalArgumentException("Unknown event type: " + type);
            }
            return eventType;
        }
    }

    @Getter
    public enum EventStatus {
        WAITING("waiting"),
        PENDING("pending"),
        RUNNING("running"),
        SUCCEEDED("succeeded"),
        FAILED("failed"),
        DEAD("dead");

        @EnumValue
        private final String status;

        EventStatus(String status) {
            this.status = status;
        }
    }
}
