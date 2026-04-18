package io.github.cubelitblade.activity.web;

import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.activity.application.ActivityService;
import io.github.cubelitblade.activity.dto.*;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequiredArgsConstructor
public class ActivityController {
  private final ActivityService activityService;

  @PostMapping("/api/activities")
  public ResponseEntity<Void> createActivity(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser,
      @RequestBody CreateActivityRequest request) {
    Long activityId = activityService.createActivity(authenticatedUser.accountId(), request);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(activityId)
            .toUri();
    return ResponseEntity.created(location).build();
  }

  @GetMapping("/api/activities")
  public ResponseEntity<ActivityListResponse> getActivities(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser) {
    return ResponseEntity.ok(activityService.getApprovedActivities(authenticatedUser));
  }

  @GetMapping("/api/activities/{id}")
  public ResponseEntity<ActivityView> getActivityDetail(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser, @PathVariable Long id) {
    return ResponseEntity.ok(activityService.getActivityDetail(authenticatedUser, id));
  }

  @GetMapping("/api/activities/mine")
  public ResponseEntity<MyActivitiesResponse> getMyActivities(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser) {
    return ResponseEntity.ok(activityService.getMyActivities(authenticatedUser.accountId()));
  }

  @PostMapping("/api/activities/{id}/registrations")
  public ResponseEntity<Void> registerForActivity(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser, @PathVariable Long id) {
    activityService.register(authenticatedUser, id);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/api/activities/{id}/registrations/me")
  public ResponseEntity<Void> cancelRegistration(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser, @PathVariable Long id) {
    activityService.cancelRegistration(authenticatedUser, id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/api/admin/activities/pending")
  public ResponseEntity<ActivityListResponse> getPendingActivities(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser) {
    return ResponseEntity.ok(activityService.getPendingActivities(authenticatedUser));
  }

  @PostMapping("/api/admin/activities/{id}/approve")
  public ResponseEntity<ActivityView> approveActivity(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser, @PathVariable Long id) {
    return ResponseEntity.ok(activityService.approveActivity(authenticatedUser, id));
  }

  @PostMapping("/api/admin/activities/{id}/reject")
  public ResponseEntity<ActivityView> rejectActivity(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser,
      @PathVariable Long id,
      @RequestBody RejectActivityRequest request) {
    return ResponseEntity.ok(activityService.rejectActivity(authenticatedUser, id, request));
  }
}
