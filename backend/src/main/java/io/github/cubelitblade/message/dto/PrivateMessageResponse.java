package io.github.cubelitblade.message.dto;

import io.github.cubelitblade.message.model.PrivateMessage;
import java.time.Instant;

public record PrivateMessageResponse(
    Long id,
    Long senderAccountId,
    Long recipientAccountId,
    String content,
    boolean isRead,
    Instant readAt,
    Instant createdAt) {
  public static PrivateMessageResponse from(PrivateMessage message) {
    return new PrivateMessageResponse(
        message.getId(),
        message.getSenderAccountId(),
        message.getRecipientAccountId(),
        message.getContent(),
        message.isRead(),
        message.getReadAt(),
        message.getCreatedAt());
  }
}
