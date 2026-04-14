package io.github.cubelitblade.account.web;

import io.github.cubelitblade.account.application.AccountService;
import io.github.cubelitblade.account.dto.AccountMeResponse;
import io.github.cubelitblade.account.exception.AccountNotFoundException;
import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.common.exception.ApiErrorCode;
import io.github.cubelitblade.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

  private final AccountService accountService;

  @GetMapping("/me")
  public ResponseEntity<AccountMeResponse> me(Authentication authentication) {
    if (!(authentication.getPrincipal() instanceof JwtAuthenticatedUser authenticatedUser)) {
      throw new UnauthorizedException(ApiErrorCode.UNAUTHORIZED, "Authentication is required");
    }

    return accountService
        .findAccount(authenticatedUser.accountId())
        .map(AccountMeResponse::from)
        .map(ResponseEntity::ok)
        .orElseThrow(AccountNotFoundException::notFound);
  }
}
