package io.github.cubelitblade.message.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PrivateMessage {
  private Long id;
  private Long senderAccountId;
  private Long recipientAccountId;
  private String content;
  private boolean read;
  private Instant readAt;
  private Instant createdAt;

  public static PrivateMessage create(
      Long id, Long senderAccountId, Long recipientAccountId, String content, Instant now) {
    PrivateMessage message = new PrivateMessage();
    message.id = id;
    message.senderAccountId = senderAccountId;
    message.recipientAccountId = recipientAccountId;
    message.content = content;
    message.read = false;
    message.createdAt = now;
    return message;
  }

  public static PrivateMessage reconstitute(Snapshot snapshot) {
    if (snapshot == null) {
      return null;
    }

    PrivateMessage message = new PrivateMessage();
    message.id = snapshot.id;
    message.senderAccountId = snapshot.senderAccountId;
    message.recipientAccountId = snapshot.recipientAccountId;
    message.content = snapshot.content;
    message.read = snapshot.read;
    message.readAt = snapshot.readAt;
    message.createdAt = snapshot.createdAt;
    return message;
  }

  @Builder
  public record Snapshot(
      Long id,
      Long senderAccountId,
      Long recipientAccountId,
      String content,
      boolean read,
      Instant readAt,
      Instant createdAt) {}
}
