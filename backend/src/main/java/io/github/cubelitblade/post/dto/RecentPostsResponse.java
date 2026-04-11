package io.github.cubelitblade.post.dto;

import java.util.List;

public record RecentPostsResponse(List<PostResponse> items, boolean hasMore) {}
