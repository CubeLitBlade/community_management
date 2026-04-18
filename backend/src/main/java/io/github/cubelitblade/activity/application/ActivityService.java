package io.github.cubelitblade.activity.application;

import io.github.cubelitblade.account.model.Account;
import io.github.cubelitblade.account.model.Role;
import io.github.cubelitblade.account.persistence.AccountRepository;
import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.activity.dto.*;
import io.github.cubelitblade.activity.exception.ActivityForbiddenException;
import io.github.cubelitblade.activity.exception.ActivityNotFoundException;
import io.github.cubelitblade.activity.model.Activity;
import io.github.cubelitblade.activity.model.ActivityStatus;
import io.github.cubelitblade.activity.persistence.ActivityRegistrationRepository;
import io.github.cubelitblade.activity.persistence.ActivityRepository;
import io.github.cubelitblade.common.exception.ApiErrorCode;
import io.github.cubelitblade.common.exception.ValidationException;
import io.github.cubelitblade.common.id.SnowflakeIdGenerator;
import io.github.cubelitblade.common.time.TimeProvider;
import io.github.cubelitblade.event.application.EventService;
import io.github.cubelitblade.event.model.Type;
import io.github.cubelitblade.event.model.payload.ActivityReminderEventPayload;
import io.github.cubelitblade.event.model.payload.EventPayloadMapper;
import io.github.cubelitblade.notification.application.NotificationService;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityService {
  static final Duration REMINDER_OFFSET = Duration.ofHours(1);

  private final ActivityRepository activityRepository;
  private final ActivityRegistrationRepository activityRegistrationRepository;
  private final AccountRepository accountRepository;
  private final SnowflakeIdGenerator idGenerator;
  private final TimeProvider timeProvider;
  private final NotificationService notificationService;
  private final EventService eventService;
  private final EventPayloadMapper eventPayloadMapper;

  @Transactional
  public Long createActivity(Long creatorAccountId, CreateActivityRequest request) {
    validateCreateRequest(request);
    Instant now = timeProvider.now();
    Activity activity =
        Activity.create(
            idGenerator.nextId(),
            creatorAccountId,
            request.title().trim(),
            request.description().trim(),
            request.location().trim(),
            request.registrationDeadline(),
            request.startTime(),
            request.endTime(),
            now);
    activityRepository.save(activity);
    return activity.getId();
  }

  @Transactional(readOnly = true)
  public RecentActivitiesResponse getApprovedActivities(
      JwtAuthenticatedUser authenticatedUser, String keyword, int count, Long lastId) {
    int fetchSize = count + 1;
    String normalizedKeyword = normalize(keyword);
    List<Activity> fetchedActivities =
        normalizedKeyword == null
            ? activityRepository.findApprovedActivities(fetchSize, lastId)
            : activityRepository.searchApprovedActivities(normalizedKeyword, fetchSize, lastId);
    List<ActivityView> activities =
        fetchedActivities.stream()
            .limit(count)
            .map(activity -> toView(activity, authenticatedUser))
            .toList();

    return new RecentActivitiesResponse(activities, fetchedActivities.size() > count);
  }

  @Transactional(readOnly = true)
  public ActivityView getActivityDetail(JwtAuthenticatedUser authenticatedUser, Long activityId) {
    Activity activity = getRequiredActivity(activityId);
    if (!canView(activity, authenticatedUser)) {
      throw ActivityForbiddenException.notVisible();
    }
    return toView(activity, authenticatedUser);
  }

  @Transactional(readOnly = true)
  public ActivityParticipantListResponse getActivityParticipants(
      JwtAuthenticatedUser authenticatedUser, Long activityId) {
    Activity activity = getRequiredActivity(activityId);
    if (!canViewParticipants(activity, authenticatedUser)) {
      throw ActivityForbiddenException.participantListNotVisible();
    }

    List<ActivityParticipantView> participants =
        activityRegistrationRepository.findByActivityId(activityId).stream()
            .map(
                registration ->
                    new ActivityParticipantView(
                        registration.accountId(),
                        accountRepository
                            .findAccountById(registration.accountId())
                            .map(this::displayName)
                            .orElse("未知用户"),
                        registration.createdAt()))
            .toList();
    return new ActivityParticipantListResponse(participants);
  }

  @Transactional(readOnly = true)
  public MyActivitiesResponse getMyActivities(Long accountId) {
    List<ActivityView> created =
        activityRepository.findByCreatorAccountId(accountId).stream()
            .sorted(Comparator.comparing(Activity::getCreatedAt).reversed())
            .map(activity -> toView(activity, new JwtAuthenticatedUser(accountId, Role.USER)))
            .toList();

    List<ActivityView> registered =
        activityRegistrationRepository.findActivityIdsByAccountId(accountId).stream()
            .map(activityRepository::findById)
            .flatMap(Optional::stream)
            .filter(activity -> activity.getStatus() == ActivityStatus.APPROVED)
            .sorted(Comparator.comparing(Activity::getStartTime))
            .map(activity -> toView(activity, new JwtAuthenticatedUser(accountId, Role.USER)))
            .toList();

    return new MyActivitiesResponse(created, registered);
  }

  @Transactional(readOnly = true)
  public ActivityListResponse getPendingActivities(JwtAuthenticatedUser authenticatedUser) {
    requireModerator(authenticatedUser);
    return new ActivityListResponse(
        activityRepository.findByStatus(ActivityStatus.PENDING).stream()
            .map(activity -> toView(activity, authenticatedUser))
            .toList());
  }

  @Transactional
  public ActivityView approveActivity(JwtAuthenticatedUser moderator, Long activityId) {
    requireModerator(moderator);
    Activity activity = getRequiredActivity(activityId);
    ensurePending(activity);
    ensureNotSelfModeration(activity, moderator);

    Instant now = timeProvider.now();
    activity.approve(moderator.accountId(), now);
    activityRepository.update(activity);
    notificationService.notifyActivityApproved(activity, moderator.accountId());
    scheduleReminderIfNeeded(activity);
    return toView(activity, moderator);
  }

  @Transactional
  public ActivityView rejectActivity(
      JwtAuthenticatedUser moderator, Long activityId, RejectActivityRequest request) {
    requireModerator(moderator);
    String reason = normalize(request == null ? null : request.reason());
    if (reason == null) {
      throw new ValidationException(ApiErrorCode.INVALID_REQUEST, "Rejection reason is required");
    }

    Activity activity = getRequiredActivity(activityId);
    ensurePending(activity);
    ensureNotSelfModeration(activity, moderator);

    activity.reject(moderator.accountId(), reason, timeProvider.now());
    activityRepository.update(activity);
    notificationService.notifyActivityRejected(activity, moderator.accountId());
    return toView(activity, moderator);
  }

  @Transactional
  public void register(JwtAuthenticatedUser authenticatedUser, Long activityId) {
    Activity activity = getRequiredActivity(activityId);
    ensureRegistrationAllowed(activity);

    try {
      activityRegistrationRepository.save(
          activityId, authenticatedUser.accountId(), timeProvider.now());
    } catch (DuplicateKeyException e) {
      throw new ValidationException(
          ApiErrorCode.INVALID_REQUEST, "Already registered for this activity");
    }
  }

  @Transactional
  public void cancelRegistration(JwtAuthenticatedUser authenticatedUser, Long activityId) {
    Activity activity = getRequiredActivity(activityId);
    ensureRegistrationWindowOpen(activity);
    activityRegistrationRepository.delete(activityId, authenticatedUser.accountId());
  }

  @Transactional(readOnly = true)
  public Optional<Activity> findActivity(Long activityId) {
    return activityRepository.findById(activityId);
  }

  @Transactional
  public void sendReminder(Long activityId) {
    Activity activity = getRequiredActivity(activityId);
    if (activity.getStatus() != ActivityStatus.APPROVED) {
      return;
    }

    List<Long> recipientAccountIds =
        activityRegistrationRepository.findAccountIdsByActivityId(activityId);
    if (recipientAccountIds.isEmpty()) {
      return;
    }

    notificationService.notifyActivityReminder(activity, recipientAccountIds);
  }

  private void validateCreateRequest(CreateActivityRequest request) {
    String title = normalize(request.title());
    String description = normalize(request.description());
    String location = normalize(request.location());

    if (title == null || description == null || location == null) {
      throw new ValidationException(
          ApiErrorCode.INVALID_REQUEST, "Title, description, and location are required");
    }
    if (request.registrationDeadline() == null
        || request.startTime() == null
        || request.endTime() == null) {
      throw new ValidationException(ApiErrorCode.INVALID_REQUEST, "Activity times are required");
    }
    if (!request.registrationDeadline().isBefore(request.startTime())) {
      throw new ValidationException(
          ApiErrorCode.INVALID_REQUEST, "Registration deadline must be before the start time");
    }
    if (!request.endTime().isAfter(request.startTime())) {
      throw new ValidationException(
          ApiErrorCode.INVALID_REQUEST, "End time must be after the start time");
    }
  }

  private void ensurePending(Activity activity) {
    if (activity.getStatus() != ActivityStatus.PENDING) {
      throw new ValidationException(
          ApiErrorCode.INVALID_REQUEST, "Only pending activities can be moderated");
    }
  }

  private void ensureNotSelfModeration(Activity activity, JwtAuthenticatedUser moderator) {
    if (moderator.role() != Role.OWNER
        && activity.getCreatorAccountId().equals(moderator.accountId())) {
      throw new ValidationException(
          ApiErrorCode.INVALID_REQUEST, "Activity creators cannot moderate their own activities");
    }
  }

  private void ensureRegistrationAllowed(Activity activity) {
    if (activity.getStatus() != ActivityStatus.APPROVED) {
      throw new ValidationException(
          ApiErrorCode.INVALID_REQUEST, "Only approved activities can accept registrations");
    }
    ensureRegistrationWindowOpen(activity);
  }

  private void ensureRegistrationWindowOpen(Activity activity) {
    if (!timeProvider.now().isBefore(activity.getRegistrationDeadline())) {
      throw new ValidationException(
          ApiErrorCode.INVALID_REQUEST, "Registration deadline has passed");
    }
  }

  private void scheduleReminderIfNeeded(Activity activity) {
    Instant remindAt = activity.getStartTime().minus(REMINDER_OFFSET);
    if (!remindAt.isAfter(timeProvider.now())) {
      return;
    }

    ActivityReminderEventPayload payload = new ActivityReminderEventPayload(activity.getId());
    try {
      eventService.createEvent(
          Type.ACTIVITY_REMINDER.getValue(), eventPayloadMapper.toJsonNode(payload), remindAt);
    } catch (RuntimeException e) {
      log.error("Failed to schedule reminder event for activity #{}.", activity.getId(), e);
    }
  }

  private Activity getRequiredActivity(Long activityId) {
    return activityRepository.findById(activityId).orElseThrow(ActivityNotFoundException::notFound);
  }

  private boolean canView(Activity activity, JwtAuthenticatedUser authenticatedUser) {
    if (activity.getStatus() == ActivityStatus.APPROVED) {
      return true;
    }
    if (authenticatedUser == null) {
      return false;
    }
    if (activity.getCreatorAccountId().equals(authenticatedUser.accountId())) {
      return true;
    }
    return authenticatedUser.role() == Role.ADMIN || authenticatedUser.role() == Role.OWNER;
  }

  private boolean canViewParticipants(Activity activity, JwtAuthenticatedUser authenticatedUser) {
    if (authenticatedUser == null) {
      return false;
    }
    if (activity.getCreatorAccountId().equals(authenticatedUser.accountId())) {
      return true;
    }
    return authenticatedUser.role() == Role.ADMIN || authenticatedUser.role() == Role.OWNER;
  }

  private void requireModerator(JwtAuthenticatedUser authenticatedUser) {
    if (authenticatedUser == null
        || (authenticatedUser.role() != Role.ADMIN && authenticatedUser.role() != Role.OWNER)) {
      throw ActivityForbiddenException.moderationRequired();
    }
  }

  private ActivityView toView(Activity activity, JwtAuthenticatedUser authenticatedUser) {
    String creatorDisplayName =
        accountRepository
            .findAccountById(activity.getCreatorAccountId())
            .map(this::displayName)
            .orElse(null);
    long participantCount = activityRegistrationRepository.countByActivityId(activity.getId());
    boolean viewerRegistered =
        authenticatedUser != null
            && activityRegistrationRepository.exists(
                activity.getId(), authenticatedUser.accountId());
    return ActivityView.from(activity, creatorDisplayName, participantCount, viewerRegistered);
  }

  private String displayName(Account account) {
    String nickname = normalize(account.getNickname());
    if (nickname != null) {
      return nickname;
    }
    return account.getUsername() == null ? null : account.getUsername().value();
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
