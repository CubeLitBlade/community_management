package io.github.cubelitblade.message.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cubelitblade.message.model.PrivateMessage;
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
class PrivateMessagePersistenceIntegrationTest {
  private static final Instant NOW = Instant.parse("2026-04-20T08:00:00Z");

  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private PrivateMessageRepository privateMessageRepository;

  @Test
  @DisplayName("PrivateMessageRepository: should save and find participant messages")
  void should_save_and_find_participant_messages() {
    PrivateMessage message = PrivateMessage.create(30101L, 30201L, 30202L, "Hello", NOW);

    privateMessageRepository.save(PrivateMessagePo.of(message));
    insertMessage(30102L, 30203L, 30201L, "Incoming", false, null, NOW.plusSeconds(10));
    insertMessage(30103L, 30202L, 30203L, "Unrelated", false, null, NOW.plusSeconds(20));

    List<PrivateMessagePo> messages = privateMessageRepository.findByParticipantAccountId(30201L);

    assertThat(messages).extracting(PrivateMessagePo::id).containsExactly(30102L, 30101L);
    assertThat(messages.get(1).toPrivateMessage().getContent()).isEqualTo("Hello");
  }

  @Test
  @DisplayName("PrivateMessageRepository: should find two-way conversation ordered by time")
  void should_find_two_way_conversation_ordered_by_time() {
    insertMessage(30111L, 30201L, 30202L, "Old outgoing", false, null, NOW);
    insertMessage(30112L, 30202L, 30201L, "Incoming", false, null, NOW.plusSeconds(10));
    insertMessage(30113L, 30201L, 30203L, "Other contact", false, null, NOW.plusSeconds(20));
    insertMessage(30114L, 30203L, 30202L, "Unrelated", false, null, NOW.plusSeconds(30));
    insertMessage(30115L, 30202L, 30201L, "Latest incoming", false, null, NOW.plusSeconds(40));

    List<PrivateMessagePo> conversation = privateMessageRepository.findConversation(30201L, 30202L);

    assertThat(conversation)
        .extracting(PrivateMessagePo::id)
        .containsExactly(30111L, 30112L, 30115L);
    assertThat(conversation)
        .extracting(PrivateMessagePo::senderAccountId)
        .containsExactly(30201L, 30202L, 30202L);
  }

  @Test
  @DisplayName(
      "PrivateMessageRepository: should mark only incoming unread conversation messages read")
  void should_mark_only_incoming_unread_conversation_messages_read() {
    insertMessage(30121L, 30202L, 30201L, "Unread incoming", false, null, NOW);
    insertMessage(
        30122L,
        30202L,
        30201L,
        "Already read incoming",
        true,
        NOW.plusSeconds(20),
        NOW.plusSeconds(10));
    insertMessage(30123L, 30201L, 30202L, "Outgoing", false, null, NOW.plusSeconds(20));
    insertMessage(30124L, 30203L, 30201L, "Other sender", false, null, NOW.plusSeconds(30));

    privateMessageRepository.markConversationRead(30201L, 30202L, NOW.plusSeconds(60));

    assertThat(readMessage(30121L).isRead()).isTrue();
    assertThat(readMessage(30121L).readAt()).isEqualTo(NOW.plusSeconds(60));
    assertThat(readMessage(30122L).readAt()).isEqualTo(NOW.plusSeconds(20));
    assertThat(readMessage(30123L).isRead()).isFalse();
    assertThat(readMessage(30124L).isRead()).isFalse();
  }

  private PrivateMessagePo readMessage(Long id) {
    List<PrivateMessagePo> messages = privateMessageRepository.findByParticipantAccountId(30201L);
    return messages.stream().filter(message -> message.id().equals(id)).findFirst().orElseThrow();
  }

  private void insertMessage(
      Long id,
      Long senderAccountId,
      Long recipientAccountId,
      String content,
      boolean isRead,
      Instant readAt,
      Instant createdAt) {
    jdbcTemplate.update(
        """
        insert into private_messages(
          id, sender_account_id, recipient_account_id, content, is_read, read_at, created_at
        )
        values (?, ?, ?, ?, ?, ?, ?)
        """,
        id,
        senderAccountId,
        recipientAccountId,
        content,
        isRead,
        ts(readAt),
        ts(createdAt));
  }

  private Timestamp ts(Instant instant) {
    return instant == null ? null : Timestamp.from(instant);
  }
}
