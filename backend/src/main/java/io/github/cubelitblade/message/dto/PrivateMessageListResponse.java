package io.github.cubelitblade.message.dto;

import java.util.List;

public record PrivateMessageListResponse(List<PrivateMessageResponse> messages) {}
