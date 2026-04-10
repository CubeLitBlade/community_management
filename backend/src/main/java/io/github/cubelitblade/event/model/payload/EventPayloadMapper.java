package io.github.cubelitblade.event.model.payload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPayloadMapper {
  private final ObjectMapper objectMapper;

  public <PayloadType extends EventPayload> PayloadType fromJsonNode(
      JsonNode jsonNode, Class<PayloadType> eventPayload) {
    return objectMapper.convertValue(jsonNode, eventPayload);
  }

  public JsonNode toJsonNode(EventPayload eventPayload) {
    return eventPayload == null ? null : objectMapper.valueToTree(eventPayload);
  }

  public String toJsonString(EventPayload eventPayload) {
    return objectMapper.writeValueAsString(eventPayload);
  }
}
