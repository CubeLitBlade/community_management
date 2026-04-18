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

import io.github.cubelitblade.account.model.Role;
import io.github.cubelitblade.account.persistence.AccountRepository;
import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.activity.dto.ActivityParticipantListResponse;
import io.github.cubelitblade.activity.dto.ActivityView;
import io.github.cubelitblade.activity.dto.CreateActivityRequest;
import io.github.cubelitblade.activity.dto.RejectActivityRequest;
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
    given(timeProvider.now()).willReturn(NOW);
  }

  @Test
  @DisplayName("Create: should persist new activity as pending")
  void should_create_pending_activity() {
    given(idGenerator.nextId()).willReturn(99L);

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
  @DisplayName(
      "Get approved activities: should return all approved activities when keyword is blank")
  void should_return_all_approved_activities_when_keyword_is_blank() {
    Activity earlierActivity = approvedActivity(101L, "晨练活动", "操场", "一起跑步", NOW.plusSeconds(7200));
    Activity laterActivity = approvedActivity(102L, "羽毛球约练", "体育馆", "双打优先", NOW.plusSeconds(10800));
    given(activityRepository.findApprovedActivities(3, null))
        .willReturn(List.of(earlierActivity, laterActivity));
    given(accountRepository.findAccountById(anyLong())).willReturn(Optional.empty());

    var response =
        activityService.getApprovedActivities(
            new JwtAuthenticatedUser(9L, Role.USER), "   ", 2, null);

    verify(activityRepository).findApprovedActivities(3, null);
    verify(activityRepository, never()).searchApprovedActivities(any());
    assertThat(response.items()).extracting(ActivityView::id).containsExactly(101L, 102L);
    assertThat(response.hasMore()).isFalse();
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

    var result = activityService.approveActivity(new JwtAuthenticatedUser(7L, Role.OWNER), 101L);

    verify(activityRepository).update(activity);
    verify(notificationService).notifyActivityApproved(activity, 7L);
    assertThat(result.status()).isEqualTo(ActivityStatus.APPROVED.getValue());
    assertThat(activity.getStatus()).isEqualTo(ActivityStatus.APPROVED);
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
    return Activity.reconstitute(
        Activity.Snapshot.builder()
            .id(id)
            .creatorAccountId(7L)
            .title(title)
            .description(description)
            .location(location)
            .registrationDeadline(startTime.minusSeconds(1800))
            .startTime(startTime)
            .endTime(startTime.plusSeconds(3600))
            .status(ActivityStatus.APPROVED)
            .createdAt(NOW)
            .updatedAt(NOW)
            .build());
  }
}
