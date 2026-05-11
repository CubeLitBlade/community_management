package io.github.cubelitblade.reaction.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import io.github.cubelitblade.reaction.model.Reaction;
import io.github.cubelitblade.reaction.model.ReactionType;
import io.github.cubelitblade.reaction.model.TargetType;
import io.github.cubelitblade.reaction.persistence.query.ReactionCountVo;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ReactionPersistenceIntegrationTest {
  private static final Instant NOW = Instant.parse("2026-04-20T08:00:00Z");
  private static final List<String> POST_REACTION_COUNT_KEYS =
      List.of(
          "post:reaction:counts:9501", "post:reaction:counts:9502", "post:reaction:counts:9503");

  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private StringRedisTemplate stringRedisTemplate;
  @Autowired private ReactionRepository reactionRepository;
  @Autowired private ReactionQueryRepository reactionQueryRepository;

  @BeforeEach
  void setUp() {
    stringRedisTemplate.delete(POST_REACTION_COUNT_KEYS);
    insertAccount(9401L, "reaction_author", "Reaction Author");
    insertAccount(9402L, "another_reactor", "Another Reactor");
    insertAccount(9403L, "third_reactor", "Third Reactor");
  }

  @AfterEach
  void tearDown() {
    stringRedisTemplate.delete(POST_REACTION_COUNT_KEYS);
  }

  @Test
  @DisplayName("ReactionRepository: should create and find user reaction")
  void should_create_and_find_user_reaction() {
    Reaction reaction =
        Reaction.create(9601L, 9401L, TargetType.POST, 9501L, ReactionType.LIKE, NOW);

    reactionRepository.createReaction(reaction);

    assertThat(reactionRepository.findUserReaction(9401L, TargetType.POST, 9501L))
        .hasValueSatisfying(
            persisted -> {
              assertThat(persisted.getId()).isEqualTo(9601L);
              assertThat(persisted.getAccountId()).isEqualTo(9401L);
              assertThat(persisted.getTargetType()).isEqualTo(TargetType.POST);
              assertThat(persisted.getTargetId()).isEqualTo(9501L);
              assertThat(persisted.getReactionType()).isEqualTo(ReactionType.LIKE);
              assertThat(persisted.getCreatedAt()).isEqualTo(NOW);
            });
  }

  @Test
  @DisplayName("ReactionRepository: should update and delete user reaction")
  void should_update_and_delete_user_reaction() {
    Reaction reaction =
        Reaction.create(9602L, 9401L, TargetType.POST, 9501L, ReactionType.LIKE, NOW);
    reactionRepository.createReaction(reaction);

    reaction.set(ReactionType.LOVE, NOW.plusSeconds(60));
    reactionRepository.updateReaction(reaction);

    assertThat(reactionRepository.findUserReaction(9401L, TargetType.POST, 9501L))
        .hasValueSatisfying(
            persisted -> {
              assertThat(persisted.getReactionType()).isEqualTo(ReactionType.LOVE);
              assertThat(persisted.getUpdatedAt()).isEqualTo(NOW.plusSeconds(60));
            });

    reactionRepository.deleteReaction(9602L);

    assertThat(reactionRepository.findUserReaction(9401L, TargetType.POST, 9501L)).isEmpty();
  }

  @Test
  @DisplayName("ReactionRepository: should enforce one reaction per user target")
  void should_enforce_one_reaction_per_user_target() {
    reactionRepository.createReaction(
        Reaction.create(9603L, 9401L, TargetType.POST, 9501L, ReactionType.LIKE, NOW));

    assertThatThrownBy(
            () ->
                reactionRepository.createReaction(
                    Reaction.create(
                        9604L,
                        9401L,
                        TargetType.POST,
                        9501L,
                        ReactionType.LOVE,
                        NOW.plusSeconds(1))))
        .isInstanceOf(DuplicateKeyException.class);
  }

  @Test
  @DisplayName("ReactionQueryRepository: should count reactions by post targets")
  void should_count_reactions_by_post_targets() {
    insertReaction(9605L, 9401L, "post", 9501L, "like");
    insertReaction(9606L, 9402L, "post", 9501L, "like");
    insertReaction(9607L, 9403L, "post", 9501L, "love");
    insertReaction(9608L, 9401L, "post", 9502L, "sad");
    insertReaction(9609L, 9402L, "comment", 9501L, "laugh");

    Map<Long, List<ReactionCountVo>> counts =
        reactionQueryRepository.selectReactionsByTargets(TargetType.POST, List.of(9501L, 9502L));

    assertThat(counts).containsOnlyKeys(9501L, 9502L);
    assertThat(counts.get(9501L))
        .extracting(ReactionCountVo::reactionType, ReactionCountVo::count)
        .containsExactlyInAnyOrder(tuple("like", 2L), tuple("love", 1L));
    assertThat(counts.get(9502L))
        .extracting(ReactionCountVo::reactionType, ReactionCountVo::count)
        .containsExactly(tuple("sad", 1L));
  }

  @Test
  @DisplayName("ReactionQueryRepository: should count non-post targets without post cache")
  void should_count_non_post_targets_without_post_cache() {
    insertReaction(9610L, 9401L, "comment", 9701L, "like");
    insertReaction(9611L, 9402L, "comment", 9701L, "laugh");
    insertReaction(9612L, 9403L, "comment", 9701L, "laugh");
    insertReaction(9613L, 9401L, "post", 9701L, "love");

    Map<Long, List<ReactionCountVo>> counts =
        reactionQueryRepository.selectReactionsByTargets(TargetType.COMMENT, List.of(9701L));

    assertThat(counts).containsOnlyKeys(9701L);
    assertThat(counts.get(9701L))
        .extracting(ReactionCountVo::reactionType, ReactionCountVo::count)
        .containsExactlyInAnyOrder(tuple("like", 1L), tuple("laugh", 2L));
  }

  @Test
  @DisplayName("ReactionQueryRepository: should find current user's reactions by targets")
  void should_find_current_user_reactions_by_targets() {
    insertReaction(9614L, 9401L, "post", 9501L, "like");
    insertReaction(9615L, 9401L, "post", 9502L, "love");
    insertReaction(9616L, 9402L, "post", 9502L, "sad");
    insertReaction(9617L, 9401L, "comment", 9503L, "laugh");

    Map<Long, String> reactions =
        reactionQueryRepository.findUserReactionsByTargets(
            9401L, TargetType.POST, List.of(9501L, 9502L, 9503L));

    assertThat(reactions).containsExactlyInAnyOrderEntriesOf(Map.of(9501L, "like", 9502L, "love"));
  }

  @Test
  @DisplayName("ReactionQueryRepository: should return empty maps for empty targets")
  void should_return_empty_maps_for_empty_targets() {
    assertThat(reactionQueryRepository.selectReactionsByTargets(TargetType.POST, List.of()))
        .isEmpty();
    assertThat(
            reactionQueryRepository.findUserReactionsByTargets(9401L, TargetType.POST, List.of()))
        .isEmpty();
  }

  private void insertAccount(Long id, String username, String nickname) {
    jdbcTemplate.update(
        """
        insert into accounts(id, username, nickname, password_hash, status, role, created_at, updated_at)
        values (?, ?, ?, 'hash', 'normal', 'user', ?, ?)
        """,
        id,
        username,
        nickname,
        ts(NOW),
        ts(NOW));
  }

  private void insertReaction(
      Long id, Long accountId, String targetType, Long targetId, String reactionType) {
    jdbcTemplate.update(
        """
        insert into reactions(id, account_id, target_type, target_id, reaction_type, created_at, updated_at)
        values (?, ?, ?, ?, ?, ?, ?)
        """,
        id,
        accountId,
        targetType,
        targetId,
        reactionType,
        ts(NOW),
        ts(NOW));
  }

  private Timestamp ts(Instant instant) {
    return instant == null ? null : Timestamp.from(instant);
  }
}
