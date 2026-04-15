package io.github.cubelitblade.reaction.web;

import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.reaction.application.ReactionService;
import io.github.cubelitblade.reaction.application.SetReactionResult;
import io.github.cubelitblade.reaction.dto.AddReactionRequest;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
public class ReactionController {

  private final ReactionService reactionService;

  @PostMapping
  public ResponseEntity<Void> setReaction(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser,
      @RequestBody AddReactionRequest request) {

    SetReactionResult result = reactionService.setReaction(authenticatedUser, request);

    if (result.outcome() != SetReactionResult.Outcome.CREATED) {
      return ResponseEntity.noContent().build();
    }

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(result.reactionId())
            .toUri();

    return ResponseEntity.created(location).build();
  }
}
