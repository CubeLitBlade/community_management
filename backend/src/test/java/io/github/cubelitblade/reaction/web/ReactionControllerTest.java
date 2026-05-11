package io.github.cubelitblade.reaction.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.github.cubelitblade.account.model.Role;
import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.account.security.JwtTokenProvider;
import io.github.cubelitblade.reaction.application.ReactionService;
import io.github.cubelitblade.reaction.application.SetReactionResult;
import io.github.cubelitblade.reaction.dto.AddReactionRequest;
import jakarta.servlet.http.Cookie;
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
class ReactionControllerTest {
  private static final String CSRF_TOKEN = "test-csrf-token";

  @Autowired private MockMvcTester mvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private ReactionService reactionService;
  @MockitoBean private JwtTokenProvider jwtTokenProvider;
  @MockitoBean private StringRedisTemplate stringRedisTemplate;

  @Test
  void should_return_created_for_new_reaction() {
    AddReactionRequest request = new AddReactionRequest("post", 99L, "like");
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(7L, Role.USER));
    when(reactionService.setReaction(new JwtAuthenticatedUser(7L, Role.USER), request))
        .thenReturn(SetReactionResult.created(55L));

    assertThat(
            mvc.post()
                .uri("/api/reactions")
                .cookie(authCookie())
                .cookie(csrfCookie())
                .header("X-XSRF-TOKEN", CSRF_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(serialize(request)))
        .hasStatus(HttpStatus.CREATED)
        .hasHeader("Location", "http://localhost:8080/api/reactions/55");
  }

  @Test
  void should_return_no_content_for_updated_reaction() {
    AddReactionRequest request = new AddReactionRequest("post", 99L, "love");
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(7L, Role.USER));
    when(reactionService.setReaction(new JwtAuthenticatedUser(7L, Role.USER), request))
        .thenReturn(SetReactionResult.updated(55L));

    assertThat(authorizedPost(request)).hasStatus(HttpStatus.NO_CONTENT);
  }

  @Test
  void should_return_no_content_for_removed_reaction() {
    AddReactionRequest request = new AddReactionRequest("post", 99L, null);
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(7L, Role.USER));
    when(reactionService.setReaction(new JwtAuthenticatedUser(7L, Role.USER), request))
        .thenReturn(SetReactionResult.removed(55L));

    assertThat(authorizedPost(request)).hasStatus(HttpStatus.NO_CONTENT);
  }

  @Test
  void should_return_no_content_for_unchanged_reaction() {
    AddReactionRequest request = new AddReactionRequest("post", 99L, null);
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(7L, Role.USER));
    when(reactionService.setReaction(new JwtAuthenticatedUser(7L, Role.USER), request))
        .thenReturn(SetReactionResult.unchanged());

    assertThat(authorizedPost(request)).hasStatus(HttpStatus.NO_CONTENT);
  }

  @Test
  void should_reject_reaction_without_token() {
    assertThat(
            mvc.post()
                .uri("/api/reactions")
                .cookie(csrfCookie())
                .header("X-XSRF-TOKEN", CSRF_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(serialize(new AddReactionRequest("post", 99L, "like"))))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void should_reject_reaction_without_csrf() {
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(7L, Role.USER));

    assertThat(
            mvc.post()
                .uri("/api/reactions")
                .cookie(authCookie())
                .contentType(MediaType.APPLICATION_JSON)
                .content(serialize(new AddReactionRequest("post", 99L, "like"))))
        .hasStatus(HttpStatus.FORBIDDEN);
  }

  private MockMvcTester.MockMvcRequestBuilder authorizedPost(AddReactionRequest request) {
    return mvc.post()
        .uri("/api/reactions")
        .cookie(authCookie())
        .cookie(csrfCookie())
        .header("X-XSRF-TOKEN", CSRF_TOKEN)
        .contentType(MediaType.APPLICATION_JSON)
        .content(serialize(request));
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
