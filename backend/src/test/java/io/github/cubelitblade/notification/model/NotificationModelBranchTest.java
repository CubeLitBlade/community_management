package io.github.cubelitblade.notification.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class NotificationModelBranchTest {
  private static final Instant NOW = Instant.parse("2026-04-20T08:00:00Z");

  @Test
  void should_mark_read_idempotently_and_reconstitute_null() {
    Notification notification =
        Notification.create(1L, 2L, 3L, NotificationType.POST_COMMENT, "post", 4L, "content", NOW);

    notification.markRead(NOW.plusSeconds(1));
    notification.markRead(NOW.plusSeconds(2));

    assertThat(notification.isRead()).isTrue();
    assertThat(notification.getReadAt()).isEqualTo(NOW.plusSeconds(1));
    assertThat(Notification.reconstitute(null)).isNull();
  }

  @Test
  void should_parse_notification_scope_values() {
    assertThat(NotificationScope.from(null)).isEqualTo(NotificationScope.ALL);
    assertThat(NotificationScope.from(" ")).isEqualTo(NotificationScope.ALL);
    assertThat(NotificationScope.from(" replies ")).isEqualTo(NotificationScope.REPLIES);
    assertThat(NotificationScope.REACTIONS.typeValues())
        .containsExactly(NotificationType.POST_REACTION.getValue());
    assertThat(NotificationScope.NOTIFICATIONS.typeValues())
        .containsExactly(
            NotificationType.ACTIVITY_REMINDER.getValue(),
            NotificationType.ACTIVITY_UPDATE.getValue());
    assertThatThrownBy(() -> NotificationScope.from("missing"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
