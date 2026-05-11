package io.github.cubelitblade.message.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.github.cubelitblade.account.exception.AccountNotFoundException;
import io.github.cubelitblade.account.model.Account;
import io.github.cubelitblade.account.model.Role;
import io.github.cubelitblade.account.model.Status;
import io.github.cubelitblade.account.model.Username;
import io.github.cubelitblade.account.persistence.AccountRepository;
import io.github.cubelitblade.common.exception.ValidationException;
import io.github.cubelitblade.common.id.SnowflakeIdGenerator;
import io.github.cubelitblade.common.time.TimeProvider;
import io.github.cubelitblade.message.dto.PrivateConversationListResponse;
import io.github.cubelitblade.message.dto.PrivateMessageListResponse;
import io.github.cubelitblade.message.dto.PrivateMessageResponse;
import io.github.cubelitblade.message.dto.SendPrivateMessageRequest;
import io.github.cubelitblade.message.model.PrivateMessage;
import io.github.cubelitblade.message.persistence.PrivateMessagePo;
import io.github.cubelitblade.message.persistence.PrivateMessageRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PrivateMessageServiceTest {
  private static final Instant NOW = Instant.parse("2026-04-15T08:00:00Z");

  @Mock private PrivateMessageRepository privateMessageRepository;
  @Mock private AccountRepository accountRepository;
  @Mock private SnowflakeIdGenerator idGenerator;
  @Mock private TimeProvider timeProvider;

  private PrivateMessageService privateMessageService;

  @BeforeEach
  void setUp() {
    privateMessageService =
        new PrivateMessageService(
            privateMessageRepository, accountRepository, idGenerator, timeProvider);
    lenient().when(timeProvider.now()).thenReturn(NOW);
  }

  @Test
  @DisplayName("Send: should persist a trimmed private message")
  void should_send_private_message() {
    given(accountRepository.findAccountById(2L)).willReturn(Optional.of(account(2L, "alice")));
    given(idGenerator.nextId()).willReturn(99L);

    PrivateMessageResponse response =
        privateMessageService.sendMessage(1L, new SendPrivateMessageRequest(2L, "  hello  "));

    ArgumentCaptor<PrivateMessagePo> captor = ArgumentCaptor.forClass(PrivateMessagePo.class);
    verify(privateMessageRepository).save(captor.capture());
    assertThat(response.id()).isEqualTo(99L);
    assertThat(response.content()).isEqualTo("hello");
    assertThat(captor.getValue().senderAccountId()).isEqualTo(1L);
    assertThat(captor.getValue().recipientAccountId()).isEqualTo(2L);
    assertThat(captor.getValue().content()).isEqualTo("hello");
    assertThat(captor.getValue().isRead()).isFalse();
  }

  @Test
  @DisplayName("Send: should reject self messages")
  void should_reject_self_message() {
    assertThatThrownBy(
            () -> privateMessageService.sendMessage(1L, new SendPrivateMessageRequest(1L, "hello")))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  @DisplayName("Send: should reject missing recipient")
  void should_reject_missing_recipient() {
    assertThatThrownBy(() -> privateMessageService.sendMessage(1L, null))
        .isInstanceOf(ValidationException.class);
    assertThatThrownBy(
            () ->
                privateMessageService.sendMessage(1L, new SendPrivateMessageRequest(null, "hello")))
        .isInstanceOf(ValidationException.class);

    verify(privateMessageRepository, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  @DisplayName("Send: should reject unknown or inactive recipient")
  void should_reject_unknown_or_inactive_recipient() {
    given(accountRepository.findAccountById(2L)).willReturn(Optional.empty());
    given(accountRepository.findAccountById(3L))
        .willReturn(Optional.of(account(3L, "bob", Status.SUSPENDED)));

    assertThatThrownBy(
            () -> privateMessageService.sendMessage(1L, new SendPrivateMessageRequest(2L, "hello")))
        .isInstanceOf(AccountNotFoundException.class);
    assertThatThrownBy(
            () -> privateMessageService.sendMessage(1L, new SendPrivateMessageRequest(3L, "hello")))
        .isInstanceOf(ValidationException.class);

    verify(privateMessageRepository, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  @DisplayName("Send: should reject blank content")
  void should_reject_blank_content() {
    given(accountRepository.findAccountById(2L)).willReturn(Optional.of(account(2L, "alice")));

    assertThatThrownBy(
            () -> privateMessageService.sendMessage(1L, new SendPrivateMessageRequest(2L, " ")))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  @DisplayName("Send: should reject content longer than limit")
  void should_reject_too_long_content() {
    given(accountRepository.findAccountById(2L)).willReturn(Optional.of(account(2L, "alice")));

    assertThatThrownBy(
            () ->
                privateMessageService.sendMessage(
                    1L, new SendPrivateMessageRequest(2L, "x".repeat(1001))))
        .isInstanceOf(ValidationException.class);

    verify(privateMessageRepository, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  @DisplayName("List: should group messages into conversations with unread counts")
  void should_group_conversations() {
    PrivateMessage incomingUnread =
        PrivateMessage.reconstitute(
            PrivateMessage.Snapshot.builder()
                .id(3L)
                .senderAccountId(2L)
                .recipientAccountId(1L)
                .content("new")
                .read(false)
                .createdAt(NOW.plusSeconds(20))
                .build());
    PrivateMessage outgoing = PrivateMessage.create(2L, 1L, 2L, "old", NOW.plusSeconds(10));
    given(privateMessageRepository.findByParticipantAccountId(1L))
        .willReturn(List.of(PrivateMessagePo.of(incomingUnread), PrivateMessagePo.of(outgoing)));
    given(accountRepository.findAccountsByIds(List.of(2L)))
        .willReturn(List.of(account(2L, "alice")));

    PrivateConversationListResponse response = privateMessageService.getConversations(1L);

    assertThat(response.conversations()).hasSize(1);
    assertThat(response.conversations().getFirst().contactAccountId()).isEqualTo(2L);
    assertThat(response.conversations().getFirst().lastMessage()).isEqualTo("new");
    assertThat(response.conversations().getFirst().unreadCount()).isEqualTo(1L);
  }

  @Test
  @DisplayName("List: should sort conversations and filter missing contacts")
  void should_sort_conversations_and_filter_missing_contacts() {
    PrivateMessage olderContact = PrivateMessage.create(1L, 1L, 2L, "older", NOW.plusSeconds(10));
    PrivateMessage newerContact =
        PrivateMessage.reconstitute(
            PrivateMessage.Snapshot.builder()
                .id(2L)
                .senderAccountId(3L)
                .recipientAccountId(1L)
                .content("newer")
                .read(false)
                .createdAt(NOW.plusSeconds(30))
                .build());
    PrivateMessage missingContact =
        PrivateMessage.create(3L, 1L, 4L, "missing", NOW.plusSeconds(40));
    given(privateMessageRepository.findByParticipantAccountId(1L))
        .willReturn(
            List.of(
                PrivateMessagePo.of(olderContact),
                PrivateMessagePo.of(newerContact),
                PrivateMessagePo.of(missingContact)));
    given(accountRepository.findAccountsByIds(List.of(2L, 3L, 4L)))
        .willReturn(List.of(account(2L, "alice"), account(3L, "bob")));

    PrivateConversationListResponse response = privateMessageService.getConversations(1L);

    assertThat(response.conversations())
        .extracting("contactAccountId", "lastMessage", "unreadCount")
        .containsExactly(
            org.assertj.core.groups.Tuple.tuple(3L, "newer", 1L),
            org.assertj.core.groups.Tuple.tuple(2L, "older", 0L));
  }

  @Test
  @DisplayName("Conversation: should mark contact messages read before returning history")
  void should_mark_conversation_read() {
    given(accountRepository.findAccountById(2L)).willReturn(Optional.of(account(2L, "alice")));
    PrivateMessage message = PrivateMessage.create(10L, 2L, 1L, "hello", NOW);
    given(privateMessageRepository.findConversation(1L, 2L))
        .willReturn(List.of(PrivateMessagePo.of(message)));

    PrivateMessageListResponse response = privateMessageService.getConversation(1L, 2L);

    verify(privateMessageRepository).markConversationRead(1L, 2L, NOW);
    assertThat(response.messages()).hasSize(1);
    assertThat(response.messages().getFirst().content()).isEqualTo("hello");
  }

  @Test
  @DisplayName("Conversation: should reject missing contact")
  void should_reject_missing_contact_when_getting_conversation() {
    given(accountRepository.findAccountById(404L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> privateMessageService.getConversation(1L, 404L))
        .isInstanceOf(AccountNotFoundException.class);

    verify(privateMessageRepository, never())
        .markConversationRead(
            org.mockito.ArgumentMatchers.anyLong(),
            org.mockito.ArgumentMatchers.anyLong(),
            org.mockito.ArgumentMatchers.any());
  }

  @Test
  @DisplayName("Mark read: should require existing contact")
  void should_mark_conversation_read_for_existing_contact() {
    given(accountRepository.findAccountById(2L)).willReturn(Optional.of(account(2L, "alice")));

    privateMessageService.markConversationRead(1L, 2L);

    verify(privateMessageRepository).markConversationRead(1L, 2L, NOW);
    assertThatThrownBy(() -> privateMessageService.markConversationRead(1L, null))
        .isInstanceOf(ValidationException.class);
  }

  private Account account(Long id, String username) {
    return account(id, username, Status.NORMAL);
  }

  private Account account(Long id, String username, Status status) {
    return Account.reconstitute(
        Account.Snapshot.builder()
            .id(id)
            .username(Username.reconstitute(username))
            .nickname(username)
            .role(Role.USER)
            .status(status)
            .createdAt(NOW)
            .updatedAt(NOW)
            .build());
  }
}
