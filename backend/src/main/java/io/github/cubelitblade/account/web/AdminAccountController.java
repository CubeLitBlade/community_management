package io.github.cubelitblade.account.web;

import io.github.cubelitblade.account.application.AccountService;
import io.github.cubelitblade.account.dto.AdminAccountListResponse;
import io.github.cubelitblade.account.dto.AdminAccountView;
import io.github.cubelitblade.account.dto.ResetPasswordRequest;
import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
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
@RequestMapping("/api/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountController {
  private final AccountService accountService;

  @GetMapping
  public ResponseEntity<AdminAccountListResponse> listAccounts(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser) {
    return ResponseEntity.ok(accountService.getManageableAccounts(authenticatedUser));
  }

  @PostMapping("/{id}/reset-password")
  public ResponseEntity<AdminAccountView> resetPassword(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser,
      @PathVariable Long id,
      @RequestBody ResetPasswordRequest request) {
    return ResponseEntity.ok(accountService.resetPassword(authenticatedUser, id, request));
  }

  @PostMapping("/{id}/suspend")
  public ResponseEntity<AdminAccountView> suspendAccount(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser, @PathVariable Long id) {
    return ResponseEntity.ok(accountService.suspendAccount(authenticatedUser, id));
  }

  @PostMapping("/{id}/promote")
  public ResponseEntity<AdminAccountView> promoteAccount(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser, @PathVariable Long id) {
    return ResponseEntity.ok(accountService.promoteAccount(authenticatedUser, id));
  }

  @PostMapping("/{id}/reactivate")
  public ResponseEntity<AdminAccountView> reactivateAccount(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser, @PathVariable Long id) {
    return ResponseEntity.ok(accountService.reactivateAccount(authenticatedUser, id));
  }

  @PostMapping("/{id}/archive")
  public ResponseEntity<AdminAccountView> archiveAccount(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser, @PathVariable Long id) {
    return ResponseEntity.ok(accountService.archiveAccount(authenticatedUser, id));
  }

  @PostMapping("/{id}/demote")
  public ResponseEntity<AdminAccountView> demoteAccount(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser, @PathVariable Long id) {
    return ResponseEntity.ok(accountService.demoteAccount(authenticatedUser, id));
  }
}
