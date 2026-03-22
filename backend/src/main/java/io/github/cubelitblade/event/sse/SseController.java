package io.github.cubelitblade.event.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SseController {
    private final SseService sseService;

    @GetMapping("/sse")
    public SseEmitter subscribe() {
        return sseService.subscribe();
    }
}
