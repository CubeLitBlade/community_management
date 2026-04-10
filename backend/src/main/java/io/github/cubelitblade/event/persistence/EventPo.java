package io.github.cubelitblade.event.persistence;

import com.baomidou.mybatisplus.annotation.*;
import io.github.cubelitblade.common.typehandler.JsonbTypeHandler;
import io.github.cubelitblade.event.model.Event;
import io.github.cubelitblade.event.model.Status;
import io.github.cubelitblade.event.model.Type;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "events", autoResultMap = true)
public class EventPo {

  @TableId(type = IdType.AUTO)
  private Long id;

  private String type;

  @TableField(typeHandler = JsonbTypeHandler.class)
  private JsonNode payload;

  private String status;
  private Integer retryCount;
  private String errorMsg;
  private Instant createdAt;

  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private Instant nextRunAt;

  private Instant updatedAt;
  private String currentStep;

  @Version private Integer version;

  public static EventPo of(Event event) {
    if (event == null) {
      return null;
    }

    EventPo eventPo = new EventPo();

    eventPo.setId(event.getId());
    eventPo.setType(event.getType().getValue());
    eventPo.setPayload(event.getPayload());
    eventPo.setStatus(event.getStatus().getValue());
    eventPo.setRetryCount(event.getRetryCount());
    eventPo.setErrorMsg(event.getErrorMsg());
    eventPo.setCreatedAt(event.getCreatedAt());
    eventPo.setNextRunAt(event.getNextRunAt());
    eventPo.setUpdatedAt(event.getUpdatedAt());
    eventPo.setCurrentStep(event.getCurrentStep());
    eventPo.setVersion(event.getVersion());

    return eventPo;
  }

  public Event toEvent() {
    Event.Snapshot snapshot =
        Event.Snapshot.builder()
            .id(this.id)
            .type(Type.from(this.type))
            .payload(this.payload)
            .status(Status.from(this.status))
            .retryCount(this.retryCount)
            .errorMsg(this.errorMsg)
            .createdAt(this.createdAt)
            .nextRunAt(this.nextRunAt)
            .updatedAt(this.updatedAt)
            .currentStep(this.currentStep)
            .version(this.version)
            .build();

    return Event.reconstitute(snapshot);
  }
}
