package io.github.cubelitblade.reaction.application;

import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.common.exception.ApiErrorCode;
import io.github.cubelitblade.common.exception.ValidationException;
import io.github.cubelitblade.common.id.SnowflakeIdGenerator;
import io.github.cubelitblade.common.time.TimeProvider;
import io.github.cubelitblade.notification.application.NotificationService;
import io.github.cubelitblade.reaction.dto.AddReactionRequest;
import io.github.cubelitblade.reaction.model.Reaction;
import io.github.cubelitblade.reaction.model.ReactionType;
import io.github.cubelitblade.reaction.model.TargetType;
import io.github.cubelitblade.reaction.persistence.ReactionRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReactionService {

  private final ReactionRepository reactionRepository;
  private final ReactionCountCache reactionCountCache;
  private final SnowflakeIdGenerator idGenerator;
  private final TimeProvider timeProvider;
  private final NotificationService notificationService;

  public SetReactionResult setReaction(
      JwtAuthenticatedUser authenticatedUser, AddReactionRequest request) {
    TargetType targetType = parseTargetType(request.targetType());

    // Temporarily omit features that are not yet implemented;
    // remove them once they are implemented
    if (targetType.equals(TargetType.COMMENT) || targetType.equals(TargetType.ACTIVITY)) {
      throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Not implemented yet");
    }

    Optional<Reaction> existingReaction =
        reactionRepository.findUserReaction(
            authenticatedUser.accountId(), targetType, request.targetId());

    if (request.reactionType() == null || request.reactionType().isBlank()) {
      if (existingReaction.isPresent()) {
        Reaction reaction = existingReaction.get();
        reactionRepository.deleteReaction(reaction.getId());
        reactionCountCache.applyReactionChange(
            request.targetId(), reaction.getReactionType().getValue(), null);
        return SetReactionResult.removed(reaction.getId());
      }

      return SetReactionResult.unchanged();
    }

    ReactionType reactionType = parseReactionType(request.reactionType());

    if (existingReaction.isPresent()) {
      Reaction reaction = existingReaction.get();
      String previousReactionType = reaction.getReactionType().getValue();
      reaction.set(reactionType, timeProvider.now());
      reactionRepository.updateReaction(reaction);
      reactionCountCache.applyReactionChange(
          request.targetId(), previousReactionType, reactionType.getValue());

      return SetReactionResult.updated(reaction.getId());
    }

    long candidateId = idGenerator.nextId();

    Reaction reaction =
        Reaction.create(
            candidateId,
            authenticatedUser.accountId(),
            targetType,
            request.targetId(),
            reactionType,
            timeProvider.now());

    reactionRepository.createReaction(reaction);
    reactionCountCache.applyReactionChange(request.targetId(), null, reactionType.getValue());
    notificationService.notifyPostReaction(reaction);
    return SetReactionResult.created(reaction.getId());
  }

  private TargetType parseTargetType(String targetType) {
    try {
      return TargetType.from(targetType);
    } catch (IllegalArgumentException e) {
      throw new ValidationException(ApiErrorCode.INVALID_REQUEST, e.getMessage());
    }
  }

  private ReactionType parseReactionType(String reactionType) {
    try {
      return ReactionType.from(reactionType);
    } catch (IllegalArgumentException e) {
      throw new ValidationException(ApiErrorCode.INVALID_REQUEST, e.getMessage());
    }
  }
}
