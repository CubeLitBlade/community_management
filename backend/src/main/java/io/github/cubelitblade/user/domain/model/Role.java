package io.github.cubelitblade.user.domain.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
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
            Collectors.toMap(Role::getRole, v -> v)
    );
    @EnumValue
    private final String role;

    Role(String role) {
        this.role = role;
    }

    public static Role from(String roleName) {
        Role role = map.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Unknown role: " + roleName);
        }
        return role;
    }
}
