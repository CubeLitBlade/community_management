package io.github.cubelitblade.account.model;

import io.github.cubelitblade.account.application.PasswordHasher;
import io.github.cubelitblade.account.exception.AccountStateException;
import io.github.cubelitblade.account.exception.LoginFailedException;
import io.github.cubelitblade.common.exception.ApiErrorCode;
import java.net.InetAddress;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {
  private Long id;
  private Username username;
  private PasswordHash passwordHash;
  private boolean mustChangePassword;
  private String nickname;
  private Email email;
  private Phone phone;
  private Profile profile;
  private Role role;
  private Status status;
  private Instant createdAt;
  private Instant updatedAt;
  private Instant lastLoginAt;
  private InetAddress lastLoginIp;

  public static Account register(
      Long id, Username username, String submittedPassword, PasswordHasher hasher, Instant now) {
    Account account = new Account();
    account.id = id;
    account.username = username;
    account.nickname = username.value();
    account.passwordHash = hasher.fromRaw(submittedPassword);
    account.mustChangePassword = false;
    account.role = Role.USER;
    account.status = Status.NORMAL;
    account.createdAt = now;
    account.touch(now);
    return account;
  }

  public static Account reconstitute(Snapshot snapshot) {
    if (snapshot == null) return null;

    Account account = new Account();
    account.id = snapshot.id;
    account.username = snapshot.username;
    account.passwordHash = snapshot.passwordHash;
    account.mustChangePassword = snapshot.mustChangePassword;
    account.nickname = snapshot.nickname;
    account.email = snapshot.email;
    account.phone = snapshot.phone;
    account.profile = snapshot.profile;
    account.role = snapshot.role;
    account.status = snapshot.status;
    account.createdAt = snapshot.createdAt;
    account.updatedAt = snapshot.updatedAt;
    account.lastLoginAt = snapshot.lastLoginAt;
    account.lastLoginIp = snapshot.lastLoginIp;

    return account;
  }

  /**
   * Requires this account to be in {@link Status#NORMAL} status.
   *
   * @throws AccountStateException if the account has been archived or suspended.
   */
  public void requireNormalStatus() {
    if (this.status == Status.ARCHIVED) {
      throw AccountStateException.from(ApiErrorCode.ACCOUNT_STATE_ARCHIVED);
    }
    if (this.status == Status.SUSPENDED) {
      throw AccountStateException.from(ApiErrorCode.ACCOUNT_STATE_SUSPENDED);
    }
  }

  public boolean passwordMatches(String submittedPassword, PasswordHasher passwordHasher) {
    return passwordHasher.matches(submittedPassword, this.passwordHash);
  }

  public void recordLoginSuccess(InetAddress ip, Instant now) {
    this.lastLoginIp = ip;
    this.lastLoginAt = now;
  }

  /**
   * Changes the password. Requires verification of the current password.
   *
   * @throws LoginFailedException if the current password is incorrect.
   */
  public void changePassword(
      String currentPassword, String newPassword, PasswordHasher passwordHasher, Instant now) {
    if (!passwordHasher.matches(currentPassword, this.passwordHash)) {
      throw LoginFailedException.from(ApiErrorCode.LOGIN_FAILED_INVALID_CREDENTIALS);
    }
    this.passwordHash = passwordHasher.fromRaw(newPassword);
    this.mustChangePassword = false;
    this.touch(now);
  }

  /** Resets the password without verifying the current password. */
  public void resetPassword(String newPassword, PasswordHasher passwordHasher, Instant now) {
    this.passwordHash = passwordHasher.fromRaw(newPassword);
    this.mustChangePassword = true;
    this.touch(now);
  }

  public void updateNickname(String nickname, Instant now) {
    if (nickname == null || nickname.isBlank()) {
      this.nickname = this.username.value();
    } else {
      this.nickname = nickname;
    }
    this.touch(now);
  }

  public void updateContactInfo(String email, String phone, Instant now) {
    if (email != null && !email.isBlank()) {
      this.email = Email.of(email);
    }
    if (phone != null && !phone.isBlank()) {
      this.phone = Phone.of(phone);
    }
    this.touch(now);
  }

  public void updateProfile(Profile profile, Instant now) {
    this.profile = Objects.requireNonNull(profile);
    this.touch(now);
  }

  public void assignRole(Role role, Instant now) {
    this.role = Objects.requireNonNull(role);
    this.touch(now);
  }

  /**
   * Suspends this account.
   *
   * @throws AccountStateException if this account has been archived.
   */
  public void suspend(Instant now) {
    if (this.status == Status.SUSPENDED) {
      return;
    } else if (this.status == Status.ARCHIVED) {
      throw AccountStateException.from(ApiErrorCode.ACCOUNT_STATE_ARCHIVED);
    }
    this.status = Status.SUSPENDED;
    this.touch(now);
  }

  /**
   * Reactivates this account.
   *
   * @throws AccountStateException if this account has been archived.
   */
  public void reactivate(Instant now) {
    if (this.status == Status.NORMAL) {
      return;
    } else if (this.status == Status.ARCHIVED) {
      throw AccountStateException.from(ApiErrorCode.ACCOUNT_STATE_ARCHIVED);
    }
    this.status = Status.NORMAL;
    this.touch(now);
  }

  /**
   * Archives this account.
   *
   * <p>This action is irreversible and will erase all sensitive personal information.
   */
  public void archive(Instant now) {
    if (this.status == Status.ARCHIVED) {
      return;
    }
    this.status = Status.ARCHIVED;

    // Invalidate the original username for future registration
    this.username = Username.archiveFrom(this.username, this.id);

    // Erase sensitive personal data
    this.email = null;
    this.phone = null;
    this.profile = null;
    this.touch(now);
  }

  private void touch(Instant now) {
    this.updatedAt = now;
  }

  @Builder
  public record Snapshot(
      Long id,
      Username username,
      PasswordHash passwordHash,
      boolean mustChangePassword,
      String nickname,
      Email email,
      Phone phone,
      Profile profile,
      Role role,
      Status status,
      Instant createdAt,
      Instant updatedAt,
      Instant lastLoginAt,
      InetAddress lastLoginIp) {}
}
