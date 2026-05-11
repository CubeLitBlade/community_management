package io.github.cubelitblade.notification.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cubelitblade.notification.model.Notification;
import io.github.cubelitblade.notification.model.NotificationType;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class NotificationPersistenceIntegrationTest {
  private static final Instant NOW = Instant.parse("2026-04-20T08:00:00Z");

  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private NotificationRepository notificationRepository;

  @Test
  @DisplayName("NotificationRepository: should save and find notifications by recipient")
  void should_save_and_find_notifications_by_recipient() {
    Notification notification =
        Notification.create(
            20101L,
            20201L,
            20202L,
            NotificationType.POST_COMMENT,
            "post",
            20301L,
            "New comment",
            NOW);

    notificationRepository.save(NotificationPo.of(notification));
    insertNotification(
        20102L,
        20201L,
        20203L,
        "post_reaction",
        "post",
        20301L,
        "New reaction",
        false,
        null,
        NOW.plusSeconds(10));
    insertNotification(
        20103L,
        20299L,
        20202L,
        "post_comment",
        "post",
        20301L,
        "Other recipient",
        false,
        null,
        NOW.plusSeconds(20));

    List<NotificationPo> notifications = notificationRepository.findByRecipientAccountId(20201L);

    assertThat(notifications).extracting(NotificationPo::id).containsExactly(20102L, 20101L);
    assertThat(notifications).extracting(NotificationPo::recipientAccountId).containsOnly(20201L);
    assertThat(notifications.get(1).toNotification().getType())
        .isEqualTo(NotificationType.POST_COMMENT);
  }

  @Test
  @DisplayName("NotificationRepository: should filter by types and short-circuit empty type list")
  void should_filter_by_types_and_short_circuit_empty_type_list() {
    insertNotification(
        20111L, 20201L, 20202L, "post_comment", "post", 20301L, "Comment", false, null, NOW);
    insertNotification(
        20112L,
        20201L,
        20202L,
        "activity_update",
        "activity",
        20302L,
        "Activity",
        false,
        null,
        NOW.plusSeconds(10));
    insertNotification(
        20113L,
        20201L,
        20202L,
        "activity_reminder",
        "activity",
        20303L,
        "Reminder",
        false,
        null,
        NOW.plusSeconds(20));

    assertThat(
            notificationRepository.findByRecipientAccountIdAndTypes(
                20201L, List.of("activity_update", "activity_reminder")))
        .extracting(NotificationPo::id)
        .containsExactly(20113L, 20112L);
    assertThat(notificationRepository.findByRecipientAccountIdAndTypes(20201L, List.of()))
        .isEmpty();
  }

  @Test
  @DisplayName("NotificationRepository: should find only notification owned by recipient")
  void should_find_only_owned_notification() {
    insertNotification(
        20121L, 20201L, 20202L, "post_comment", "post", 20301L, "Comment", false, null, NOW);

    assertThat(notificationRepository.findOwnedById(20121L, 20201L)).isPresent();
    assertThat(notificationRepository.findOwnedById(20121L, 20299L)).isEmpty();
    assertThat(notificationRepository.findById(20121L)).isPresent();
  }

  @Test
  @DisplayName("NotificationRepository: should count unread notifications by recipient and type")
  void should_count_unread_notifications_by_recipient_and_type() {
    insertNotification(
        20131L, 20201L, 20202L, "post_comment", "post", 20301L, "Unread", false, null, NOW);
    insertNotification(
        20132L,
        20201L,
        20202L,
        "post_reaction",
        "post",
        20301L,
        "Read",
        true,
        NOW.plusSeconds(30),
        NOW.plusSeconds(10));
    insertNotification(
        20133L,
        20201L,
        20202L,
        "activity_update",
        "activity",
        20302L,
        "Unread activity",
        false,
        null,
        NOW.plusSeconds(20));
    insertNotification(
        20134L,
        20299L,
        20202L,
        "post_comment",
        "post",
        20301L,
        "Other recipient",
        false,
        null,
        NOW);

    assertThat(notificationRepository.countUnreadByRecipientAccountId(20201L)).isEqualTo(2);
    assertThat(
            notificationRepository.countUnreadByRecipientAccountIdAndTypes(
                20201L, List.of("post_comment")))
        .isEqualTo(1);
    assertThat(notificationRepository.countUnreadByRecipientAccountIdAndTypes(20201L, List.of()))
        .isZero();
  }

  @Test
  @DisplayName("NotificationRepository: should update one owned notification read status")
  void should_update_one_owned_notification_read_status() {
    insertNotification(
        20141L, 20201L, 20202L, "post_comment", "post", 20301L, "Owned", false, null, NOW);
    insertNotification(
        20142L, 20299L, 20202L, "post_comment", "post", 20301L, "Other", false, null, NOW);

    notificationRepository.updateReadStatus(
        NotificationPo.builder()
            .id(20141L)
            .recipientAccountId(20201L)
            .isRead(true)
            .readAt(NOW.plusSeconds(60))
            .build());
    notificationRepository.updateReadStatus(
        NotificationPo.builder()
            .id(20142L)
            .recipientAccountId(20201L)
            .isRead(true)
            .readAt(NOW.plusSeconds(60))
            .build());

    assertThat(notificationRepository.findById(20141L))
        .hasValueSatisfying(
            notification -> {
              assertThat(notification.isRead()).isTrue();
              assertThat(notification.readAt()).isEqualTo(NOW.plusSeconds(60));
            });
    assertThat(notificationRepository.findById(20142L))
        .hasValueSatisfying(
            notification -> {
              assertThat(notification.isRead()).isFalse();
              assertThat(notification.readAt()).isNull();
            });
  }

  @Test
  @DisplayName("NotificationRepository: should mark all unread notifications read for recipient")
  void should_mark_all_unread_notifications_read_for_recipient() {
    insertNotification(
        20151L, 20201L, 20202L, "post_comment", "post", 20301L, "Unread one", false, null, NOW);
    insertNotification(
        20152L,
        20201L,
        20202L,
        "activity_update",
        "activity",
        20302L,
        "Unread two",
        false,
        null,
        NOW.plusSeconds(10));
    insertNotification(
        20153L,
        20201L,
        20202L,
        "post_reaction",
        "post",
        20301L,
        "Already read",
        true,
        NOW.plusSeconds(30),
        NOW.plusSeconds(20));
    insertNotification(
        20154L,
        20299L,
        20202L,
        "post_comment",
        "post",
        20301L,
        "Other recipient",
        false,
        null,
        NOW);

    notificationRepository.markAllRead(20201L, NOW.plusSeconds(60));

    assertThat(notificationRepository.findById(20151L).orElseThrow().readAt())
        .isEqualTo(NOW.plusSeconds(60));
    assertThat(notificationRepository.findById(20152L).orElseThrow().readAt())
        .isEqualTo(NOW.plusSeconds(60));
    assertThat(notificationRepository.findById(20153L).orElseThrow().readAt())
        .isEqualTo(NOW.plusSeconds(30));
    assertThat(notificationRepository.findById(20154L).orElseThrow().isRead()).isFalse();
  }

  private void insertNotification(
      Long id,
      Long recipientAccountId,
      Long actorAccountId,
      String type,
      String targetType,
      Long targetId,
      String content,
      boolean isRead,
      Instant readAt,
      Instant createdAt) {
    jdbcTemplate.update(
        """
        insert into notifications(
          id, recipient_account_id, actor_account_id, type, target_type,
          target_id, content, is_read, read_at, created_at
        )
        values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        id,
        recipientAccountId,
        actorAccountId,
        type,
        targetType,
        targetId,
        content,
        isRead,
        ts(readAt),
        ts(createdAt));
  }

  private Timestamp ts(Instant instant) {
    return instant == null ? null : Timestamp.from(instant);
  }
}
