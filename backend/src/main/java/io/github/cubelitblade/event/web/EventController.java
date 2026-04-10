package io.github.cubelitblade.event.web;

import io.github.cubelitblade.event.application.EventService;
import io.github.cubelitblade.event.model.Event;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
public class EventController {
  private final EventService eventService;

  @PostMapping("/{type}")
  public ResponseEntity<?> createEvent(
      @PathVariable("type") String eventType, @RequestBody JsonNode payloadJson) {
    Event event = eventService.createEvent(eventType, payloadJson);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(event.getId())
            .toUri();
    return ResponseEntity.created(location).build();
  }
}
