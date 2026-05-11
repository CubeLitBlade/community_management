package io.github.cubelitblade.reaction.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import io.github.cubelitblade.reaction.persistence.query.ReactionCountVo;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(MockitoExtension.class)
class ReactionCountCacheTest {
  private static final String KEY_101 = "post:reaction:counts:101";
  private static final String KEY_102 = "post:reaction:counts:102";

  @Mock private StringRedisTemplate stringRedisTemplate;
  @Mock private HashOperations<String, Object, Object> hashOperations;
  @Mock private Function<List<Long>, Map<Long, List<ReactionCountVo>>> fallbackLoader;

  private ReactionCountCache reactionCountCache;

  @BeforeEach
  void setUp() {
    reactionCountCache = new ReactionCountCache(stringRedisTemplate);
  }

  @Test
  @DisplayName("Get counts: should return cached values without fallback")
  void should_return_cached_counts_without_fallback() {
    givenHashOperations();
    given(hashOperations.entries(KEY_101))
        .willReturn(new LinkedHashMap<>(Map.of("_cached", "1", "like", "2", "love", "1")));

    Map<Long, List<ReactionCountVo>> result =
        reactionCountCache.getPostReactionCounts(List.of(101L), fallbackLoader);

    assertThat(result).containsOnlyKeys(101L);
    assertThat(result.get(101L))
        .extracting("reactionType", "count")
        .containsExactlyInAnyOrder(
            org.assertj.core.groups.Tuple.tuple("like", 2L),
            org.assertj.core.groups.Tuple.tuple("love", 1L));
    verifyNoInteractions(fallbackLoader);
    verify(stringRedisTemplate, never()).delete(KEY_101);
  }

  @Test
  @DisplayName("Get counts: should load and cache only missing post ids")
  void should_load_and_cache_missing_post_ids() {
    givenHashOperations();
    given(hashOperations.entries(KEY_101)).willReturn(Map.of("_cached", "1", "like", "2"));
    given(hashOperations.entries(KEY_102)).willReturn(Map.of());
    given(fallbackLoader.apply(List.of(102L)))
        .willReturn(Map.of(102L, List.of(new ReactionCountVo("love", 3L))));

    Map<Long, List<ReactionCountVo>> result =
        reactionCountCache.getPostReactionCounts(List.of(101L, 102L), fallbackLoader);

    assertThat(result.get(101L))
        .extracting("reactionType", "count")
        .containsExactly(org.assertj.core.groups.Tuple.tuple("like", 2L));
    assertThat(result.get(102L))
        .extracting("reactionType", "count")
        .containsExactly(org.assertj.core.groups.Tuple.tuple("love", 3L));
    verify(fallbackLoader).apply(List.of(102L));
    verify(stringRedisTemplate).delete(KEY_102);
    verify(hashOperations).putAll(KEY_102, Map.of("_cached", "1", "love", "3"));
  }

  @Test
  @DisplayName("Get counts: should cache marker when fallback has no counts")
  void should_cache_marker_for_empty_fallback_result() {
    givenHashOperations();
    given(hashOperations.entries(KEY_101)).willReturn(Map.of());
    given(fallbackLoader.apply(List.of(101L))).willReturn(Map.of());

    Map<Long, List<ReactionCountVo>> result =
        reactionCountCache.getPostReactionCounts(List.of(101L), fallbackLoader);

    assertThat(result.get(101L)).isEmpty();
    verify(stringRedisTemplate).delete(KEY_101);
    verify(hashOperations).putAll(KEY_101, Map.of("_cached", "1"));
  }

  @Test
  @DisplayName("Apply change: should increment next reaction for new reaction")
  void should_increment_next_reaction_for_create() {
    givenHashOperations();

    reactionCountCache.applyReactionChange(101L, null, "like");

    verify(hashOperations).putIfAbsent(KEY_101, "_cached", "1");
    verify(hashOperations).increment(KEY_101, "like", 1);
    verify(hashOperations, never()).increment(KEY_101, "like", -1);
  }

  @Test
  @DisplayName("Apply change: should decrement previous and increment next for update")
  void should_move_count_for_reaction_update() {
    givenHashOperations();
    given(hashOperations.increment(KEY_101, "like", -1)).willReturn(4L);

    reactionCountCache.applyReactionChange(101L, "like", "love");

    verify(hashOperations).increment(KEY_101, "like", -1);
    verify(hashOperations).increment(KEY_101, "love", 1);
    verify(hashOperations, never()).delete(KEY_101, "like");
  }

  @Test
  @DisplayName("Apply change: should delete previous field when decrement reaches zero")
  void should_delete_previous_reaction_when_count_reaches_zero() {
    givenHashOperations();
    given(hashOperations.increment(KEY_101, "like", -1)).willReturn(0L);

    reactionCountCache.applyReactionChange(101L, "like", null);

    verify(hashOperations).increment(KEY_101, "like", -1);
    verify(hashOperations).delete(KEY_101, "like");
  }

  @Test
  @DisplayName("Apply change: should not write when reaction type does not change")
  void should_not_write_when_reaction_type_does_not_change() {
    reactionCountCache.applyReactionChange(101L, "like", "like");

    verify(hashOperations, never()).putIfAbsent(eq(KEY_101), eq("_cached"), eq("1"));
    verify(hashOperations, never()).increment(eq(KEY_101), eq("like"), eq(1L));
    verify(hashOperations, never()).increment(eq(KEY_101), eq("like"), eq(-1L));
  }

  @Test
  @DisplayName("Apply change: should not write when both previous and next are empty")
  void should_not_write_when_both_reactions_are_empty() {
    reactionCountCache.applyReactionChange(101L, null, null);

    verify(hashOperations, never()).putIfAbsent(eq(KEY_101), eq("_cached"), eq("1"));
    verify(hashOperations, never()).increment(eq(KEY_101), eq("like"), eq(1L));
    verify(hashOperations, never()).increment(eq(KEY_101), eq("like"), eq(-1L));
  }

  private void givenHashOperations() {
    given(stringRedisTemplate.opsForHash()).willReturn(hashOperations);
  }
}
