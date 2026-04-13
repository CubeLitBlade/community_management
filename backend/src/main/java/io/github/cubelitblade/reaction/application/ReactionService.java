package io.github.cubelitblade.reaction.application;

import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.common.id.SnowflakeIdGenerator;
import io.github.cubelitblade.common.time.TimeProvider;
import io.github.cubelitblade.reaction.dto.AddReactionRequest;
import io.github.cubelitblade.reaction.model.Reaction;
import io.github.cubelitblade.reaction.model.ReactionType;
import io.github.cubelitblade.reaction.model.TargetType;
import io.github.cubelitblade.reaction.persistence.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReactionService {

  private final ReactionRepository reactionRepository;
  private final SnowflakeIdGenerator idGenerator;
  private final TimeProvider timeProvider;

  public long addReaction(JwtAuthenticatedUser authenticatedUser, AddReactionRequest request) {
    long candidateId = idGenerator.nextId();

    Reaction reaction =
        Reaction.create(
            candidateId,
            authenticatedUser.accountId(),
            TargetType.from(request.targetType()),
            request.targetId(),
            ReactionType.from(request.reactionType()),
            timeProvider.now());

    reactionRepository.createReaction(reaction);

    return reaction.getId();
  }
}
