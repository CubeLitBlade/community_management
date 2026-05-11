package io.github.cubelitblade.account.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.cubelitblade.account.exception.AccountStateException;
import io.github.cubelitblade.common.exception.ApiErrorCode;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AccountModelBranchTest {
  private static final Instant NOW = Instant.parse("2026-04-20T08:00:00Z");

  @Test
  void should_update_nickname_with_blank_fallback_and_custom_value() {
    Account account = account(Status.NORMAL);

    account.updateNickname(null, NOW);
    assertThat(account.getNickname()).isEqualTo("user");

    account.updateNickname(" ", NOW.plusSeconds(1));
    assertThat(account.getNickname()).isEqualTo("user");

    account.updateNickname("Custom", NOW.plusSeconds(2));
    assertThat(account.getNickname()).isEqualTo("Custom");
    assertThat(account.getUpdatedAt()).isEqualTo(NOW.plusSeconds(2));
  }

  @Test
  void should_update_only_present_contact_fields() {
    Account account = account(Status.NORMAL);

    account.updateContactInfo(null, " ", NOW);
    assertThat(account.getEmail()).isNull();
    assertThat(account.getPhone()).isNull();

    account.updateContactInfo("USER@Example.COM", "12345678901", NOW.plusSeconds(1));
    assertThat(account.getEmail().value()).isEqualTo("user@example.com");
    assertThat(account.getPhone().value()).isEqualTo("12345678901");
  }

  @Test
  void should_handle_status_transitions_and_idempotent_states() {
    Account suspended = account(Status.SUSPENDED);
    suspended.suspend(NOW);
    assertThat(suspended.getStatus()).isEqualTo(Status.SUSPENDED);

    Account normal = account(Status.NORMAL);
    normal.reactivate(NOW);
    assertThat(normal.getStatus()).isEqualTo(Status.NORMAL);

    Account archived = account(Status.ARCHIVED);
    archived.archive(NOW);
    assertThat(archived.getStatus()).isEqualTo(Status.ARCHIVED);
    assertThatThrownBy(() -> archived.suspend(NOW)).isInstanceOf(AccountStateException.class);
    assertThatThrownBy(() -> archived.reactivate(NOW)).isInstanceOf(AccountStateException.class);
  }

  @Test
  void should_reconstitute_null_snapshot_as_null_and_require_profile() {
    assertThat(Account.reconstitute(null)).isNull();
    assertThatThrownBy(() -> account(Status.NORMAL).updateProfile(null, NOW))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void should_compare_usernames_by_value_and_reject_too_long_archive_name() {
    Username username = Username.of("same");
    assertThat(username).isEqualTo(Username.of("same"));
    assertThat(username).isNotEqualTo(Username.of("other"));
    assertThat(username).isNotEqualTo("same");
    assertThat(username).hasSameHashCodeAs(Username.of("same"));
    assertThat(username.toString()).isEqualTo("same");

    Account account =
        Account.reconstitute(
            Account.Snapshot.builder()
                .id(1234567890123456789L)
                .username(Username.reconstitute("this_username_is_already_far_too_long"))
                .passwordHash(new PasswordHash("hash"))
                .nickname("this_username_is_already_far_too_long")
                .role(Role.USER)
                .status(Status.NORMAL)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build());

    assertThatThrownBy(() -> account.archive(NOW)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_cover_value_object_validation_edges() {
    assertThatThrownBy(() -> new PasswordHash(null)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new PasswordHash(" ")).isInstanceOf(IllegalArgumentException.class);
    assertThat(new PasswordHash("hash").toString()).isEqualTo("************");

    assertThat(new Profile(null).gender()).isZero();
    assertThat(new Profile(1).gender()).isEqualTo(1);
    assertThatThrownBy(() -> new Profile(3)).isInstanceOf(IllegalArgumentException.class);

    assertThat(Password.check(null)).contains(ApiErrorCode.INPUT_PASSWORD_BLANK);
    assertThat(Password.check("abc")).contains(ApiErrorCode.INPUT_PASSWORD_BAD_LENGTH);
    assertThat(Password.check("abcdef")).contains(ApiErrorCode.INPUT_PASSWORD_BAD_FORMAT);
    assertThat(Password.check("abc123")).isEmpty();
  }

  private Account account(Status status) {
    return Account.reconstitute(
        Account.Snapshot.builder()
            .id(1L)
            .username(Username.of("user"))
            .passwordHash(new PasswordHash("hash"))
            .nickname("user")
            .role(Role.USER)
            .status(status)
            .createdAt(NOW.minusSeconds(60))
            .updatedAt(NOW.minusSeconds(30))
            .build());
  }
}
