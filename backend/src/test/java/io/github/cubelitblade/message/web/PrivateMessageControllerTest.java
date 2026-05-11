package io.github.cubelitblade.message.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.cubelitblade.account.model.Role;
import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.account.security.JwtTokenProvider;
import io.github.cubelitblade.message.application.PrivateMessageService;
import io.github.cubelitblade.message.dto.PrivateConversationListResponse;
import io.github.cubelitblade.message.dto.PrivateConversationResponse;
import io.github.cubelitblade.message.dto.PrivateMessageListResponse;
import io.github.cubelitblade.message.dto.PrivateMessageResponse;
import io.github.cubelitblade.message.dto.SendPrivateMessageRequest;
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
import tools.jackson.databind.ObjectMapper;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class PrivateMessageControllerTest {
  private static final String CSRF_TOKEN = "test-csrf-token";
  private static final Instant NOW = Instant.parse("2026-04-20T08:00:00Z");

  @Autowired private MockMvcTester mvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private PrivateMessageService privateMessageService;
  @MockitoBean private JwtTokenProvider jwtTokenProvider;
  @MockitoBean private StringRedisTemplate stringRedisTemplate;

  @Test
  void should_return_conversations_with_valid_token() {
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(7L, Role.USER));
    when(privateMessageService.getConversations(7L))
        .thenReturn(
            new PrivateConversationListResponse(
                List.of(new PrivateConversationResponse(8L, "bob", "Bob", "Hi", 8L, NOW, 2L))));

    assertThat(mvc.get().uri("/api/messages/conversations").cookie(authCookie()))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .satisfies(
            json ->
                assertThat(json)
                    .extractingPath("$.conversations[0].contactAccountId")
                    .isEqualTo(8));
  }

  @Test
  void should_return_conversation_with_valid_token() {
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(7L, Role.USER));
    when(privateMessageService.getConversation(7L, 8L))
        .thenReturn(
            new PrivateMessageListResponse(
                List.of(new PrivateMessageResponse(1L, 8L, 7L, "Hi", false, null, NOW))));

    assertThat(mvc.get().uri("/api/messages/conversations/8").cookie(authCookie()))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .satisfies(
            json -> assertThat(json).extractingPath("$.messages[0].content").isEqualTo("Hi"));
  }

  @Test
  void should_send_message_with_valid_token_and_csrf() {
    SendPrivateMessageRequest request = new SendPrivateMessageRequest(8L, "Hello");
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(7L, Role.USER));
    when(privateMessageService.sendMessage(7L, request))
        .thenReturn(new PrivateMessageResponse(1L, 7L, 8L, "Hello", false, null, NOW));

    assertThat(
            mvc.post()
                .uri("/api/messages")
                .cookie(authCookie())
                .cookie(csrfCookie())
                .header("X-XSRF-TOKEN", CSRF_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(serialize(request)))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .satisfies(json -> assertThat(json).extractingPath("$.content").isEqualTo("Hello"));
  }

  @Test
  void should_mark_conversation_read_with_valid_token_and_csrf() {
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(7L, Role.USER));

    assertThat(
            mvc.post()
                .uri("/api/messages/conversations/8/read")
                .cookie(authCookie())
                .cookie(csrfCookie())
                .header("X-XSRF-TOKEN", CSRF_TOKEN))
        .hasStatus(HttpStatus.NO_CONTENT);

    verify(privateMessageService).markConversationRead(7L, 8L);
  }

  @Test
  void should_reject_send_message_without_csrf() {
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(7L, Role.USER));

    assertThat(
            mvc.post()
                .uri("/api/messages")
                .cookie(authCookie())
                .contentType(MediaType.APPLICATION_JSON)
                .content(serialize(new SendPrivateMessageRequest(8L, "Hello"))))
        .hasStatus(HttpStatus.FORBIDDEN);
  }

  @Test
  void should_reject_conversations_without_token() {
    assertThat(mvc.get().uri("/api/messages/conversations")).hasStatus(HttpStatus.UNAUTHORIZED);
  }

  private Cookie authCookie() {
    return new Cookie("TEST_AUTH_TOKEN", "valid-token");
  }

  private Cookie csrfCookie() {
    return new Cookie("XSRF-TOKEN", CSRF_TOKEN);
  }

  private String serialize(Object value) {
    return objectMapper.writeValueAsString(value);
  }
}
