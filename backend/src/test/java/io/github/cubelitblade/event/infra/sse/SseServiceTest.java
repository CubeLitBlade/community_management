package io.github.cubelitblade.event.infra.sse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.cubelitblade.notification.dto.NotificationResponse;
import java.io.IOException;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseServiceTest {

  @Test
  @DisplayName("Send: should push only to the intended user emitters")
  void should_push_only_to_intended_user_emitters() throws IOException {
    SseService sseService = new SseService();
    SseEmitter emitterOne = mock(SseEmitter.class);
    SseEmitter emitterTwo = mock(SseEmitter.class);

    sseService.register(1L, emitterOne);
    sseService.register(2L, emitterTwo);

    NotificationResponse notification =
        new NotificationResponse(
            1L,
            1L,
            2L,
            "Alice",
            "post_comment",
            "post",
            3L,
            "content",
            "Post title",
            "Post summary",
            null,
            null,
            false,
            null,
            Instant.now());

    sseService.sendToUser(1L, notification);

    verify(emitterOne).send(any(SseEmitter.SseEventBuilder.class));
    verify(emitterTwo, times(0)).send(any(SseEmitter.SseEventBuilder.class));
  }

  @Test
  @DisplayName("Send: should remove dead emitters on failure")
  void should_remove_dead_emitter_on_failure() throws IOException {
    SseService sseService = new SseService();
    SseEmitter brokenEmitter = mock(SseEmitter.class);
    SseEmitter healthyEmitter = mock(SseEmitter.class);
    doThrow(new IOException("boom"))
        .when(brokenEmitter)
        .send(any(SseEmitter.SseEventBuilder.class));

    sseService.register(1L, brokenEmitter);
    sseService.register(1L, healthyEmitter);

    NotificationResponse notification =
        new NotificationResponse(
            1L,
            1L,
            2L,
            "Alice",
            "post_comment",
            "post",
            3L,
            "content",
            "Post title",
            "Post summary",
            null,
            null,
            false,
            null,
            Instant.now());

    sseService.sendToUser(1L, notification);
    sseService.sendToUser(1L, notification);

    verify(brokenEmitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    verify(healthyEmitter, times(2)).send(any(SseEmitter.SseEventBuilder.class));
  }
}
