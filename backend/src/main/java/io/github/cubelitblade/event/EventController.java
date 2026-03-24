package io.github.cubelitblade.event;

import io.github.cubelitblade.common.exception.InvalidParameterException;
import io.github.cubelitblade.event.payload.DemoEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @PostMapping("/{type}")
    public ResponseEntity<?> createEvent(@PathVariable("type") String eventType, @RequestBody DemoEventPayload payload) {
        Event event = eventService.enqueueEvent(eventType , payload);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(event.getId()).toUri();
        return ResponseEntity.created(location).build();
    }
}
