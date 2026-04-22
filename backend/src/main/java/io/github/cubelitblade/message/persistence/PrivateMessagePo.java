package io.github.cubelitblade.message.persistence;

import io.github.cubelitblade.message.model.PrivateMessage;
import java.time.Instant;
import lombok.Builder;

@Builder
public record PrivateMessagePo(
    Long id,
    Long senderAccountId,
    Long recipientAccountId,
    String content,
    Boolean isRead,
    Instant readAt,
    Instant createdAt) {
  public static PrivateMessagePo of(PrivateMessage message) {
    if (message == null) {
      return null;
    }

    return PrivateMessagePo.builder()
        .id(message.getId())
        .senderAccountId(message.getSenderAccountId())
        .recipientAccountId(message.getRecipientAccountId())
        .content(message.getContent())
        .isRead(message.isRead())
        .readAt(message.getReadAt())
        .createdAt(message.getCreatedAt())
        .build();
  }

  public PrivateMessage toPrivateMessage() {
    PrivateMessage.Snapshot snapshot =
        PrivateMessage.Snapshot.builder()
            .id(id)
            .senderAccountId(senderAccountId)
            .recipientAccountId(recipientAccountId)
            .content(content)
            .read(Boolean.TRUE.equals(isRead))
            .readAt(readAt)
            .createdAt(createdAt)
            .build();

    return PrivateMessage.reconstitute(snapshot);
  }
}
