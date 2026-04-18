package io.github.cubelitblade.activity.dto;

import java.util.List;

public record MyActivitiesResponse(List<ActivityView> created, List<ActivityView> registered) {}
