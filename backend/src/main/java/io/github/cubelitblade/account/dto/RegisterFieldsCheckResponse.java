package io.github.cubelitblade.account.dto;

import java.util.List;

public record RegisterFieldsCheckResponse(boolean available, List<String> reasons) {}
