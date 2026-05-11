package io.github.cubelitblade.comment.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.github.cubelitblade.account.model.Role;
import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.account.security.JwtTokenProvider;
import io.github.cubelitblade.comment.application.CommentService;
import io.github.cubelitblade.comment.dto.CommentDetailView;
import io.github.cubelitblade.comment.dto.CommentListResponse;
import io.github.cubelitblade.comment.dto.CreateCommentRequest;
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
class CommentControllerTest {
  private static final String CSRF_TOKEN = "test-csrf-token";
  private static final Instant NOW = Instant.parse("2026-04-20T08:00:00Z");

  @Autowired private MockMvcTester mvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private CommentService commentService;
  @MockitoBean private JwtTokenProvider jwtTokenProvider;
  @MockitoBean private StringRedisTemplate stringRedisTemplate;

  @Test
  void should_return_comments_without_token() {
    when(commentService.getComments("post", 99L))
        .thenReturn(
            new CommentListResponse(
                List.of(
                    new CommentDetailView(
                        1L, "post", 99L, 7L, "alice", "Alice", null, null, null, null, "Nice", NOW,
                        null))));

    assertThat(mvc.get().uri("/api/comments?targetType=post&targetId=99"))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .satisfies(
            json -> {
              assertThat(json).extractingPath("$.items[0].id").isEqualTo(1);
              assertThat(json).extractingPath("$.items[0].content").isEqualTo("Nice");
            });
  }

  @Test
  void should_create_comment_with_valid_token_and_csrf() {
    CreateCommentRequest request = new CreateCommentRequest("post", 99L, null, "Nice");
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(7L, Role.USER));
    when(commentService.createComment(new JwtAuthenticatedUser(7L, Role.USER), request))
        .thenReturn(55L);

    assertThat(
            mvc.post()
                .uri("/api/comments")
                .cookie(authCookie())
                .cookie(csrfCookie())
                .header("X-XSRF-TOKEN", CSRF_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(serialize(request)))
        .hasStatus(HttpStatus.CREATED)
        .hasHeader("Location", "http://localhost:8080/api/comments/55");
  }

  @Test
  void should_reject_create_comment_without_token() {
    assertThat(
            mvc.post()
                .uri("/api/comments")
                .cookie(csrfCookie())
                .header("X-XSRF-TOKEN", CSRF_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(serialize(new CreateCommentRequest("post", 99L, null, "Nice"))))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void should_reject_create_comment_without_csrf() {
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(7L, Role.USER));

    assertThat(
            mvc.post()
                .uri("/api/comments")
                .cookie(authCookie())
                .contentType(MediaType.APPLICATION_JSON)
                .content(serialize(new CreateCommentRequest("post", 99L, null, "Nice"))))
        .hasStatus(HttpStatus.FORBIDDEN);
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
