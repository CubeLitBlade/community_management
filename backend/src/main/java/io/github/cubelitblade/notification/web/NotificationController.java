package io.github.cubelitblade.notification.web;

import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.notification.application.NotificationService;
import io.github.cubelitblade.notification.dto.NotificationListResponse;
import io.github.cubelitblade.notification.dto.NotificationUnreadCountResponse;
import io.github.cubelitblade.notification.model.NotificationScope;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<NotificationListResponse> getNotifications(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser,
      @RequestParam(value = "scope", required = false) String scope) {
    return ResponseEntity.ok(
        notificationService.getNotifications(
            authenticatedUser.accountId(), NotificationScope.from(scope)));
  }

  @GetMapping("/unread-count")
  public ResponseEntity<NotificationUnreadCountResponse> getUnreadCount(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser,
      @RequestParam(value = "scope", required = false) String scope) {
    return ResponseEntity.ok(
        notificationService.getUnreadCount(
            authenticatedUser.accountId(), NotificationScope.from(scope)));
  }

  @PostMapping("/{id}/read")
  public ResponseEntity<Void> markRead(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser, @PathVariable Long id) {
    notificationService.markRead(authenticatedUser.accountId(), id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/read-all")
  public ResponseEntity<Void> markAllRead(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser) {
    notificationService.markAllRead(authenticatedUser.accountId());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/stream")
  public SseEmitter subscribe(@AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser) {
    return notificationService.subscribe(authenticatedUser.accountId());
  }
}
