package io.github.cubelitblade.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.github.cubelitblade.utils.JsonbTypeHandler;
import lombok.*;
import tools.jackson.databind.JsonNode;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(autoResultMap = true)
public class Event {
    @TableId(type = IdType.AUTO)
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


    @Getter
    public enum EventType {
        DEMO_EVENT("demo");

        @EnumValue
        private final String type;

        EventType(String type) {
            this.type = type;
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
