package io.github.cubelitblade.account.web;

import io.github.cubelitblade.account.application.AccountService;
import io.github.cubelitblade.account.dto.AccountLoginRequest;
import io.github.cubelitblade.account.dto.AccountRegisterRequest;
import io.github.cubelitblade.account.dto.RegisterFieldsCheckRequest;
import io.github.cubelitblade.account.dto.RegisterFieldsCheckResponse;
import io.github.cubelitblade.account.dto.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AccountService accountService;

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

    return ResponseEntity.ok(accountService.login(request, inetAddress));
  }

  @PostMapping("/register/check")
  public ResponseEntity<RegisterFieldsCheckResponse> registerValidation(
      @RequestBody RegisterFieldsCheckRequest request) {
    return ResponseEntity.ok(accountService.checkRegisterFields(request));
  }
}
