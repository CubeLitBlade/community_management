package io.github.cubelitblade.post.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.cubelitblade.account.model.Role;
import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.account.security.JwtTokenProvider;
import io.github.cubelitblade.post.application.PostService;
import io.github.cubelitblade.post.dto.EditPostRequest;
import io.github.cubelitblade.post.dto.PostDetailView;
import io.github.cubelitblade.post.dto.PostReactionView;
import io.github.cubelitblade.post.dto.PublishPostRequest;
import io.github.cubelitblade.post.dto.RecentPostsResponse;
import io.github.cubelitblade.post.model.Post;
import io.github.cubelitblade.post.model.Status;
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
class PostControllerTest {
  private static final String CSRF_TOKEN = "test-csrf-token";
  private static final Instant NOW = Instant.parse("2026-04-20T08:00:00Z");

  @Autowired private MockMvcTester mvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private PostService postService;
  @MockitoBean private JwtTokenProvider jwtTokenProvider;
  @MockitoBean private StringRedisTemplate stringRedisTemplate;

  @Test
  void should_publish_post_with_valid_token_and_csrf() {
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(7L, Role.USER));
    when(postService.publishPost(7L, new PublishPostRequest("Title", "Content"))).thenReturn(99L);

    assertThat(
            mvc.post()
                .uri("/api/posts")
                .cookie(authCookie())
                .cookie(csrfCookie())
                .header("X-XSRF-TOKEN", CSRF_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(serialize(new PublishPostRequest("Title", "Content"))))
        .hasStatus(HttpStatus.CREATED)
        .hasHeader("Location", "http://localhost:8080/api/posts/99");
  }

  @Test
  void should_return_recent_posts_without_token() {
    when(postService.getRecentPosts(null, 2, 50L))
        .thenReturn(
            new RecentPostsResponse(
                List.of(
                    new PostDetailView(
                        49L,
                        7L,
                        "alice",
                        "Alice",
                        "Title",
                        "Content",
                        List.of(new PostReactionView("like", 2L)),
                        null,
                        NOW,
                        null)),
                false));

    assertThat(
            mvc.get().uri("/api/posts/recent?count=2&lastId=50").accept(MediaType.APPLICATION_JSON))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .satisfies(
            json -> {
              assertThat(json).extractingPath("$.items[0].id").isEqualTo(49);
              assertThat(json)
                  .extractingPath("$.items[0].reactions[0].reactionType")
                  .isEqualTo("like");
            });
  }

  @Test
  void should_reject_invalid_recent_post_count() {
    assertThat(mvc.get().uri("/api/posts/recent?count=0").accept(MediaType.APPLICATION_JSON))
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  void should_return_post_detail_without_token() {
    when(postService.getPostDetail(null, 99L))
        .thenReturn(
            new PostDetailView(
                99L, 7L, "alice", "Alice", "Title", "Content", List.of(), null, NOW, null));

    assertThat(mvc.get().uri("/api/posts/99").accept(MediaType.APPLICATION_JSON))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .satisfies(json -> assertThat(json).extractingPath("$.title").isEqualTo("Title"));
  }

  @Test
  void should_archive_post_with_valid_token_and_csrf() {
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(7L, Role.USER));

    assertThat(
            mvc.delete()
                .uri("/api/posts/99")
                .cookie(authCookie())
                .cookie(csrfCookie())
                .header("X-XSRF-TOKEN", CSRF_TOKEN))
        .hasStatus(HttpStatus.NO_CONTENT);

    verify(postService).archivePost(new JwtAuthenticatedUser(7L, Role.USER), 99L);
  }

  @Test
  void should_reject_archive_without_csrf() {
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(7L, Role.USER));

    assertThat(mvc.delete().uri("/api/posts/99").cookie(authCookie()))
        .hasStatus(HttpStatus.FORBIDDEN);
  }

  @Test
  void should_edit_post_with_valid_token_and_csrf() {
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(7L, Role.USER));
    Post edited =
        Post.reconstitute(
            Post.Snapshot.builder()
                .id(99L)
                .authorId(7L)
                .title("New")
                .content("Updated")
                .status(Status.NORMAL)
                .createdAt(NOW.minusSeconds(60))
                .updatedAt(NOW)
                .build());
    when(postService.editPost(
            new JwtAuthenticatedUser(7L, Role.USER), 99L, new EditPostRequest("New", "Updated")))
        .thenReturn(edited);

    assertThat(
            mvc.patch()
                .uri("/api/posts/99")
                .cookie(authCookie())
                .cookie(csrfCookie())
                .header("X-XSRF-TOKEN", CSRF_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(serialize(new EditPostRequest("New", "Updated"))))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .satisfies(json -> assertThat(json).extractingPath("$.title").isEqualTo("New"));
  }

  @Test
  void should_reject_publish_without_token() {
    assertThat(
            mvc.post()
                .uri("/api/posts")
                .cookie(csrfCookie())
                .header("X-XSRF-TOKEN", CSRF_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(serialize(new PublishPostRequest("Title", "Content"))))
        .hasStatus(HttpStatus.UNAUTHORIZED);
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
