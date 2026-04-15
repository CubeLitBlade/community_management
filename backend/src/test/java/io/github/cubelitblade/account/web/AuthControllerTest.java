package io.github.cubelitblade.account.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;

import io.github.cubelitblade.account.application.AccountService;
import io.github.cubelitblade.account.dto.AccountLoginRequest;
import io.github.cubelitblade.account.dto.AccountRegisterRequest;
import io.github.cubelitblade.account.dto.TokenResponse;
import io.github.cubelitblade.account.model.Account;
import io.github.cubelitblade.account.security.JwtTokenProvider;
import java.net.InetAddress;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@ActiveProfiles("test")
@WebMvcTest(controllers = AuthController.class)
@AutoConfigureRestDocs
class AuthControllerTest {

  @Autowired private MockMvcTester mvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private AccountService accountService;

  @MockitoBean private JwtTokenProvider jwtTokenProvider;

  @MockitoBean private StringRedisTemplate stringRedisTemplate;

  private String serialize(Object obj) {
    return objectMapper.writeValueAsString(obj);
  }

  @Nested
  class register {

    @Test
    void success() {

      AccountRegisterRequest request =
          new AccountRegisterRequest("user", "password123", "user@example.com", null);

      Account account = mock(Account.class);
      when(account.getId()).thenReturn(1L);

      when(accountService.register(any())).thenReturn(account);

      assertThat(
              mvc.post()
                  .uri("/api/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(serialize(request))
                  .accept(MediaType.APPLICATION_JSON))
          .hasStatus(HttpStatus.CREATED)
          .apply(document("auth-register"));
    }

    @Test
    void duplicateUsername() {
      AccountRegisterRequest request =
          new AccountRegisterRequest("duplicate", "password123", "dup@example.com", null);

      doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "username already exists"))
          .when(accountService)
          .register(request);

      assertThat(
              mvc.post()
                  .uri("/api/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(serialize(request))
                  .accept(MediaType.APPLICATION_JSON))
          .hasStatus(HttpStatus.CONFLICT)
          .apply(document("auth-register-duplicate-username"));
    }
  }

  @Test
  void should_return_token_and_password_change_flag_on_login() {
    AccountLoginRequest request = new AccountLoginRequest("owner", "password123");

    when(accountService.login(eq(request), any(InetAddress.class)))
        .thenReturn(new TokenResponse("jwt-token", true));

    assertThat(
            mvc.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(serialize(request))
                .accept(MediaType.APPLICATION_JSON))
        .hasStatus(HttpStatus.OK)
        .apply(document("auth-login"))
        .bodyJson()
        .satisfies(
            json -> {
              assertThat(json).extractingPath("$.accessToken").isEqualTo("jwt-token");
              assertThat(json).extractingPath("$.mustChangePassword").isEqualTo(true);
            });
  }
}
