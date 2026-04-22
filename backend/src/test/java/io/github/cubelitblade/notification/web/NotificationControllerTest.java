package io.github.cubelitblade.notification.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;

import io.github.cubelitblade.account.model.Role;
import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.account.security.JwtTokenProvider;
import io.github.cubelitblade.notification.application.NotificationService;
import io.github.cubelitblade.notification.dto.NotificationListResponse;
import io.github.cubelitblade.notification.dto.NotificationResponse;
import io.github.cubelitblade.notification.dto.NotificationUnreadCountResponse;
import io.github.cubelitblade.notification.model.NotificationScope;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class NotificationControllerTest {
  private static final String CSRF_TOKEN = "test-csrf-token";

  @Autowired private MockMvcTester mvc;

  @MockitoBean private NotificationService notificationService;
  @MockitoBean private JwtTokenProvider jwtTokenProvider;
  @MockitoBean private StringRedisTemplate stringRedisTemplate;

  @Test
  void should_return_unauthorized_without_token() {
    assertThat(mvc.get().uri("/api/notifications").accept(MediaType.APPLICATION_JSON))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void should_return_notifications_with_valid_token() {
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(1L, Role.USER));
    when(notificationService.getNotifications(1L, NotificationScope.ALL))
        .thenReturn(
            new NotificationListResponse(
                List.of(
                    new NotificationResponse(
                        1L,
                        1L,
                        2L,
                        "Alice",
                        "post_comment",
                        "post",
                        3L,
                        "New comment on your post",
                        "Community update",
                        "Alice commented on your post",
                        null,
                        null,
                        false,
                        null,
                        Instant.parse("2026-04-15T08:00:00Z")))));

    assertThat(
            mvc.get()
                .uri("/api/notifications")
                .cookie(new Cookie("TEST_AUTH_TOKEN", "valid-token"))
                .accept(MediaType.APPLICATION_JSON))
        .hasStatus(HttpStatus.OK)
        .apply(document("notifications-list"))
        .bodyJson()
        .satisfies(
            json -> {
              assertThat(json).extractingPath("$.notifications[0].id").isEqualTo(1);
              assertThat(json).extractingPath("$.notifications[0].type").isEqualTo("post_comment");
              assertThat(json).extractingPath("$.notifications[0].isRead").isEqualTo(false);
            });
  }

  @Test
  void should_return_unread_count_with_valid_token() {
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(1L, Role.USER));
    when(notificationService.getUnreadCount(1L, NotificationScope.ALL))
        .thenReturn(new NotificationUnreadCountResponse(2));

    assertThat(
            mvc.get()
                .uri("/api/notifications/unread-count")
                .cookie(new Cookie("TEST_AUTH_TOKEN", "valid-token"))
                .accept(MediaType.APPLICATION_JSON))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .satisfies(json -> assertThat(json).extractingPath("$.count").isEqualTo(2));
  }

  @Test
  void should_mark_notification_read() {
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(1L, Role.USER));

    assertThat(
            mvc.post()
                .uri("/api/notifications/10/read")
                .cookie(new Cookie("TEST_AUTH_TOKEN", "valid-token"))
                .cookie(new Cookie("XSRF-TOKEN", CSRF_TOKEN))
                .header("X-XSRF-TOKEN", CSRF_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
        .hasStatus(HttpStatus.NO_CONTENT);

    verify(notificationService).markRead(1L, 10L);
  }

  @Test
  void should_mark_all_notifications_read() {
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(1L, Role.USER));

    assertThat(
            mvc.post()
                .uri("/api/notifications/read-all")
                .cookie(new Cookie("TEST_AUTH_TOKEN", "valid-token"))
                .cookie(new Cookie("XSRF-TOKEN", CSRF_TOKEN))
                .header("X-XSRF-TOKEN", CSRF_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
        .hasStatus(HttpStatus.NO_CONTENT);

    verify(notificationService).markAllRead(1L);
  }
}
