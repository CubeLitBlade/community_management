package io.github.cubelitblade.activity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.github.cubelitblade.account.model.Account;
import io.github.cubelitblade.account.model.PasswordHash;
import io.github.cubelitblade.account.model.Role;
import io.github.cubelitblade.account.model.Status;
import io.github.cubelitblade.account.model.Username;
import io.github.cubelitblade.account.persistence.AccountRepository;
import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.activity.dto.ActivityParticipantListResponse;
import io.github.cubelitblade.activity.dto.ActivityView;
import io.github.cubelitblade.activity.dto.CreateActivityRequest;
import io.github.cubelitblade.activity.dto.RejectActivityRequest;
import io.github.cubelitblade.activity.exception.ActivityForbiddenException;
import io.github.cubelitblade.activity.exception.ActivityNotFoundException;
import io.github.cubelitblade.activity.model.Activity;
import io.github.cubelitblade.activity.model.ActivityStatus;
import io.github.cubelitblade.activity.persistence.ActivityRegistrationPo;
import io.github.cubelitblade.activity.persistence.ActivityRegistrationRepository;
import io.github.cubelitblade.activity.persistence.ActivityRepository;
import io.github.cubelitblade.common.exception.ValidationException;
import io.github.cubelitblade.common.id.SnowflakeIdGenerator;
import io.github.cubelitblade.common.time.TimeProvider;
import io.github.cubelitblade.event.application.EventService;
import io.github.cubelitblade.event.model.Type;
import io.github.cubelitblade.event.model.payload.EventPayloadMapper;
import io.github.cubelitblade.notification.application.NotificationService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import tools.jackson.databind.node.JsonNodeFactory;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {
  private static final Instant NOW = Instant.parse("2026-04-15T08:00:00Z");

  @Mock private ActivityRepository activityRepository;
  @Mock private ActivityRegistrationRepository activityRegistrationRepository;
  @Mock private AccountRepository accountRepository;
  @Mock private SnowflakeIdGenerator idGenerator;
  @Mock private TimeProvider timeProvider;
  @Mock private NotificationService notificationService;
  @Mock private EventService eventService;
  @Mock private EventPayloadMapper eventPayloadMapper;

  private ActivityService activityService;

  @BeforeEach
  void setUp() {
    activityService =
        new ActivityService(
            activityRepository,
            activityRegistrationRepository,
            accountRepository,
            idGenerator,
            timeProvider,
            notificationService,
            eventService,
            eventPayloadMapper);
  }

  @Test
  @DisplayName("Create: should persist new activity as pending")
  void should_create_pending_activity() {
    given(idGenerator.nextId()).willReturn(99L);
    given(timeProvider.now()).willReturn(NOW);

    Long activityId =
        activityService.createActivity(
            7L,
            new CreateActivityRequest(
                "晨练活动",
                "一起跑步",
                "操场",
                NOW.plusSeconds(3600),
                NOW.plusSeconds(7200),
                NOW.plusSeconds(10800)));

    ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
    verify(activityRepository).save(captor.capture());
    assertThat(activityId).isEqualTo(99L);
    assertThat(captor.getValue().getStatus()).isEqualTo(ActivityStatus.PENDING);
    assertThat(captor.getValue().getCreatorAccountId()).isEqualTo(7L);
  }

  @Test
  @DisplayName("Create: should trim text fields before saving")
  void should_trim_text_fields_when_creating_activity() {
    given(idGenerator.nextId()).willReturn(100L);
    given(timeProvider.now()).willReturn(NOW);

    activityService.createActivity(
        7L,
        new CreateActivityRequest(
            "  晨练活动  ",
            "  一起跑步  ",
            "  操场  ",
            NOW.plusSeconds(3600),
            NOW.plusSeconds(7200),
            NOW.plusSeconds(10800)));

    ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
    verify(activityRepository).save(captor.capture());
    assertThat(captor.getValue().getTitle()).isEqualTo("晨练活动");
    assertThat(captor.getValue().getDescription()).isEqualTo("一起跑步");
    assertThat(captor.getValue().getLocation()).isEqualTo("操场");
  }

  @Test
  @DisplayName("Create: should reject missing text and invalid times")
  void should_reject_invalid_create_requests() {
    assertThatThrownBy(
            () ->
                activityService.createActivity(
                    7L,
                    new CreateActivityRequest(
                        " ",
                        "一起跑步",
                        "操场",
                        NOW.plusSeconds(3600),
                        NOW.plusSeconds(7200),
                        NOW.plusSeconds(10800))))
        .isInstanceOf(ValidationException.class);
    assertThatThrownBy(
            () ->
                activityService.createActivity(
                    7L,
                    new CreateActivityRequest(
                        "晨练活动", "一起跑步", "操场", null, NOW.plusSeconds(7200), NOW.plusSeconds(10800))))
        .isInstanceOf(ValidationException.class);
    assertThatThrownBy(
            () ->
                activityService.createActivity(
                    7L,
                    new CreateActivityRequest(
                        "晨练活动",
                        "一起跑步",
                        "操场",
                        NOW.plusSeconds(7200),
                        NOW.plusSeconds(7200),
                        NOW.plusSeconds(10800))))
        .isInstanceOf(ValidationException.class);
    assertThatThrownBy(
            () ->
                activityService.createActivity(
                    7L,
                    new CreateActivityRequest(
                        "晨练活动",
                        "一起跑步",
                        "操场",
                        NOW.plusSeconds(3600),
                        NOW.plusSeconds(7200),
                        NOW.plusSeconds(7200))))
        .isInstanceOf(ValidationException.class);

    verify(activityRepository, never()).save(any());
  }

  @Test
  @DisplayName("Get approved activities: should search approved activities by keyword")
  void should_search_approved_activities_by_keyword() {
    Activity titleMatch = approvedActivity(201L, "羽毛球约练", "北区体育馆", "周末友谊赛", NOW.plusSeconds(7200));
    Activity locationMatch =
        approvedActivity(202L, "周末散步", "羽毛球中心", "轻松活动", NOW.plusSeconds(10800));
    Activity descriptionMatch =
        approvedActivity(203L, "室内运动", "综合馆", "欢迎羽毛球新手", NOW.plusSeconds(14400));
    given(activityRepository.searchApprovedActivities("羽毛球", 4, null))
        .willReturn(List.of(titleMatch, locationMatch, descriptionMatch));
    given(accountRepository.findAccountById(anyLong())).willReturn(Optional.empty());

    var response =
        activityService.getApprovedActivities(
            new JwtAuthenticatedUser(9L, Role.USER), "羽毛球", 3, null);

    verify(activityRepository).searchApprovedActivities("羽毛球", 4, null);
    verify(activityRepository, never()).findApprovedActivities(anyInt(), any());
    assertThat(response.items()).extracting(ActivityView::id).containsExactly(201L, 202L, 203L);
    assertThat(response.hasMore()).isFalse();
  }

  @Test
  @DisplayName("Get approved activities: should trim keyword before search")
  void should_trim_keyword_before_search() {
    Activity activity =
        approvedActivity(301L, "Badminton Night", "Gym", "Friendly doubles", NOW.plusSeconds(7200));
    given(activityRepository.searchApprovedActivities("badminton", 2, null))
        .willReturn(List.of(activity));
    given(accountRepository.findAccountById(anyLong())).willReturn(Optional.empty());

    var response =
        activityService.getApprovedActivities(
            new JwtAuthenticatedUser(9L, Role.USER), "  badminton  ", 1, null);

    verify(activityRepository).searchApprovedActivities("badminton", 2, null);
    assertThat(response.items()).extracting(ActivityView::id).containsExactly(301L);
    assertThat(response.hasMore()).isFalse();
  }

  @Test
  @DisplayName("Get approved activities: should list without keyword and report hasMore")
  void should_list_approved_activities_without_keyword_and_report_has_more() {
    Activity first =
        approvedActivity(401L, "First", "Gym", "Friendly doubles", NOW.plusSeconds(7200));
    Activity second =
        approvedActivity(402L, "Second", "Gym", "Friendly doubles", NOW.plusSeconds(10800));
    given(activityRepository.findApprovedActivities(2, 500L)).willReturn(List.of(first, second));
    given(accountRepository.findAccountById(anyLong())).willReturn(Optional.empty());

    var response =
        activityService.getApprovedActivities(
            new JwtAuthenticatedUser(9L, Role.USER), " ", 1, 500L);

    verify(activityRepository).findApprovedActivities(2, 500L);
    verify(activityRepository, never()).searchApprovedActivities(any(), anyInt(), any());
    assertThat(response.items()).extracting(ActivityView::id).containsExactly(401L);
    assertThat(response.hasMore()).isTrue();
  }

  @Test
  @DisplayName("Detail: should allow approved activity to anonymous viewer")
  void should_allow_anonymous_viewer_to_see_approved_activity_detail() {
    Activity activity = approvedActivity(501L, "开放活动", "操场", "一起跑步", NOW.plusSeconds(7200));
    given(activityRepository.findById(501L)).willReturn(Optional.of(activity));
    given(accountRepository.findAccountById(7L))
        .willReturn(Optional.of(account(7L, "creator", "组织者")));
    given(activityRegistrationRepository.countByActivityId(501L)).willReturn(3L);

    ActivityView view = activityService.getActivityDetail(null, 501L);

    assertThat(view.creatorDisplayName()).isEqualTo("组织者");
    assertThat(view.participantCount()).isEqualTo(3L);
    assertThat(view.viewerRegistered()).isFalse();
  }

  @Test
  @DisplayName("Detail: should allow creator and moderator to view non-public activity")
  void should_allow_creator_and_moderator_to_view_non_public_activity_detail() {
    Activity activity = activity(502L, 7L, ActivityStatus.PENDING, NOW.plusSeconds(7200));
    given(activityRepository.findById(502L)).willReturn(Optional.of(activity));
    given(accountRepository.findAccountById(7L)).willReturn(Optional.empty());

    assertThat(
            activityService.getActivityDetail(new JwtAuthenticatedUser(7L, Role.USER), 502L).id())
        .isEqualTo(502L);
    assertThat(
            activityService.getActivityDetail(new JwtAuthenticatedUser(2L, Role.ADMIN), 502L).id())
        .isEqualTo(502L);
  }

  @Test
  @DisplayName("Detail: should reject invisible or missing activity")
  void should_reject_invisible_or_missing_activity_detail() {
    Activity activity = activity(503L, 7L, ActivityStatus.REJECTED, NOW.plusSeconds(7200));
    given(activityRepository.findById(503L)).willReturn(Optional.of(activity));
    given(activityRepository.findById(404L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> activityService.getActivityDetail(null, 503L))
        .isInstanceOf(ActivityForbiddenException.class);
    assertThatThrownBy(
            () -> activityService.getActivityDetail(new JwtAuthenticatedUser(9L, Role.USER), 503L))
        .isInstanceOf(ActivityForbiddenException.class);
    assertThatThrownBy(
            () -> activityService.getActivityDetail(new JwtAuthenticatedUser(9L, Role.USER), 404L))
        .isInstanceOf(ActivityNotFoundException.class);
  }

  @Test
  @DisplayName("Approve: should notify creator and schedule reminder")
  void should_approve_and_schedule_reminder() {
    Activity activity =
        Activity.create(
            101L,
            7L,
            "晨练活动",
            "一起跑步",
            "操场",
            NOW.plusSeconds(3600),
            NOW.plusSeconds(7200),
            NOW.plusSeconds(10800),
            NOW);
    given(activityRepository.findById(101L)).willReturn(Optional.of(activity));
    given(eventPayloadMapper.toJsonNode(any())).willReturn(JsonNodeFactory.instance.objectNode());
    given(timeProvider.now()).willReturn(NOW);

    activityService.approveActivity(new JwtAuthenticatedUser(2L, Role.ADMIN), 101L);

    verify(activityRepository).update(activity);
    verify(notificationService).notifyActivityApproved(activity, 2L);
    verify(eventService)
        .createEvent(
            eq(Type.ACTIVITY_REMINDER.getValue()),
            any(),
            eq(activity.getStartTime().minus(ActivityService.REMINDER_OFFSET)));
  }

  @Test
  @DisplayName("Approve: should reject self moderation for admin")
  void should_reject_self_moderation_for_admin() {
    Activity activity =
        Activity.create(
            101L,
            7L,
            "晨练活动",
            "一起跑步",
            "操场",
            NOW.plusSeconds(3600),
            NOW.plusSeconds(7200),
            NOW.plusSeconds(10800),
            NOW);
    given(activityRepository.findById(101L)).willReturn(Optional.of(activity));

    assertThatThrownBy(
            () -> activityService.approveActivity(new JwtAuthenticatedUser(7L, Role.ADMIN), 101L))
        .isInstanceOf(ValidationException.class);

    verify(activityRepository, never()).update(any());
    verify(notificationService, never()).notifyActivityApproved(any(), any());
  }

  @Test
  @DisplayName("Approve: should allow owner to moderate own activity")
  void should_allow_owner_to_approve_own_activity() {
    Activity activity =
        Activity.create(
            101L,
            7L,
            "晨练活动",
            "一起跑步",
            "操场",
            NOW.plusSeconds(3600),
            NOW.plusSeconds(7200),
            NOW.plusSeconds(10800),
            NOW);
    given(activityRepository.findById(101L)).willReturn(Optional.of(activity));
    given(eventPayloadMapper.toJsonNode(any())).willReturn(JsonNodeFactory.instance.objectNode());
    given(timeProvider.now()).willReturn(NOW);

    var result = activityService.approveActivity(new JwtAuthenticatedUser(7L, Role.OWNER), 101L);

    verify(activityRepository).update(activity);
    verify(notificationService).notifyActivityApproved(activity, 7L);
    assertThat(result.status()).isEqualTo(ActivityStatus.APPROVED.getValue());
    assertThat(activity.getStatus()).isEqualTo(ActivityStatus.APPROVED);
  }

  @Test
  @DisplayName("Approve: should reject non-moderator and non-pending activity")
  void should_reject_approve_for_non_moderator_or_non_pending_activity() {
    Activity approved = approvedActivity(104L, "已通过", "操场", "一起跑步", NOW.plusSeconds(7200));
    given(activityRepository.findById(104L)).willReturn(Optional.of(approved));

    assertThatThrownBy(
            () -> activityService.approveActivity(new JwtAuthenticatedUser(9L, Role.USER), 104L))
        .isInstanceOf(ActivityForbiddenException.class);
    assertThatThrownBy(() -> activityService.approveActivity(null, 104L))
        .isInstanceOf(ActivityForbiddenException.class);
    assertThatThrownBy(
            () -> activityService.approveActivity(new JwtAuthenticatedUser(2L, Role.ADMIN), 104L))
        .isInstanceOf(ValidationException.class);

    verify(activityRepository, never()).update(any());
    verify(notificationService, never()).notifyActivityApproved(any(), anyLong());
  }

  @Test
  @DisplayName("Approve: should skip reminder when reminder time has passed")
  void should_skip_reminder_when_remind_time_has_passed() {
    Activity activity =
        Activity.create(
            105L,
            7L,
            "临近活动",
            "即将开始",
            "操场",
            NOW.minusSeconds(3600),
            NOW.plusSeconds(1800),
            NOW.plusSeconds(3600),
            NOW.minusSeconds(7200));
    given(activityRepository.findById(105L)).willReturn(Optional.of(activity));
    given(timeProvider.now()).willReturn(NOW);

    activityService.approveActivity(new JwtAuthenticatedUser(2L, Role.ADMIN), 105L);

    verify(activityRepository).update(activity);
    verify(eventService, never()).createEvent(eq(Type.ACTIVITY_REMINDER.getValue()), any(), any());
  }

  @Test
  @DisplayName("Approve: should still approve when reminder scheduling fails")
  void should_approve_even_if_reminder_scheduling_fails() {
    Activity activity =
        Activity.create(
            101L,
            7L,
            "晨练活动",
            "一起跑步",
            "操场",
            NOW.plusSeconds(3600),
            NOW.plusSeconds(7200),
            NOW.plusSeconds(10800),
            NOW);
    given(activityRepository.findById(101L)).willReturn(Optional.of(activity));
    given(eventPayloadMapper.toJsonNode(any())).willReturn(JsonNodeFactory.instance.objectNode());
    given(timeProvider.now()).willReturn(NOW);
    doThrow(new IllegalStateException("queue unavailable"))
        .when(eventService)
        .createEvent(eq(Type.ACTIVITY_REMINDER.getValue()), any(), any());

    var result = activityService.approveActivity(new JwtAuthenticatedUser(2L, Role.ADMIN), 101L);

    verify(activityRepository).update(activity);
    verify(notificationService).notifyActivityApproved(activity, 2L);
    verify(eventService)
        .createEvent(
            eq(Type.ACTIVITY_REMINDER.getValue()),
            any(),
            eq(activity.getStartTime().minus(ActivityService.REMINDER_OFFSET)));
    assertThat(result.status()).isEqualTo(ActivityStatus.APPROVED.getValue());
    assertThat(activity.getStatus()).isEqualTo(ActivityStatus.APPROVED);
  }

  @Test
  @DisplayName("Reject: should notify creator with reason")
  void should_reject_and_notify_creator() {
    Activity activity =
        Activity.create(
            101L,
            7L,
            "晨练活动",
            "一起跑步",
            "操场",
            NOW.plusSeconds(3600),
            NOW.plusSeconds(7200),
            NOW.plusSeconds(10800),
            NOW);
    given(activityRepository.findById(101L)).willReturn(Optional.of(activity));
    given(timeProvider.now()).willReturn(NOW);

    activityService.rejectActivity(
        new JwtAuthenticatedUser(2L, Role.ADMIN), 101L, new RejectActivityRequest("时间信息不完整"));

    verify(activityRepository).update(activity);
    verify(notificationService).notifyActivityRejected(activity, 2L);
    verify(eventService, never()).createEvent(eq(Type.ACTIVITY_REMINDER.getValue()), any(), any());
    assertThat(activity.getStatus()).isEqualTo(ActivityStatus.REJECTED);
  }

  @Test
  @DisplayName("Register: should reject signup after deadline")
  void should_reject_registration_after_deadline() {
    Activity activity =
        Activity.reconstitute(
            Activity.Snapshot.builder()
                .id(101L)
                .creatorAccountId(7L)
                .title("晨练活动")
                .description("一起跑步")
                .location("操场")
                .registrationDeadline(NOW.minusSeconds(1))
                .startTime(NOW.plusSeconds(7200))
                .endTime(NOW.plusSeconds(10800))
                .status(ActivityStatus.APPROVED)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build());
    given(activityRepository.findById(101L)).willReturn(Optional.of(activity));
    given(timeProvider.now()).willReturn(NOW);

    assertThatThrownBy(
            () -> activityService.register(new JwtAuthenticatedUser(9L, Role.USER), 101L))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  @DisplayName("Register: should reject duplicate signup")
  void should_reject_duplicate_signup() {
    Activity activity =
        Activity.reconstitute(
            Activity.Snapshot.builder()
                .id(101L)
                .creatorAccountId(7L)
                .title("晨练活动")
                .description("一起跑步")
                .location("操场")
                .registrationDeadline(NOW.plusSeconds(100))
                .startTime(NOW.plusSeconds(7200))
                .endTime(NOW.plusSeconds(10800))
                .status(ActivityStatus.APPROVED)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build());
    given(activityRepository.findById(101L)).willReturn(Optional.of(activity));
    given(timeProvider.now()).willReturn(NOW);
    org.mockito.Mockito.doThrow(new DuplicateKeyException("dup"))
        .when(activityRegistrationRepository)
        .save(101L, 9L, NOW);

    assertThatThrownBy(
            () -> activityService.register(new JwtAuthenticatedUser(9L, Role.USER), 101L))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  @DisplayName("Reject: should require moderator, reason, and pending activity")
  void should_reject_invalid_rejection_requests() {
    Activity approved = approvedActivity(106L, "已通过", "操场", "一起跑步", NOW.plusSeconds(7200));
    Activity pending = activity(107L, 7L, ActivityStatus.PENDING, NOW.plusSeconds(7200));
    given(activityRepository.findById(106L)).willReturn(Optional.of(approved));
    given(activityRepository.findById(107L)).willReturn(Optional.of(pending));

    assertThatThrownBy(
            () ->
                activityService.rejectActivity(
                    new JwtAuthenticatedUser(9L, Role.USER), 106L, new RejectActivityRequest("原因")))
        .isInstanceOf(ActivityForbiddenException.class);
    assertThatThrownBy(
            () ->
                activityService.rejectActivity(
                    new JwtAuthenticatedUser(2L, Role.ADMIN), 107L, new RejectActivityRequest(" ")))
        .isInstanceOf(ValidationException.class);
    assertThatThrownBy(
            () ->
                activityService.rejectActivity(
                    new JwtAuthenticatedUser(2L, Role.ADMIN),
                    106L,
                    new RejectActivityRequest("原因")))
        .isInstanceOf(ValidationException.class);
    assertThatThrownBy(
            () ->
                activityService.rejectActivity(
                    new JwtAuthenticatedUser(7L, Role.ADMIN),
                    107L,
                    new RejectActivityRequest("原因")))
        .isInstanceOf(ValidationException.class);

    verify(activityRepository, never()).update(any());
    verify(notificationService, never()).notifyActivityRejected(any(), anyLong());
  }

  @Test
  @DisplayName("Register: should save signup before deadline")
  void should_register_before_deadline() {
    Activity activity = approvedActivity(108L, "活动", "操场", "一起跑步", NOW.plusSeconds(7200));
    given(activityRepository.findById(108L)).willReturn(Optional.of(activity));
    given(timeProvider.now()).willReturn(NOW);

    activityService.register(new JwtAuthenticatedUser(9L, Role.USER), 108L);

    verify(activityRegistrationRepository).save(108L, 9L, NOW);
  }

  @Test
  @DisplayName("Register: should reject non-approved activity")
  void should_reject_registration_for_non_approved_activity() {
    Activity activity = activity(109L, 7L, ActivityStatus.PENDING, NOW.plusSeconds(7200));
    given(activityRepository.findById(109L)).willReturn(Optional.of(activity));

    assertThatThrownBy(
            () -> activityService.register(new JwtAuthenticatedUser(9L, Role.USER), 109L))
        .isInstanceOf(ValidationException.class);

    verify(activityRegistrationRepository, never()).save(anyLong(), anyLong(), any());
  }

  @Test
  @DisplayName("Cancel registration: should delete signup while window is open")
  void should_cancel_registration_before_deadline() {
    Activity activity = approvedActivity(110L, "活动", "操场", "一起跑步", NOW.plusSeconds(7200));
    given(activityRepository.findById(110L)).willReturn(Optional.of(activity));
    given(timeProvider.now()).willReturn(NOW);

    activityService.cancelRegistration(new JwtAuthenticatedUser(9L, Role.USER), 110L);

    verify(activityRegistrationRepository).delete(110L, 9L);
  }

  @Test
  @DisplayName("Cancel registration: should reject after deadline")
  void should_reject_cancel_registration_after_deadline() {
    Activity activity = approvedActivity(111L, "活动", "操场", "一起跑步", NOW.plusSeconds(7200));
    activity =
        Activity.reconstitute(
            Activity.Snapshot.builder()
                .id(activity.getId())
                .creatorAccountId(activity.getCreatorAccountId())
                .title(activity.getTitle())
                .description(activity.getDescription())
                .location(activity.getLocation())
                .registrationDeadline(NOW)
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .status(activity.getStatus())
                .createdAt(activity.getCreatedAt())
                .updatedAt(activity.getUpdatedAt())
                .build());
    given(activityRepository.findById(111L)).willReturn(Optional.of(activity));
    given(timeProvider.now()).willReturn(NOW);

    assertThatThrownBy(
            () -> activityService.cancelRegistration(new JwtAuthenticatedUser(9L, Role.USER), 111L))
        .isInstanceOf(ValidationException.class);

    verify(activityRegistrationRepository, never()).delete(anyLong(), anyLong());
  }

  @Test
  @DisplayName("My activities: should sort created and registered activities")
  void should_get_my_activities_sorted_and_filter_registered_by_approved_status() {
    Activity createdOld = activity(112L, 9L, ActivityStatus.PENDING, NOW.plusSeconds(7200), NOW);
    Activity createdNew =
        activity(113L, 9L, ActivityStatus.APPROVED, NOW.plusSeconds(10800), NOW.plusSeconds(60));
    Activity registeredLater =
        approvedActivity(114L, "Later", "操场", "一起跑步", NOW.plusSeconds(10800));
    Activity registeredEarlier =
        approvedActivity(115L, "Earlier", "操场", "一起跑步", NOW.plusSeconds(7200));
    Activity registeredRejected =
        activity(116L, 7L, ActivityStatus.REJECTED, NOW.plusSeconds(3600));
    given(activityRepository.findByCreatorAccountId(9L))
        .willReturn(List.of(createdOld, createdNew));
    given(activityRegistrationRepository.findActivityIdsByAccountId(9L))
        .willReturn(List.of(114L, 116L, 115L, 999L));
    given(activityRepository.findById(114L)).willReturn(Optional.of(registeredLater));
    given(activityRepository.findById(115L)).willReturn(Optional.of(registeredEarlier));
    given(activityRepository.findById(116L)).willReturn(Optional.of(registeredRejected));
    given(activityRepository.findById(999L)).willReturn(Optional.empty());
    given(accountRepository.findAccountById(anyLong())).willReturn(Optional.empty());

    var response = activityService.getMyActivities(9L);

    assertThat(response.created()).extracting(ActivityView::id).containsExactly(113L, 112L);
    assertThat(response.registered()).extracting(ActivityView::id).containsExactly(115L, 114L);
  }

  @Test
  @DisplayName("Pending activities: should require moderator and map pending list")
  void should_get_pending_activities_for_moderator_only() {
    Activity pending = activity(117L, 7L, ActivityStatus.PENDING, NOW.plusSeconds(7200));
    given(activityRepository.findByStatus(ActivityStatus.PENDING)).willReturn(List.of(pending));
    given(accountRepository.findAccountById(7L)).willReturn(Optional.empty());

    assertThat(
            activityService
                .getPendingActivities(new JwtAuthenticatedUser(2L, Role.ADMIN))
                .activities())
        .extracting(ActivityView::id)
        .containsExactly(117L);
    assertThatThrownBy(
            () -> activityService.getPendingActivities(new JwtAuthenticatedUser(9L, Role.USER)))
        .isInstanceOf(ActivityForbiddenException.class);
  }

  @Test
  @DisplayName("Find activity: should delegate repository lookup")
  void should_find_activity() {
    Activity activity = activity(118L, 7L, ActivityStatus.PENDING, NOW.plusSeconds(7200));
    given(activityRepository.findById(118L)).willReturn(Optional.of(activity));
    given(activityRepository.findById(404L)).willReturn(Optional.empty());

    assertThat(activityService.findActivity(118L)).contains(activity);
    assertThat(activityService.findActivity(404L)).isEmpty();
  }

  @Test
  @DisplayName("Reminder: should skip non-approved or empty-recipient activities")
  void should_skip_reminder_for_non_approved_or_empty_recipients() {
    Activity pending = activity(119L, 7L, ActivityStatus.PENDING, NOW.plusSeconds(7200));
    Activity approved = approvedActivity(120L, "活动", "操场", "一起跑步", NOW.plusSeconds(7200));
    given(activityRepository.findById(119L)).willReturn(Optional.of(pending));
    given(activityRepository.findById(120L)).willReturn(Optional.of(approved));
    given(activityRegistrationRepository.findAccountIdsByActivityId(120L)).willReturn(List.of());

    activityService.sendReminder(119L);
    activityService.sendReminder(120L);

    verify(notificationService, never()).notifyActivityReminder(any(), any());
  }

  @Test
  @DisplayName("Participants: should use nickname, username, or unknown display name")
  void should_resolve_participant_display_names() {
    Activity activity = approvedActivity(121L, "活动", "操场", "一起跑步", NOW.plusSeconds(7200));
    given(activityRepository.findById(121L)).willReturn(Optional.of(activity));
    given(activityRegistrationRepository.findByActivityId(121L))
        .willReturn(
            List.of(
                ActivityRegistrationPo.builder()
                    .activityId(121L)
                    .accountId(3L)
                    .createdAt(NOW)
                    .build(),
                ActivityRegistrationPo.builder()
                    .activityId(121L)
                    .accountId(4L)
                    .createdAt(NOW.plusSeconds(1))
                    .build(),
                ActivityRegistrationPo.builder()
                    .activityId(121L)
                    .accountId(5L)
                    .createdAt(NOW.plusSeconds(2))
                    .build()));
    given(accountRepository.findAccountById(3L))
        .willReturn(Optional.of(account(3L, "user3", "昵称")));
    given(accountRepository.findAccountById(4L)).willReturn(Optional.of(account(4L, "user4", " ")));
    given(accountRepository.findAccountById(5L)).willReturn(Optional.empty());

    ActivityParticipantListResponse response =
        activityService.getActivityParticipants(new JwtAuthenticatedUser(2L, Role.ADMIN), 121L);

    assertThat(response.participants())
        .extracting(participant -> participant.displayName())
        .containsExactly("昵称", "user4", "未知用户");
  }

  @Test
  @DisplayName("Reminder: should notify currently registered users")
  void should_notify_registered_users_on_reminder() {
    Activity activity =
        Activity.reconstitute(
            Activity.Snapshot.builder()
                .id(101L)
                .creatorAccountId(7L)
                .title("晨练活动")
                .description("一起跑步")
                .location("操场")
                .registrationDeadline(NOW.plusSeconds(100))
                .startTime(NOW.plusSeconds(7200))
                .endTime(NOW.plusSeconds(10800))
                .status(ActivityStatus.APPROVED)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build());
    given(activityRepository.findById(101L)).willReturn(Optional.of(activity));
    given(activityRegistrationRepository.findAccountIdsByActivityId(101L))
        .willReturn(List.of(3L, 4L));

    activityService.sendReminder(101L);

    verify(notificationService).notifyActivityReminder(activity, List.of(3L, 4L));
  }

  @Test
  @DisplayName("Participants: should allow creator to view registered users")
  void should_allow_creator_to_view_registered_users() {
    Activity activity =
        Activity.reconstitute(
            Activity.Snapshot.builder()
                .id(101L)
                .creatorAccountId(7L)
                .title("晨练活动")
                .description("一起跑步")
                .location("操场")
                .registrationDeadline(NOW.plusSeconds(100))
                .startTime(NOW.plusSeconds(7200))
                .endTime(NOW.plusSeconds(10800))
                .status(ActivityStatus.APPROVED)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build());
    given(activityRepository.findById(101L)).willReturn(Optional.of(activity));
    given(activityRegistrationRepository.findByActivityId(101L))
        .willReturn(
            List.of(
                ActivityRegistrationPo.builder()
                    .activityId(101L)
                    .accountId(3L)
                    .createdAt(NOW.plusSeconds(10))
                    .build(),
                ActivityRegistrationPo.builder()
                    .activityId(101L)
                    .accountId(4L)
                    .createdAt(NOW.plusSeconds(20))
                    .build()));
    given(accountRepository.findAccountById(3L)).willReturn(Optional.empty());
    given(accountRepository.findAccountById(4L)).willReturn(Optional.empty());

    ActivityParticipantListResponse response =
        activityService.getActivityParticipants(new JwtAuthenticatedUser(7L, Role.USER), 101L);

    assertThat(response.participants()).hasSize(2);
    assertThat(response.participants().get(0).accountId()).isEqualTo(3L);
    assertThat(response.participants().get(1).accountId()).isEqualTo(4L);
  }

  @Test
  @DisplayName("Participants: should reject normal viewer")
  void should_reject_normal_viewer_from_participant_list() {
    Activity activity =
        Activity.reconstitute(
            Activity.Snapshot.builder()
                .id(101L)
                .creatorAccountId(7L)
                .title("晨练活动")
                .description("一起跑步")
                .location("操场")
                .registrationDeadline(NOW.plusSeconds(100))
                .startTime(NOW.plusSeconds(7200))
                .endTime(NOW.plusSeconds(10800))
                .status(ActivityStatus.APPROVED)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build());
    given(activityRepository.findById(101L)).willReturn(Optional.of(activity));

    assertThatThrownBy(
            () ->
                activityService.getActivityParticipants(
                    new JwtAuthenticatedUser(9L, Role.USER), 101L))
        .isInstanceOf(io.github.cubelitblade.activity.exception.ActivityForbiddenException.class);
  }

  private Activity approvedActivity(
      Long id, String title, String location, String description, Instant startTime) {
    return activity(id, 7L, title, location, description, ActivityStatus.APPROVED, startTime, NOW);
  }

  private Activity activity(
      Long id, Long creatorAccountId, ActivityStatus status, Instant startTime) {
    return activity(id, creatorAccountId, status, startTime, NOW);
  }

  private Activity activity(
      Long id, Long creatorAccountId, ActivityStatus status, Instant startTime, Instant createdAt) {
    return activity(id, creatorAccountId, "晨练活动", "操场", "一起跑步", status, startTime, createdAt);
  }

  private Activity activity(
      Long id,
      Long creatorAccountId,
      String title,
      String location,
      String description,
      ActivityStatus status,
      Instant startTime,
      Instant createdAt) {
    return Activity.reconstitute(
        Activity.Snapshot.builder()
            .id(id)
            .creatorAccountId(creatorAccountId)
            .title(title)
            .description(description)
            .location(location)
            .registrationDeadline(startTime.minusSeconds(1800))
            .startTime(startTime)
            .endTime(startTime.plusSeconds(3600))
            .status(status)
            .createdAt(createdAt)
            .updatedAt(createdAt)
            .build());
  }

  private Account account(Long id, String username, String nickname) {
    return Account.reconstitute(
        Account.Snapshot.builder()
            .id(id)
            .username(Username.of(username))
            .passwordHash(new PasswordHash("hashed-" + username))
            .nickname(nickname)
            .role(Role.USER)
            .status(Status.NORMAL)
            .createdAt(NOW)
            .updatedAt(NOW)
            .build());
  }
}
