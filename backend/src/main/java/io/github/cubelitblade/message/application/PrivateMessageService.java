package io.github.cubelitblade.message.application;

import io.github.cubelitblade.account.exception.AccountNotFoundException;
import io.github.cubelitblade.account.model.Account;
import io.github.cubelitblade.account.model.Status;
import io.github.cubelitblade.account.persistence.AccountRepository;
import io.github.cubelitblade.common.exception.ApiErrorCode;
import io.github.cubelitblade.common.exception.ValidationException;
import io.github.cubelitblade.common.id.SnowflakeIdGenerator;
import io.github.cubelitblade.common.time.TimeProvider;
import io.github.cubelitblade.message.dto.PrivateConversationListResponse;
import io.github.cubelitblade.message.dto.PrivateConversationResponse;
import io.github.cubelitblade.message.dto.PrivateMessageListResponse;
import io.github.cubelitblade.message.dto.PrivateMessageResponse;
import io.github.cubelitblade.message.dto.SendPrivateMessageRequest;
import io.github.cubelitblade.message.model.PrivateMessage;
import io.github.cubelitblade.message.persistence.PrivateMessagePo;
import io.github.cubelitblade.message.persistence.PrivateMessageRepository;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PrivateMessageService {
  private static final int MAX_CONTENT_LENGTH = 1000;

  private final PrivateMessageRepository privateMessageRepository;
  private final AccountRepository accountRepository;
  private final SnowflakeIdGenerator idGenerator;
  private final TimeProvider timeProvider;

  @Transactional
  public PrivateMessageResponse sendMessage(
      Long senderAccountId, SendPrivateMessageRequest request) {
    Long recipientAccountId = request == null ? null : request.recipientAccountId();
    if (recipientAccountId == null) {
      throw new ValidationException(ApiErrorCode.INVALID_REQUEST, "Recipient account is required");
    }
    if (senderAccountId.equals(recipientAccountId)) {
      throw new ValidationException(ApiErrorCode.INVALID_REQUEST, "Cannot send a message to self");
    }

    Account recipient =
        accountRepository
            .findAccountById(recipientAccountId)
            .orElseThrow(AccountNotFoundException::notFound);
    if (recipient.getStatus() != Status.NORMAL) {
      throw new ValidationException(
          ApiErrorCode.INVALID_REQUEST, "Recipient account is not active");
    }

    String content = normalizeContent(request.content());
    Instant now = timeProvider.now();
    PrivateMessage message =
        PrivateMessage.create(
            idGenerator.nextId(), senderAccountId, recipientAccountId, content, now);
    privateMessageRepository.save(PrivateMessagePo.of(message));
    return PrivateMessageResponse.from(message);
  }

  @Transactional(readOnly = true)
  public PrivateConversationListResponse getConversations(Long accountId) {
    List<PrivateMessage> messages =
        privateMessageRepository.findByParticipantAccountId(accountId).stream()
            .map(PrivateMessagePo::toPrivateMessage)
            .toList();

    Map<Long, List<PrivateMessage>> messagesByContact = new LinkedHashMap<>();
    for (PrivateMessage message : messages) {
      Long contactAccountId = contactAccountId(accountId, message);
      messagesByContact.computeIfAbsent(contactAccountId, _ -> new ArrayList<>()).add(message);
    }

    Map<Long, Account> contactsById =
        accountRepository.findAccountsByIds(new ArrayList<>(messagesByContact.keySet())).stream()
            .collect(Collectors.toMap(Account::getId, Function.identity()));

    List<PrivateConversationResponse> conversations =
        messagesByContact.entrySet().stream()
            .map(entry -> toConversation(accountId, entry.getKey(), entry.getValue(), contactsById))
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(PrivateConversationResponse::lastMessageAt).reversed())
            .toList();

    return new PrivateConversationListResponse(conversations);
  }

  @Transactional
  public PrivateMessageListResponse getConversation(Long accountId, Long contactAccountId) {
    ensureContactExists(contactAccountId);
    privateMessageRepository.markConversationRead(accountId, contactAccountId, timeProvider.now());

    List<PrivateMessageResponse> messages =
        privateMessageRepository.findConversation(accountId, contactAccountId).stream()
            .map(PrivateMessagePo::toPrivateMessage)
            .map(PrivateMessageResponse::from)
            .toList();

    return new PrivateMessageListResponse(messages);
  }

  @Transactional
  public void markConversationRead(Long accountId, Long contactAccountId) {
    ensureContactExists(contactAccountId);
    privateMessageRepository.markConversationRead(accountId, contactAccountId, timeProvider.now());
  }

  private PrivateConversationResponse toConversation(
      Long accountId,
      Long contactAccountId,
      List<PrivateMessage> messages,
      Map<Long, Account> contactsById) {
    Account contact = contactsById.get(contactAccountId);
    if (contact == null) {
      return null;
    }

    PrivateMessage lastMessage = messages.getFirst();
    long unreadCount =
        messages.stream()
            .filter(message -> accountId.equals(message.getRecipientAccountId()))
            .filter(message -> !message.isRead())
            .count();

    return new PrivateConversationResponse(
        contact.getId(),
        contact.getUsername().value(),
        contact.getNickname(),
        lastMessage.getContent(),
        lastMessage.getSenderAccountId(),
        lastMessage.getCreatedAt(),
        unreadCount);
  }

  private void ensureContactExists(Long contactAccountId) {
    if (contactAccountId == null) {
      throw new ValidationException(ApiErrorCode.INVALID_REQUEST, "Contact account is required");
    }
    accountRepository
        .findAccountById(contactAccountId)
        .orElseThrow(AccountNotFoundException::notFound);
  }

  private Long contactAccountId(Long accountId, PrivateMessage message) {
    if (accountId.equals(message.getSenderAccountId())) {
      return message.getRecipientAccountId();
    }
    return message.getSenderAccountId();
  }

  private String normalizeContent(String content) {
    if (content == null || content.isBlank()) {
      throw new ValidationException(ApiErrorCode.INVALID_REQUEST, "Message content is required");
    }

    String normalized = content.trim();
    if (normalized.length() > MAX_CONTENT_LENGTH) {
      throw new ValidationException(
          ApiErrorCode.INVALID_REQUEST, "Message content must be at most 1000 characters");
    }

    return normalized;
  }
}
