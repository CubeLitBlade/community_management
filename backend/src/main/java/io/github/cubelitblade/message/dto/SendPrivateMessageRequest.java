package io.github.cubelitblade.message.dto;

public record SendPrivateMessageRequest(Long recipientAccountId, String content) {}
