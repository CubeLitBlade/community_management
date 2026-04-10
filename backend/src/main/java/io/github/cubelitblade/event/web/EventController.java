package io.github.cubelitblade.event.web;

import io.github.cubelitblade.event.application.EventService;
import io.github.cubelitblade.event.model.Event;
import io.github.cubelitblade.event.model.payload.DemoEventPayload;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
public class EventController {
  private final EventService eventService;

  @PostMapping("/{type}")
  public ResponseEntity<?> createEvent(
      @PathVariable("type") String eventType, @RequestBody DemoEventPayload payload) {
    Event event = eventService.enqueueEvent(eventType, payload);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(event.getId())
            .toUri();
    return ResponseEntity.created(location).build();
  }
}
