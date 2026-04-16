package io.github.cubelitblade.comment.application;

import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.comment.dto.CommentDetailView;
import io.github.cubelitblade.comment.dto.CommentListResponse;
import io.github.cubelitblade.comment.dto.CreateCommentRequest;
import io.github.cubelitblade.comment.model.Comment;
import io.github.cubelitblade.comment.model.TargetType;
import io.github.cubelitblade.comment.persistence.CommentQueryRepository;
import io.github.cubelitblade.comment.persistence.CommentRepository;
import io.github.cubelitblade.common.exception.ApiErrorCode;
import io.github.cubelitblade.common.exception.ValidationException;
import io.github.cubelitblade.common.id.SnowflakeIdGenerator;
import io.github.cubelitblade.common.time.TimeProvider;
import io.github.cubelitblade.notification.application.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final CommentQueryRepository commentQueryRepository;
  private final SnowflakeIdGenerator idGenerator;
  private final TimeProvider timeProvider;
  private final NotificationService notificationService;

  public Long createComment(JwtAuthenticatedUser authenticatedUser, CreateCommentRequest request) {
    TargetType targetType = parseTargetType(request.targetType());
    validateCreateRequest(request);

    Comment comment =
        Comment.create(
            idGenerator.nextId(),
            targetType,
            request.targetId(),
            authenticatedUser.accountId(),
            request.parentId(),
            request.content(),
            timeProvider.now());

    commentRepository.createComment(comment);
    notificationService.notifyPostComment(comment);

    return comment.getId();
  }

  public CommentListResponse getComments(String targetType, Long targetId) {
    if (targetId == null) {
      throw new ValidationException(ApiErrorCode.INVALID_REQUEST, "targetId is required");
    }

    TargetType parsedTargetType = parseTargetType(targetType);

    return new CommentListResponse(
        commentQueryRepository.getCommentsByTarget(parsedTargetType, targetId).stream()
            .map(CommentDetailView::from)
            .toList());
  }

  private TargetType parseTargetType(String targetType) {
    if (targetType == null || targetType.isBlank()) {
      throw new ValidationException(ApiErrorCode.INVALID_REQUEST, "targetType is required");
    }

    try {
      return TargetType.from(targetType);
    } catch (IllegalArgumentException e) {
      throw new ValidationException(ApiErrorCode.INVALID_REQUEST, e.getMessage());
    }
  }

  private void validateCreateRequest(CreateCommentRequest request) {
    if (request.targetId() == null) {
      throw new ValidationException(ApiErrorCode.INVALID_REQUEST, "targetId is required");
    }
    if (request.content() == null || request.content().isBlank()) {
      throw new ValidationException(ApiErrorCode.INVALID_REQUEST, "content is required");
    }
  }
}
