package io.github.cubelitblade.comment.dto;

import java.util.List;

public record CommentListResponse(List<CommentDetailView> items) {}
