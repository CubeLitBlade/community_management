package io.github.cubelitblade.reaction.application;

import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.common.id.SnowflakeIdGenerator;
import io.github.cubelitblade.common.time.TimeProvider;
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
  private final SnowflakeIdGenerator idGenerator;
  private final TimeProvider timeProvider;

  // TODO: Replace Optional<Long> with an explicit result type after the MVP phase.
  public Optional<Long> setReaction(
      JwtAuthenticatedUser authenticatedUser, AddReactionRequest request) {
    TargetType targetType = TargetType.from(request.targetType());

    // Temporarily omit features that are not yet implemented;
    // remove them once they are implemented
    if (targetType.equals(TargetType.COMMENT) || targetType.equals(TargetType.ACTIVITY)) {
      throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Not implemented yet");
    }

    ReactionType reactionType = ReactionType.from(request.reactionType());
    Optional<Reaction> existingReaction =
        reactionRepository.findUserReaction(
            authenticatedUser.accountId(), targetType, request.targetId());

    if (existingReaction.isPresent()) {
      Reaction reaction = existingReaction.get();
      reaction.set(reactionType, timeProvider.now());
      reactionRepository.updateReaction(reaction);

      return Optional.empty();
    } else {
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
      return Optional.of(reaction.getId());
    }
  }
}
