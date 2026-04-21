package io.github.cubelitblade.account.persistence;

import io.github.cubelitblade.account.model.Account;
import io.github.cubelitblade.account.model.Email;
import io.github.cubelitblade.account.model.PasswordHash;
import io.github.cubelitblade.account.model.Phone;
import io.github.cubelitblade.account.model.Profile;
import io.github.cubelitblade.account.model.Role;
import io.github.cubelitblade.account.model.Status;
import io.github.cubelitblade.account.model.Username;
import java.net.InetAddress;
import java.time.Instant;
import java.util.function.Function;
import lombok.Builder;

@Builder
public record AccountPo(
    Long id,
    String username,
    String passwordHash,
    Boolean mustChangePassword,
    String nickname,
    String email,
    String phone,
    Profile profile,
    String role,
    String status,
    Instant createdAt,
    Instant updatedAt,
    Instant lastLoginAt,
    InetAddress lastLoginIp) {

  public static AccountPo fromDomain(Account account) {
    if (account == null) {
      return null;
    }
    return AccountPo.builder()
        .id(account.getId())
        .username(mapIfNotNull(account.getUsername(), Username::value))
        .passwordHash(mapIfNotNull(account.getPasswordHash(), PasswordHash::value))
        .mustChangePassword(account.isMustChangePassword())
        .nickname(account.getNickname())
        .email(mapIfNotNull(account.getEmail(), Email::value))
        .phone(mapIfNotNull(account.getPhone(), Phone::value))
        .profile(account.getProfile())
        .role(mapIfNotNull(account.getRole(), Role::getValue))
        .status(mapIfNotNull(account.getStatus(), Status::getValue))
        .createdAt(account.getCreatedAt())
        .updatedAt(account.getUpdatedAt())
        .lastLoginAt(account.getLastLoginAt())
        .lastLoginIp(account.getLastLoginIp())
        .build();
  }

  public Account toDomain() {
    Account.Snapshot snapshot =
        Account.Snapshot.builder()
            .id(this.id)
            .username(mapIfNotNull(this.username, Username::reconstitute))
            .passwordHash(mapIfNotNull(this.passwordHash, PasswordHash::new))
            .mustChangePassword(Boolean.TRUE.equals(this.mustChangePassword))
            .nickname(this.nickname)
            .email(mapIfNotNull(this.email, Email::new))
            .phone(mapIfNotNull(this.phone, Phone::new))
            .profile(this.profile)
            .role(mapIfNotNull(this.role, Role::from))
            .status(mapIfNotNull(this.status, Status::from))
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .lastLoginAt(this.lastLoginAt)
            .lastLoginIp(this.lastLoginIp)
            .build();

    return Account.reconstitute(snapshot);
  }

  private static <S, T> T mapIfNotNull(S source, Function<S, T> mapper) {
    if (source == null) {
      return null;
    }
    return mapper.apply(source);
  }
}
