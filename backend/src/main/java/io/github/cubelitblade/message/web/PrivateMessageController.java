package io.github.cubelitblade.message.web;

import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.message.application.PrivateMessageService;
import io.github.cubelitblade.message.dto.PrivateConversationListResponse;
import io.github.cubelitblade.message.dto.PrivateMessageListResponse;
import io.github.cubelitblade.message.dto.PrivateMessageResponse;
import io.github.cubelitblade.message.dto.SendPrivateMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class PrivateMessageController {
  private final PrivateMessageService privateMessageService;

  @GetMapping("/conversations")
  public ResponseEntity<PrivateConversationListResponse> getConversations(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser) {
    return ResponseEntity.ok(privateMessageService.getConversations(authenticatedUser.accountId()));
  }

  @GetMapping("/conversations/{accountId}")
  public ResponseEntity<PrivateMessageListResponse> getConversation(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser,
      @PathVariable Long accountId) {
    return ResponseEntity.ok(
        privateMessageService.getConversation(authenticatedUser.accountId(), accountId));
  }

  @PostMapping
  public ResponseEntity<PrivateMessageResponse> sendMessage(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser,
      @RequestBody SendPrivateMessageRequest request) {
    return ResponseEntity.ok(
        privateMessageService.sendMessage(authenticatedUser.accountId(), request));
  }

  @PostMapping("/conversations/{accountId}/read")
  public ResponseEntity<Void> markConversationRead(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser,
      @PathVariable Long accountId) {
    privateMessageService.markConversationRead(authenticatedUser.accountId(), accountId);
    return ResponseEntity.noContent().build();
  }
}
