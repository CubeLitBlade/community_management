package io.github.cubelitblade.activity.dto;

import java.util.List;

public record RecentActivitiesResponse(List<ActivityView> items, boolean hasMore) {}
