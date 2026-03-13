package io.github.cubelitblade.entity;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.cubelitblade.utils.JsonbTypeHandler;
import lombok.*;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(autoResultMap = true)
public class Event {
    @Getter
    public enum EventType {
        TEST_EVENT("test_event");

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
        FAILED("failed");

        @EnumValue
        private final String status;

        EventStatus(String status) {
            this.status = status;
        }
    }

    private Long id;

    private EventType type;

    @TableField(typeHandler = JsonbTypeHandler.class)
    private JsonNode payload;

    private EventStatus status;

    private Integer retryCount;

    private String errorMsg;

    private LocalDateTime createdAt;

    private LocalDateTime nextRunAt;

    private LocalDateTime updatedAt;
}
