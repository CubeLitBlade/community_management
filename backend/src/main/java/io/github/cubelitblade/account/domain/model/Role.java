package io.github.cubelitblade.account.domain.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum Role {
    OWNER("owner"),
    ADMIN("admin"),
    USER("user");

    private static final Map<String, Role> map = Arrays.stream(Role.values()).collect(
            Collectors.toMap(Role::getValue, v -> v)
    );
    private final String value;

    Role(String value) {
        this.value = value;
    }

    public static Role from(String roleName) {
        Role role = map.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Unknown role: " + roleName);
        }
        return role;
    }
}
