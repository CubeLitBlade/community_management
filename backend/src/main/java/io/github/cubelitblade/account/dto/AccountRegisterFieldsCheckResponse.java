package io.github.cubelitblade.account.dto;

import java.util.List;

public record AccountRegisterFieldsCheckResponse(boolean isAvailable, List<String> reasons) {}
