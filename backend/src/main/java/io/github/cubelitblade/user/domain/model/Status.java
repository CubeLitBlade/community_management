package io.github.cubelitblade.user.domain.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum Status {
    NORMAL("normal"),
    SUSPENDED("suspended"),
    ARCHIVED("archived");

    private static final Map<String, Status> map = Arrays.stream(Status.values()).collect(
            Collectors.toMap(Status::getValue, v -> v)
    );
    private final String value;

    Status(String value) {
        this.value = value;
    }

    public static Status from(String statusName) {
        Status status = map.get(statusName);
        if (status == null) {
            throw new IllegalArgumentException("Unknown status: " + statusName);
        }
        return status;
    }
}
