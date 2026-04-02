package io.github.cubelitblade.user.interfaces;

import io.github.cubelitblade.user.application.service.AccountService;
import io.github.cubelitblade.user.infra.security.jwt.JwtAuthenticatedUser;
import io.github.cubelitblade.user.interfaces.vo.AccountMeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/me")
    public ResponseEntity<AccountMeResponse> me(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof JwtAuthenticatedUser authenticatedUser)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return accountService.findAccount(authenticatedUser.accountId())
                .map(AccountMeResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}

