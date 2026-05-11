package io.github.cubelitblade.reaction.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.github.cubelitblade.account.model.Role;
import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.common.exception.ValidationException;
import io.github.cubelitblade.common.id.SnowflakeIdGenerator;
import io.github.cubelitblade.common.time.TimeProvider;
import io.github.cubelitblade.notification.application.NotificationService;
import io.github.cubelitblade.reaction.dto.AddReactionRequest;
import io.github.cubelitblade.reaction.model.Reaction;
import io.github.cubelitblade.reaction.model.ReactionType;
import io.github.cubelitblade.reaction.model.TargetType;
import io.github.cubelitblade.reaction.persistence.ReactionRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ReactionServiceTest {
  private static final Instant NOW = Instant.parse("2026-04-20T08:00:00Z");

  @Mock private ReactionRepository reactionRepository;
  @Mock private ReactionCountCache reactionCountCache;
  @Mock private SnowflakeIdGenerator idGenerator;
  @Mock private TimeProvider timeProvider;
  @Mock private NotificationService notificationService;

  private ReactionService reactionService;

  @BeforeEach
  void setUp() {
    reactionService =
        new ReactionService(
            reactionRepository, reactionCountCache, idGenerator, timeProvider, notificationService);
  }

  @Test
  @DisplayName("Set reaction: should create new post reaction, update cache, and notify")
  void should_create_post_reaction() {
    given(reactionRepository.findUserReaction(7L, TargetType.POST, 101L))
        .willReturn(Optional.empty());
    given(idGenerator.nextId()).willReturn(99L);
    given(timeProvider.now()).willReturn(NOW);

    SetReactionResult result =
        reactionService.setReaction(
            new JwtAuthenticatedUser(7L, Role.USER), new AddReactionRequest("post", 101L, "like"));

    ArgumentCaptor<Reaction> captor = ArgumentCaptor.forClass(Reaction.class);
    verify(reactionRepository).createReaction(captor.capture());
    verify(reactionCountCache).applyReactionChange(101L, null, "like");
    verify(notificationService).notifyPostReaction(captor.getValue());
    assertThat(result.outcome()).isEqualTo(SetReactionResult.Outcome.CREATED);
    assertThat(result.reactionId()).isEqualTo(99L);
    assertThat(captor.getValue().getReactionType()).isEqualTo(ReactionType.LIKE);
  }

  @Test
  @DisplayName("Set reaction: should update existing reaction and move cache count")
  void should_update_existing_reaction() {
    Reaction existing =
        Reaction.create(55L, 7L, TargetType.POST, 101L, ReactionType.LIKE, NOW.minusSeconds(60));
    given(reactionRepository.findUserReaction(7L, TargetType.POST, 101L))
        .willReturn(Optional.of(existing));
    given(timeProvider.now()).willReturn(NOW);

    SetReactionResult result =
        reactionService.setReaction(
            new JwtAuthenticatedUser(7L, Role.USER), new AddReactionRequest("post", 101L, "love"));

    verify(reactionRepository).updateReaction(existing);
    verify(reactionCountCache).applyReactionChange(101L, "like", "love");
    verify(notificationService, never()).notifyPostReaction(any());
    assertThat(existing.getReactionType()).isEqualTo(ReactionType.LOVE);
    assertThat(result.outcome()).isEqualTo(SetReactionResult.Outcome.UPDATED);
    assertThat(result.reactionId()).isEqualTo(55L);
  }

  @Test
  @DisplayName("Set reaction: should keep cache unchanged when reaction type stays the same")
  void should_update_existing_reaction_without_cache_count_change_when_type_is_same() {
    Reaction existing =
        Reaction.create(55L, 7L, TargetType.POST, 101L, ReactionType.LIKE, NOW.minusSeconds(60));
    given(reactionRepository.findUserReaction(7L, TargetType.POST, 101L))
        .willReturn(Optional.of(existing));
    given(timeProvider.now()).willReturn(NOW);

    SetReactionResult result =
        reactionService.setReaction(
            new JwtAuthenticatedUser(7L, Role.USER), new AddReactionRequest("post", 101L, "like"));

    verify(reactionRepository).updateReaction(existing);
    verify(reactionCountCache).applyReactionChange(101L, "like", "like");
    verify(notificationService, never()).notifyPostReaction(any());
    assertThat(existing.getReactionType()).isEqualTo(ReactionType.LIKE);
    assertThat(result.outcome()).isEqualTo(SetReactionResult.Outcome.UPDATED);
    assertThat(result.reactionId()).isEqualTo(55L);
  }

  @Test
  @DisplayName("Set reaction: should remove existing reaction when reaction type is blank")
  void should_remove_existing_reaction() {
    Reaction existing =
        Reaction.create(55L, 7L, TargetType.POST, 101L, ReactionType.LIKE, NOW.minusSeconds(60));
    given(reactionRepository.findUserReaction(7L, TargetType.POST, 101L))
        .willReturn(Optional.of(existing));

    SetReactionResult result =
        reactionService.setReaction(
            new JwtAuthenticatedUser(7L, Role.USER), new AddReactionRequest("post", 101L, " "));

    verify(reactionRepository).deleteReaction(55L);
    verify(reactionCountCache).applyReactionChange(101L, "like", null);
    verify(notificationService, never()).notifyPostReaction(any());
    assertThat(result.outcome()).isEqualTo(SetReactionResult.Outcome.REMOVED);
    assertThat(result.reactionId()).isEqualTo(55L);
  }

  @Test
  @DisplayName("Set reaction: should not write when clearing a missing reaction")
  void should_keep_missing_reaction_unchanged_when_clearing() {
    given(reactionRepository.findUserReaction(7L, TargetType.POST, 101L))
        .willReturn(Optional.empty());

    SetReactionResult result =
        reactionService.setReaction(
            new JwtAuthenticatedUser(7L, Role.USER), new AddReactionRequest("post", 101L, null));

    verify(reactionRepository, never()).createReaction(any());
    verify(reactionRepository, never()).updateReaction(any());
    verify(reactionRepository, never()).deleteReaction(any());
    verify(reactionCountCache, never()).applyReactionChange(any(), any(), any());
    assertThat(result.outcome()).isEqualTo(SetReactionResult.Outcome.UNCHANGED);
    assertThat(result.reactionId()).isNull();
  }

  @Test
  @DisplayName("Set reaction: should reject unsupported target types for now")
  void should_reject_unimplemented_comment_target_type() {
    assertThatThrownBy(
            () ->
                reactionService.setReaction(
                    new JwtAuthenticatedUser(7L, Role.USER),
                    new AddReactionRequest("comment", 101L, "like")))
        .isInstanceOf(ResponseStatusException.class)
        .extracting("statusCode")
        .isEqualTo(HttpStatus.NOT_IMPLEMENTED);

    verify(reactionRepository, never()).findUserReaction(any(), any(), any());
  }

  @Test
  @DisplayName("Set reaction: should reject activity reactions for now")
  void should_reject_unimplemented_activity_target_type() {
    assertThatThrownBy(
            () ->
                reactionService.setReaction(
                    new JwtAuthenticatedUser(7L, Role.USER),
                    new AddReactionRequest("activity", 101L, "like")))
        .isInstanceOf(ResponseStatusException.class)
        .extracting("statusCode")
        .isEqualTo(HttpStatus.NOT_IMPLEMENTED);

    verify(reactionRepository, never()).findUserReaction(any(), any(), any());
  }

  @Test
  @DisplayName("Set reaction: should reject unknown target type")
  void should_reject_unknown_target_type() {
    assertThatThrownBy(
            () ->
                reactionService.setReaction(
                    new JwtAuthenticatedUser(7L, Role.USER),
                    new AddReactionRequest("unknown", 101L, "like")))
        .isInstanceOf(ValidationException.class);

    verify(reactionRepository, never()).findUserReaction(any(), any(), any());
    verify(reactionCountCache, never()).applyReactionChange(any(), any(), any());
  }

  @Test
  @DisplayName("Set reaction: should reject unknown reaction type")
  void should_reject_unknown_reaction_type() {
    given(reactionRepository.findUserReaction(7L, TargetType.POST, 101L))
        .willReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                reactionService.setReaction(
                    new JwtAuthenticatedUser(7L, Role.USER),
                    new AddReactionRequest("post", 101L, "wow")))
        .isInstanceOf(ValidationException.class);

    verify(reactionRepository, never()).createReaction(any());
    verify(reactionCountCache, never()).applyReactionChange(any(), any(), any());
  }
}
