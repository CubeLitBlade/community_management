package io.github.cubelitblade.event.infra.sse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
public class SseService {
  private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emittersByAccountId =
      new ConcurrentHashMap<>();

  public SseEmitter subscribe(Long accountId) {
    SseEmitter emitter = new SseEmitter(0L);
    register(accountId, emitter);
    emitter.onCompletion(() -> remove(accountId, emitter));
    emitter.onTimeout(() -> remove(accountId, emitter));

    try {
      emitter.send(SseEmitter.event().name("connected").data("connected"));
    } catch (IOException e) {
      remove(accountId, emitter);
      emitter.completeWithError(e);
    }

    return emitter;
  }

  void register(Long accountId, SseEmitter emitter) {
    emittersByAccountId
        .computeIfAbsent(accountId, ignored -> new CopyOnWriteArrayList<>())
        .add(emitter);
  }

  public void sendToUser(Long accountId, Object payload) {
    List<SseEmitter> emitters = emittersByAccountId.get(accountId);
    if (emitters == null) {
      return;
    }

    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(SseEmitter.event().name("notification").data(payload));
      } catch (IOException e) {
        log.error("Failed to push notification to account {}.", accountId, e);
        remove(accountId, emitter);
      }
    }
  }

  public void broadcast(String message) {
    for (Map.Entry<Long, CopyOnWriteArrayList<SseEmitter>> entry : emittersByAccountId.entrySet()) {
      for (SseEmitter emitter : entry.getValue()) {
        try {
          emitter.send(SseEmitter.event().name("message").data(message));
        } catch (IOException e) {
          log.error("Failed to push message.", e);
          remove(entry.getKey(), emitter);
        }
      }
    }
  }

  private void remove(Long accountId, SseEmitter emitter) {
    CopyOnWriteArrayList<SseEmitter> emitters = emittersByAccountId.get(accountId);
    if (emitters == null) {
      return;
    }

    emitters.remove(emitter);
    if (emitters.isEmpty()) {
      emittersByAccountId.remove(accountId, emitters);
    }
  }
}
