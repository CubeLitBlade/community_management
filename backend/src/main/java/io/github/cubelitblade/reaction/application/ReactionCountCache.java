package io.github.cubelitblade.reaction.application;

import io.github.cubelitblade.reaction.persistence.query.ReactionCountVo;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReactionCountCache {

  private static final String KEY_PREFIX = "post:reaction:counts:";
  private static final String CACHE_MARKER_FIELD = "_cached";
  private static final String CACHE_MARKER_VALUE = "1";

  private final StringRedisTemplate stringRedisTemplate;

  public Map<Long, List<ReactionCountVo>> getPostReactionCounts(
      List<Long> postIds, Function<List<Long>, Map<Long, List<ReactionCountVo>>> fallbackLoader) {
    Map<Long, List<ReactionCountVo>> result = new LinkedHashMap<>();
    List<Long> missingPostIds = new ArrayList<>();

    for (Long postId : postIds) {
      Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(buildKey(postId));
      if (entries.isEmpty()) {
        missingPostIds.add(postId);
        continue;
      }

      result.put(postId, toReactionCounts(entries));
    }

    if (missingPostIds.isEmpty()) {
      return result;
    }

    Map<Long, List<ReactionCountVo>> loadedCounts = fallbackLoader.apply(missingPostIds);
    for (Long postId : missingPostIds) {
      List<ReactionCountVo> counts = loadedCounts.getOrDefault(postId, List.of());
      putPostReactionCounts(postId, counts);
      result.put(postId, counts);
    }

    return result;
  }

  public void applyReactionChange(
      Long postId, String previousReactionType, String nextReactionType) {
    if (previousReactionType != null && !previousReactionType.equals(nextReactionType)) {
      decrement(postId, previousReactionType);
    }

    if (nextReactionType != null && !nextReactionType.equals(previousReactionType)) {
      increment(postId, nextReactionType);
    }
  }

  private void increment(Long postId, String reactionType) {
    String key = buildKey(postId);
    stringRedisTemplate.opsForHash().putIfAbsent(key, CACHE_MARKER_FIELD, CACHE_MARKER_VALUE);
    stringRedisTemplate.opsForHash().increment(key, reactionType, 1);
  }

  private void decrement(Long postId, String reactionType) {
    String key = buildKey(postId);
    stringRedisTemplate.opsForHash().putIfAbsent(key, CACHE_MARKER_FIELD, CACHE_MARKER_VALUE);
    Long updatedCount = stringRedisTemplate.opsForHash().increment(key, reactionType, -1);
    if (updatedCount != null && updatedCount <= 0) {
      stringRedisTemplate.opsForHash().delete(key, reactionType);
    }
  }

  private void putPostReactionCounts(Long postId, List<ReactionCountVo> counts) {
    String key = buildKey(postId);
    stringRedisTemplate.delete(key);

    Map<String, String> values = new LinkedHashMap<>();
    values.put(CACHE_MARKER_FIELD, CACHE_MARKER_VALUE);
    for (ReactionCountVo count : counts) {
      values.put(count.reactionType(), Long.toString(count.count()));
    }

    stringRedisTemplate.opsForHash().putAll(key, values);
  }

  private List<ReactionCountVo> toReactionCounts(Map<Object, Object> entries) {
    List<ReactionCountVo> counts = new ArrayList<>();
    for (Map.Entry<Object, Object> entry : entries.entrySet()) {
      String field = entry.getKey().toString();
      if (CACHE_MARKER_FIELD.equals(field)) {
        continue;
      }

      counts.add(new ReactionCountVo(field, Long.parseLong(entry.getValue().toString())));
    }
    return counts;
  }

  private String buildKey(Long postId) {
    return KEY_PREFIX + postId;
  }
}
