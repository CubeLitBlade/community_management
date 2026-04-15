package io.github.cubelitblade.account.web;

import io.github.cubelitblade.account.application.AccountService;
import io.github.cubelitblade.account.dto.AccountLoginRequest;
import io.github.cubelitblade.account.dto.AccountRegisterRequest;
import io.github.cubelitblade.account.dto.RegisterFieldsCheckRequest;
import io.github.cubelitblade.account.dto.RegisterFieldsCheckResponse;
import io.github.cubelitblade.account.dto.TokenResponse;
import io.github.cubelitblade.account.security.AuthCookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AccountService accountService;
  private final AuthCookieService authCookieService;

  @PostMapping("/register")
  public ResponseEntity<Void> register(@RequestBody AccountRegisterRequest request) {
    Long id = accountService.register(request).getId();
    URI url =
        ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/api/account/{id}")
            .buildAndExpand(id)
            .toUri();
    return ResponseEntity.created(url).build();
  }

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(
      @Valid @RequestBody AccountLoginRequest request,
      @RequestHeader(value = "X-Forwarded-For", required = false) String xForwardedFor,
      HttpServletRequest httpServletRequest) {
    InetAddress inetAddress = null;

    try {
      String ip;

      if (xForwardedFor == null || xForwardedFor.isBlank()) {
        ip = httpServletRequest.getRemoteAddr();
      } else {
        ip = xForwardedFor.split(",")[0].trim();
      }

      inetAddress = InetAddress.getByName(ip);
    } catch (UnknownHostException e) {
      log.warn("Unknown host: ", e);
    }

    AccountService.LoginResult loginResult = accountService.login(request, inetAddress);
    HttpHeaders headers = new HttpHeaders();
    authCookieService.writeAuthCookie(headers, loginResult.token());
    return ResponseEntity.ok().headers(headers).body(loginResult.response());
  }

  @PostMapping("/register/check")
  public ResponseEntity<RegisterFieldsCheckResponse> registerValidation(
      @RequestBody RegisterFieldsCheckRequest request) {
    return ResponseEntity.ok(accountService.checkRegisterFields(request));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request) {
    String token = authCookieService.resolveToken(request);
    if (token != null) {
      accountService.logout(token);
    }

    HttpHeaders headers = new HttpHeaders();
    authCookieService.clearAuthCookie(headers);
    return ResponseEntity.noContent().headers(headers).build();
  }

  @GetMapping("/csrf")
  public ResponseEntity<Void> csrf(CsrfToken csrfToken) {
    csrfToken.getToken();
    return ResponseEntity.noContent().build();
  }
}
