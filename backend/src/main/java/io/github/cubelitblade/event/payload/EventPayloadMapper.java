package io.github.cubelitblade.event.payload;

import io.github.cubelitblade.common.exception.FatalEventException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPayloadMapper {
    private final ObjectMapper objectMapper;

    /**
     * Deserializes a {@link JsonNode} into the specified {@link EventPayload} type.
     *
     * @param jsonNode      The {@link JsonNode} containing the event payload data.
     * @param eventPayload  The target class type to deserialize into.
     * @param <PayloadType> The generic type of the payload.
     * @return The deserialized payload object.
     * @throws FatalEventException if deserialization fails.
     */
    public <PayloadType extends EventPayload> PayloadType fromJsonNode(JsonNode jsonNode, Class<PayloadType> eventPayload) {
        try {
            return objectMapper.convertValue(jsonNode, eventPayload);
        } catch (IllegalArgumentException e) {
            throw new FatalEventException("Failed to convert JsonNode to " + eventPayload.getName(), e);
        }
    }

    /**
     * Serializes an {@link EventPayload} object into a {@link JsonNode}.
     *
     * @param eventPayload The payload object to serialize.
     * @return The {@link JsonNode} representation of the payload, or null if input was null.
     * @throws FatalEventException if serialization fails.
     */
    public JsonNode toJsonNode(EventPayload eventPayload) {
        try {
            return eventPayload == null ? null : objectMapper.valueToTree(eventPayload);
        } catch (JacksonException e) {
            throw new FatalEventException("Failed to serialize EventPayload", e);
        }
    }

    /**
     * Serializes an {@link EventPayload} object into a JSON string.
     *
     * @param eventPayload The payload object to serialize.
     * @return The JSON string representation.
     * @throws FatalEventException if serialization fails.
     */
    public String toJsonString(EventPayload eventPayload) {
        try {
            return objectMapper.writeValueAsString(eventPayload);
        } catch (JacksonException e) {
            throw new FatalEventException("Failed to serialize EventPayload", e);
        }
    }
}
