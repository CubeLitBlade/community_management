package io.github.cubelitblade.account.domain.model;

import io.github.cubelitblade.account.domain.exception.AccountArchivedException;
import io.github.cubelitblade.account.domain.exception.AccountSuspendedException;
import io.github.cubelitblade.account.domain.exception.InvalidCredentialsException;
import io.github.cubelitblade.account.domain.service.PasswordService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {
    private Long id;
    private Username username;
    private PasswordHash passwordHash;
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

    public static Account register(Username username, String submittedPassword, PasswordService hasher, Instant now) {
        Account account = new Account();
        account.username = username;
        account.nickname = username.value();
        account.passwordHash = hasher.fromRaw(submittedPassword);
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
     * @throws AccountArchivedException  if the account has been archived.
     * @throws AccountSuspendedException if the account has been suspended.
     */
    public void requireNormalStatus() {
        if (this.status == Status.ARCHIVED) {
            throw new AccountArchivedException();
        }
        if (this.status == Status.SUSPENDED) {
            throw new AccountSuspendedException();
        }
    }

    public boolean passwordMatches(String submittedPassword, PasswordService passwordService) {
        return passwordService.matches(submittedPassword, this.passwordHash);
    }

    public void recordLoginSuccess(InetAddress ip, Instant now) {
        this.lastLoginIp = ip;
        this.lastLoginAt = now;
    }

    /**
     * Changes the password. Requires verification of the current password.
     *
     * @throws InvalidCredentialsException if the current password is incorrect.
     */
    public void changePassword(String currentPassword, String newPassword, PasswordService passwordService, Instant now) {
        if (!passwordService.matches(currentPassword, this.passwordHash)) {
            throw new InvalidCredentialsException();
        }
        this.passwordHash = passwordService.fromRaw(newPassword);
        this.touch(now);
    }

    /**
     * Resets the password without verifying the current password.
     */
    public void resetPassword(String newPassword, PasswordService passwordService, Instant now) {
        this.passwordHash = passwordService.fromRaw(newPassword);
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

    public void updateContactInfo(Email email, Phone phone, Instant now) {
        this.email = Objects.requireNonNullElse(email, this.email);
        this.phone = Objects.requireNonNullElse(phone, this.phone);
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
     * @throws AccountArchivedException if this account has been archived.
     */
    public void suspend(Instant now) {
        if (this.status == Status.SUSPENDED) {
            return;
        } else if (this.status == Status.ARCHIVED) {
            throw new AccountArchivedException();
        }
        this.status = Status.SUSPENDED;
        this.touch(now);
    }

    /**
     * Reactivates this account.
     *
     * @throws AccountArchivedException if this account has been archived.
     */
    public void reactivate(Instant now) {
        if (this.status == Status.NORMAL) {
            return;
        } else if (this.status == Status.ARCHIVED) {
            throw new AccountArchivedException();
        }
        this.status = Status.NORMAL;
        this.touch(now);
    }

    /**
     * Archives this account.
     * <p>
     * This action is irreversible and will erase all sensitive personal information.
     * </p>
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

    public void initIdentifier(long id) {
        if (this.id != null) {
            throw new IllegalStateException("Identifier already initialized.");
        }
        if (id <= 0) {
            throw new IllegalArgumentException("Identifier must be greater than zero, got: " + id);
        }
        this.id = id;
    }

    private void touch(Instant now) {
        this.updatedAt = now;
    }

    @Builder
    public record Snapshot(
            Long id,
            Username username,
            PasswordHash passwordHash,
            String nickname,
            Email email,
            Phone phone,
            Profile profile,
            Role role,
            Status status,
            Instant createdAt,
            Instant updatedAt,
            Instant lastLoginAt,
            InetAddress lastLoginIp
    ) {
    }
}