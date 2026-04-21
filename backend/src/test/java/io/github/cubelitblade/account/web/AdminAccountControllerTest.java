package io.github.cubelitblade.account.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.cubelitblade.account.application.AccountService;
import io.github.cubelitblade.account.dto.AdminAccountListResponse;
import io.github.cubelitblade.account.dto.AdminAccountView;
import io.github.cubelitblade.account.dto.ResetPasswordRequest;
import io.github.cubelitblade.account.model.Role;
import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.account.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import java.util.List;
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
class AdminAccountControllerTest {
  private static final String CSRF_TOKEN = "test-csrf-token";

  @Autowired private MockMvcTester mvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private AccountService accountService;
  @MockitoBean private JwtTokenProvider jwtTokenProvider;

  private String serialize(Object obj) {
    return objectMapper.writeValueAsString(obj);
  }

  @Test
  void should_list_accounts_for_admin() {
    JwtAuthenticatedUser authenticatedUser = new JwtAuthenticatedUser(2L, Role.ADMIN);
    when(jwtTokenProvider.parseToken("valid-token")).thenReturn(authenticatedUser);
    when(accountService.getManageableAccounts(authenticatedUser))
        .thenReturn(
            new AdminAccountListResponse(
                List.of(
                    new AdminAccountView(1L, "owner", "owner", "owner", "normal", false),
                    new AdminAccountView(3L, "user", "user", "user", "normal", true))));

    assertThat(
            mvc.get()
                .uri("/api/admin/accounts")
                .cookie(new Cookie("TEST_AUTH_TOKEN", "valid-token"))
                .accept(MediaType.APPLICATION_JSON))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .satisfies(
            json ->
                assertThat(json).extractingPath("$.accounts[1].mustChangePassword").isEqualTo(true));
  }

  @Test
  void should_reset_password() {
    ResetPasswordRequest request = new ResetPasswordRequest("Newpass123");
    JwtAuthenticatedUser authenticatedUser = new JwtAuthenticatedUser(2L, Role.ADMIN);
    when(jwtTokenProvider.parseToken("valid-token")).thenReturn(authenticatedUser);
    when(accountService.resetPassword(authenticatedUser, 3L, request))
        .thenReturn(new AdminAccountView(3L, "user", "user", "user", "normal", true));

    assertThat(
            mvc.post()
                .uri("/api/admin/accounts/3/reset-password")
                .cookie(new Cookie("TEST_AUTH_TOKEN", "valid-token"))
                .cookie(new Cookie("XSRF-TOKEN", CSRF_TOKEN))
                .header("X-XSRF-TOKEN", CSRF_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(serialize(request)))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .satisfies(
            json -> assertThat(json).extractingPath("$.mustChangePassword").isEqualTo(true));
  }

  @Test
  void should_suspend_account() {
    JwtAuthenticatedUser authenticatedUser = new JwtAuthenticatedUser(1L, Role.OWNER);
    when(jwtTokenProvider.parseToken("valid-token")).thenReturn(authenticatedUser);
    when(accountService.suspendAccount(authenticatedUser, 2L))
        .thenReturn(new AdminAccountView(2L, "admin", "admin", "admin", "suspended", false));

    assertThat(
            mvc.post()
                .uri("/api/admin/accounts/2/suspend")
                .cookie(new Cookie("TEST_AUTH_TOKEN", "valid-token"))
                .cookie(new Cookie("XSRF-TOKEN", CSRF_TOKEN))
                .header("X-XSRF-TOKEN", CSRF_TOKEN))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .satisfies(json -> assertThat(json).extractingPath("$.status").isEqualTo("suspended"));
  }

  @Test
  void should_promote_account() {
    JwtAuthenticatedUser authenticatedUser = new JwtAuthenticatedUser(1L, Role.OWNER);
    when(jwtTokenProvider.parseToken("valid-token")).thenReturn(authenticatedUser);
    when(accountService.promoteAccount(authenticatedUser, 3L))
        .thenReturn(new AdminAccountView(3L, "user", "user", "admin", "normal", false));

    assertThat(
            mvc.post()
                .uri("/api/admin/accounts/3/promote")
                .cookie(new Cookie("TEST_AUTH_TOKEN", "valid-token"))
                .cookie(new Cookie("XSRF-TOKEN", CSRF_TOKEN))
                .header("X-XSRF-TOKEN", CSRF_TOKEN))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .satisfies(json -> assertThat(json).extractingPath("$.role").isEqualTo("admin"));

    verify(accountService).promoteAccount(authenticatedUser, 3L);
  }

  @Test
  void should_reactivate_account() {
    JwtAuthenticatedUser authenticatedUser = new JwtAuthenticatedUser(1L, Role.OWNER);
    when(jwtTokenProvider.parseToken("valid-token")).thenReturn(authenticatedUser);
    when(accountService.reactivateAccount(authenticatedUser, 2L))
        .thenReturn(new AdminAccountView(2L, "admin", "admin", "admin", "normal", false));

    assertThat(
            mvc.post()
                .uri("/api/admin/accounts/2/reactivate")
                .cookie(new Cookie("TEST_AUTH_TOKEN", "valid-token"))
                .cookie(new Cookie("XSRF-TOKEN", CSRF_TOKEN))
                .header("X-XSRF-TOKEN", CSRF_TOKEN))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .satisfies(json -> assertThat(json).extractingPath("$.status").isEqualTo("normal"));

    verify(accountService).reactivateAccount(authenticatedUser, 2L);
  }

  @Test
  void should_archive_account() {
    JwtAuthenticatedUser authenticatedUser = new JwtAuthenticatedUser(1L, Role.OWNER);
    when(jwtTokenProvider.parseToken("valid-token")).thenReturn(authenticatedUser);
    when(accountService.archiveAccount(authenticatedUser, 3L))
        .thenReturn(
            new AdminAccountView(3L, "user#archived_3", "user", "user", "archived", false));

    assertThat(
            mvc.post()
                .uri("/api/admin/accounts/3/archive")
                .cookie(new Cookie("TEST_AUTH_TOKEN", "valid-token"))
                .cookie(new Cookie("XSRF-TOKEN", CSRF_TOKEN))
                .header("X-XSRF-TOKEN", CSRF_TOKEN))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .satisfies(json -> assertThat(json).extractingPath("$.status").isEqualTo("archived"));

    verify(accountService).archiveAccount(authenticatedUser, 3L);
  }

  @Test
  void should_demote_account() {
    JwtAuthenticatedUser authenticatedUser = new JwtAuthenticatedUser(1L, Role.OWNER);
    when(jwtTokenProvider.parseToken("valid-token")).thenReturn(authenticatedUser);
    when(accountService.demoteAccount(authenticatedUser, 2L))
        .thenReturn(new AdminAccountView(2L, "admin", "admin", "user", "normal", false));

    assertThat(
            mvc.post()
                .uri("/api/admin/accounts/2/demote")
                .cookie(new Cookie("TEST_AUTH_TOKEN", "valid-token"))
                .cookie(new Cookie("XSRF-TOKEN", CSRF_TOKEN))
                .header("X-XSRF-TOKEN", CSRF_TOKEN))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .satisfies(json -> assertThat(json).extractingPath("$.role").isEqualTo("user"));

    verify(accountService).demoteAccount(authenticatedUser, 2L);
  }
}
