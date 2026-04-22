package io.github.cubelitblade.message.dto;

import java.time.Instant;

public record PrivateConversationResponse(
    Long contactAccountId,
    String contactUsername,
    String contactNickname,
    String lastMessage,
    Long lastSenderAccountId,
    Instant lastMessageAt,
    long unreadCount) {}
