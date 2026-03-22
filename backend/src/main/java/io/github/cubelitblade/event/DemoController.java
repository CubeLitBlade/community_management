package io.github.cubelitblade.event;

import io.github.cubelitblade.event.payload.DemoEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DemoController {
    private final EventService eventService;

    @PostMapping("/demo/event")
    public void createEvent(@RequestBody DemoEventPayload payload) {
        eventService.createDemoEvent(payload);
    }
}
