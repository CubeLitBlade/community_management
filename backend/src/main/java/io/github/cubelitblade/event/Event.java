package io.github.cubelitblade.event;

import com.baomidou.mybatisplus.annotation.*;
import io.github.cubelitblade.common.typehandler.JsonbTypeHandler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.JsonNode;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@TableName(autoResultMap = true)
public class Event {
    @TableId(type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    private EventType type;

    @TableField(typeHandler = JsonbTypeHandler.class)
    private JsonNode payload;

    private EventStatus status = EventStatus.WAITING;

    private Integer retryCount = 0;

    private String errorMsg;

    private Instant createdAt;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Instant nextRunAt;

    private Instant updatedAt;

    private String currentStep;

    public static Event create(EventType type, JsonNode payload, Clock clock) {
        Instant now = Instant.now(clock);

        Event event = new Event();
        event.type = type;
        event.payload = payload;
        event.status = EventStatus.WAITING;
        event.createdAt = now;
        event.nextRunAt = now;
        event.updatedAt = now;

        return event;
    }

    public static Event create(String type, JsonNode payload, Clock clock) {
        Instant now = Instant.now(clock);

        Event event = new Event();
        event.type = EventType.from(type);
        event.payload = payload;
        event.status = EventStatus.WAITING;
        event.createdAt = now;
        event.nextRunAt = now;
        event.updatedAt = now;

        return event;
    }

    public void await(Instant now, Instant nextRunAt) {
        this.status = EventStatus.WAITING;
        this.errorMsg = null;
        this.nextRunAt = nextRunAt;
        this.retryCount = 0;
        this.touch(now);
    }

    public void succeed(Instant now) {
        this.status = EventStatus.SUCCEEDED;
        this.errorMsg = null;
        this.nextRunAt = null;
        this.touch(now);
    }

    public void fail(String reason, Instant now) {
        this.status = EventStatus.FAILED;
        this.errorMsg = reason;
        this.nextRunAt = null;
        this.touch(now);
    }

    public void die(String reason, Instant now) {
        this.status = EventStatus.DEAD;
        this.errorMsg = reason;
        this.nextRunAt = null;
        this.touch(now);
    }

    public void run(Instant now) {
        this.status = EventStatus.RUNNING;
        this.nextRunAt = null;
        this.touch(now);
    }

    public void prepareForRetry(Instant nextRunAt, String reason, Instant now) {
        this.status = EventStatus.WAITING;
        retryCount = retryCount + 1;
        this.nextRunAt = nextRunAt;
        this.errorMsg = reason;
        this.currentStep = null;    // Clear progress to a clean retry
        this.touch(now);
    }

    public void toStep(String currentStep, Instant now) {
        this.currentStep = currentStep;
        this.touch(now);
    }

    private void touch(Instant now) {
        this.updatedAt = now;
    }


    @Getter
    public enum EventType {
        EVENT("event"),
        DEMO_EVENT("demo");

        private static final Map<String, EventType> map = Arrays.stream(EventType.values()).collect(
                Collectors.toMap(EventType::getType, v -> v)
        );
        @EnumValue
        private final String type;

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
