package io.github.cubelitblade.reaction.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public enum ReactionType {
  LIKE("like"),
  LOVE("love"),
  LAUGH("laugh"),
  SAD("sad");

  private static final Map<String, ReactionType> map =
      Arrays.stream(ReactionType.values())
          .collect(Collectors.toMap(ReactionType::getValue, v -> v));

  private final String value;

  ReactionType(String value) {
    this.value = value;
  }

  public static ReactionType from(String value) {
    ReactionType reactionType = map.get(value);
    if (reactionType == null) {
      throw new IllegalArgumentException("Unknown reaction type: " + value);
    }
    return reactionType;
  }
}
