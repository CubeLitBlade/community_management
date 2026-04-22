package io.github.cubelitblade.account.web;

import io.github.cubelitblade.account.application.AccountService;
import io.github.cubelitblade.account.dto.AccountMeResponse;
import io.github.cubelitblade.account.dto.ChangePasswordRequest;
import io.github.cubelitblade.account.dto.ContactAccountListResponse;
import io.github.cubelitblade.account.exception.AccountNotFoundException;
import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

  private final AccountService accountService;

  @GetMapping("/me")
  public ResponseEntity<AccountMeResponse> me(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser) {
    return accountService
        .findAccount(authenticatedUser.accountId())
        .map(AccountMeResponse::from)
        .map(ResponseEntity::ok)
        .orElseThrow(AccountNotFoundException::notFound);
  }

  @GetMapping("/contacts")
  public ResponseEntity<ContactAccountListResponse> contacts(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser) {
    return ResponseEntity.ok(accountService.getContacts(authenticatedUser.accountId()));
  }

  @PostMapping("/change-password")
  public ResponseEntity<Void> changePassword(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser,
      @RequestBody ChangePasswordRequest request) {
    accountService.changePassword(authenticatedUser.accountId(), request);
    return ResponseEntity.noContent().build();
  }
}
