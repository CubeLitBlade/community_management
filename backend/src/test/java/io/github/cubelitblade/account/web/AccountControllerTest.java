package io.github.cubelitblade.account.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;

import io.github.cubelitblade.account.application.AccountService;
import io.github.cubelitblade.account.dto.ChangePasswordRequest;
import io.github.cubelitblade.account.model.Account;
import io.github.cubelitblade.account.model.Role;
import io.github.cubelitblade.account.model.Status;
import io.github.cubelitblade.account.model.Username;
import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.account.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.ObjectMapper;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class AccountControllerTest {
  private static final String CSRF_TOKEN = "test-csrf-token";

  @Autowired private MockMvcTester mvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private AccountService accountService;

  @MockitoBean private JwtTokenProvider jwtTokenProvider;

  private String serialize(Object obj) {
    return objectMapper.writeValueAsString(obj);
  }

  @Test
  void should_return_unauthorized_without_token() {
    assertThat(mvc.get().uri("/api/account/me").accept(MediaType.APPLICATION_JSON))
        .hasStatus(HttpStatus.UNAUTHORIZED)
        .apply(document("account-me-unauthorized"));
  }

  @Test
  void should_return_me_with_valid_token() {
    Account account =
        Account.reconstitute(
            Account.Snapshot.builder()
                .id(1L)
                .username(Username.of("Alice"))
                .nickname("Alice")
                .role(Role.USER)
                .status(Status.NORMAL)
                .mustChangePassword(true)
                .build());

    when(accountService.findAccount(1L)).thenReturn(Optional.of(account));
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(1L, Role.USER));

    assertThat(
            mvc.get()
                .uri("/api/account/me")
                .cookie(new Cookie("TEST_AUTH_TOKEN", "valid-token"))
                .accept(MediaType.APPLICATION_JSON))
        .hasStatus(HttpStatus.OK)
        .apply(document("account-me"))
        .bodyJson()
        .satisfies(
            json -> {
              assertThat(json).extractingPath("$.id").isEqualTo(1);
              assertThat(json).extractingPath("$.username").isEqualTo("Alice");
              assertThat(json).extractingPath("$.nickname").isEqualTo("Alice");
              assertThat(json).extractingPath("$.role").isEqualTo("user");
              assertThat(json).extractingPath("$.status").isEqualTo("normal");
              assertThat(json).extractingPath("$.mustChangePassword").isEqualTo(true);
            });
  }

  @Test
  void should_change_password_with_valid_token() {
    ChangePasswordRequest request = new ChangePasswordRequest("password123", "password456");

    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(1L, Role.USER));

    assertThat(
            mvc.post()
                .uri("/api/account/change-password")
                .cookie(new Cookie("TEST_AUTH_TOKEN", "valid-token"))
                .cookie(new Cookie("XSRF-TOKEN", CSRF_TOKEN))
                .header("X-XSRF-TOKEN", CSRF_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(serialize(request))
                .accept(MediaType.APPLICATION_JSON))
        .hasStatus(HttpStatus.NO_CONTENT)
        .apply(document("account-change-password"));

    verify(accountService).changePassword(1L, request);
  }

  @Test
  void should_reject_change_password_without_csrf() {
    ChangePasswordRequest request = new ChangePasswordRequest("password123", "password456");

    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(1L, Role.USER));

    assertThat(
            mvc.post()
                .uri("/api/account/change-password")
                .cookie(new Cookie("TEST_AUTH_TOKEN", "valid-token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(serialize(request))
                .accept(MediaType.APPLICATION_JSON))
        .hasStatus(HttpStatus.FORBIDDEN);
  }
}
