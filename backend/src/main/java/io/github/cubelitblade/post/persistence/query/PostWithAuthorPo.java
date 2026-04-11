package io.github.cubelitblade.post.persistence.query;

import java.time.Instant;

public record PostWithAuthorPo(
    // from posts
    Long id,
    Long authorId,
    String title,
    String content,
    String status,
    Instant createdAt,
    Instant updatedAt,

    // from accounts
    String username,
    String nickname) {}
