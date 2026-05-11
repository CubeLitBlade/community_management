package io.github.cubelitblade.activity.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.cubelitblade.account.model.Role;
import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.account.security.JwtTokenProvider;
import io.github.cubelitblade.activity.application.ActivityService;
import io.github.cubelitblade.activity.dto.ActivityListResponse;
import io.github.cubelitblade.activity.dto.ActivityParticipantListResponse;
import io.github.cubelitblade.activity.dto.ActivityParticipantView;
import io.github.cubelitblade.activity.dto.ActivityView;
import io.github.cubelitblade.activity.dto.CreateActivityRequest;
import io.github.cubelitblade.activity.dto.MyActivitiesResponse;
import io.github.cubelitblade.activity.dto.RecentActivitiesResponse;
import io.github.cubelitblade.activity.dto.RejectActivityRequest;
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
class ActivityControllerTest {
  private static final String CSRF_TOKEN = "test-csrf-token";
  private static final Instant NOW = Instant.parse("2026-04-20T08:00:00Z");

  @Autowired private MockMvcTester mvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private ActivityService activityService;
  @MockitoBean private JwtTokenProvider jwtTokenProvider;
  @MockitoBean private StringRedisTemplate stringRedisTemplate;

  @Test
  void should_return_public_activities() {
    when(activityService.getApprovedActivities(null, "run", 2, 50L))
        .thenReturn(new RecentActivitiesResponse(List.of(activityView(49L)), false));

    assertThat(mvc.get().uri("/api/activities?keyword=run&count=2&lastId=50"))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .satisfies(json -> assertThat(json).extractingPath("$.items[0].id").isEqualTo(49));
  }

  @Test
  void should_reject_invalid_activity_count() {
    assertThat(mvc.get().uri("/api/activities?count=51")).hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  void should_create_activity_with_valid_token_and_csrf() {
    CreateActivityRequest request =
        new CreateActivityRequest(
            "Run",
            "Morning run",
            "Track",
            NOW.plusSeconds(3600),
            NOW.plusSeconds(7200),
            NOW.plusSeconds(10800));
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(7L, Role.USER));
    when(activityService.createActivity(7L, request)).thenReturn(99L);

    assertThat(
            mvc.post()
                .uri("/api/activities")
                .cookie(authCookie())
                .cookie(csrfCookie())
                .header("X-XSRF-TOKEN", CSRF_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(serialize(request)))
        .hasStatus(HttpStatus.CREATED)
        .hasHeader("Location", "http://localhost:8080/api/activities/99");
  }

  @Test
  void should_return_activity_detail_with_valid_token() {
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(7L, Role.USER));
    when(activityService.getActivityDetail(new JwtAuthenticatedUser(7L, Role.USER), 99L))
        .thenReturn(activityView(99L));

    assertThat(mvc.get().uri("/api/activities/99").cookie(authCookie()))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .satisfies(json -> assertThat(json).extractingPath("$.title").isEqualTo("Run"));
  }

  @Test
  void should_return_activity_participants_with_valid_token() {
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(7L, Role.USER));
    when(activityService.getActivityParticipants(new JwtAuthenticatedUser(7L, Role.USER), 99L))
        .thenReturn(
            new ActivityParticipantListResponse(
                List.of(new ActivityParticipantView(8L, "Bob", NOW))));

    assertThat(mvc.get().uri("/api/activities/99/participants").cookie(authCookie()))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .satisfies(
            json ->
                assertThat(json).extractingPath("$.participants[0].displayName").isEqualTo("Bob"));
  }

  @Test
  void should_return_my_activities() {
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(7L, Role.USER));
    when(activityService.getMyActivities(7L))
        .thenReturn(new MyActivitiesResponse(List.of(activityView(99L)), List.of()));

    assertThat(mvc.get().uri("/api/activities/mine").cookie(authCookie()))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .satisfies(json -> assertThat(json).extractingPath("$.created[0].id").isEqualTo(99));
  }

  @Test
  void should_register_for_activity_with_valid_token_and_csrf() {
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(7L, Role.USER));

    assertThat(
            mvc.post()
                .uri("/api/activities/99/registrations")
                .cookie(authCookie())
                .cookie(csrfCookie())
                .header("X-XSRF-TOKEN", CSRF_TOKEN))
        .hasStatus(HttpStatus.NO_CONTENT);

    verify(activityService).register(new JwtAuthenticatedUser(7L, Role.USER), 99L);
  }

  @Test
  void should_cancel_activity_registration_with_valid_token_and_csrf() {
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(7L, Role.USER));

    assertThat(
            mvc.delete()
                .uri("/api/activities/99/registrations/me")
                .cookie(authCookie())
                .cookie(csrfCookie())
                .header("X-XSRF-TOKEN", CSRF_TOKEN))
        .hasStatus(HttpStatus.NO_CONTENT);

    verify(activityService).cancelRegistration(new JwtAuthenticatedUser(7L, Role.USER), 99L);
  }

  @Test
  void should_return_pending_activities_for_admin() {
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(2L, Role.ADMIN));
    when(activityService.getPendingActivities(new JwtAuthenticatedUser(2L, Role.ADMIN)))
        .thenReturn(new ActivityListResponse(List.of(activityView(99L))));

    assertThat(mvc.get().uri("/api/admin/activities/pending").cookie(authCookie()))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .satisfies(json -> assertThat(json).extractingPath("$.activities[0].id").isEqualTo(99));
  }

  @Test
  void should_approve_activity_with_valid_token_and_csrf() {
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(2L, Role.ADMIN));
    when(activityService.approveActivity(new JwtAuthenticatedUser(2L, Role.ADMIN), 99L))
        .thenReturn(activityView(99L));

    assertThat(
            mvc.post()
                .uri("/api/admin/activities/99/approve")
                .cookie(authCookie())
                .cookie(csrfCookie())
                .header("X-XSRF-TOKEN", CSRF_TOKEN))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .satisfies(json -> assertThat(json).extractingPath("$.id").isEqualTo(99));
  }

  @Test
  void should_reject_activity_with_valid_token_and_csrf() {
    RejectActivityRequest request = new RejectActivityRequest("Incomplete");
    when(jwtTokenProvider.parseToken("valid-token"))
        .thenReturn(new JwtAuthenticatedUser(2L, Role.ADMIN));
    when(activityService.rejectActivity(new JwtAuthenticatedUser(2L, Role.ADMIN), 99L, request))
        .thenReturn(activityView(99L));

    assertThat(
            mvc.post()
                .uri("/api/admin/activities/99/reject")
                .cookie(authCookie())
                .cookie(csrfCookie())
                .header("X-XSRF-TOKEN", CSRF_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(serialize(request)))
        .hasStatus(HttpStatus.OK);
  }

  private ActivityView activityView(Long id) {
    return new ActivityView(
        id,
        7L,
        "Alice",
        "Run",
        "Morning run",
        "Track",
        NOW.plusSeconds(3600),
        NOW.plusSeconds(7200),
        NOW.plusSeconds(10800),
        "approved",
        3L,
        false,
        null,
        NOW,
        null,
        NOW.minusSeconds(3600),
        NOW);
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
