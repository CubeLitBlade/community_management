package io.github.cubelitblade.activity.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.cubelitblade.activity.model.Activity;
import io.github.cubelitblade.activity.model.ActivityStatus;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ActivityPersistenceIntegrationTest {
  private static final Instant NOW = Instant.parse("2026-04-20T08:00:00Z");

  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private ActivityRepository activityRepository;
  @Autowired private ActivityRegistrationRepository activityRegistrationRepository;

  @BeforeEach
  void setUp() {
    insertAccount(10101L, "activity_creator", "Activity Creator");
    insertAccount(10102L, "other_creator", "Other Creator");
    insertAccount(10103L, "activity_admin", "Activity Admin");
    insertAccount(10104L, "activity_participant", "Activity Participant");
  }

  @Test
  @DisplayName("ActivityRepository: should save, approve, update, and find by id")
  void should_save_update_and_find_activity_by_id() {
    Activity activity = createActivity(10201L, 10101L, "Board Game Night", NOW);
    activityRepository.save(activity);

    assertThat(activityRepository.findById(10201L))
        .hasValueSatisfying(
            persisted -> {
              assertThat(persisted.getCreatorAccountId()).isEqualTo(10101L);
              assertThat(persisted.getTitle()).isEqualTo("Board Game Night");
              assertThat(persisted.getStatus()).isEqualTo(ActivityStatus.PENDING);
              assertThat(persisted.getCreatedAt()).isEqualTo(NOW);
            });

    activity.approve(10103L, NOW.plusSeconds(60));
    activityRepository.update(activity);

    assertThat(activityRepository.findById(10201L))
        .hasValueSatisfying(
            persisted -> {
              assertThat(persisted.getStatus()).isEqualTo(ActivityStatus.APPROVED);
              assertThat(persisted.getApprovedBy()).isEqualTo(10103L);
              assertThat(persisted.getApprovedAt()).isEqualTo(NOW.plusSeconds(60));
              assertThat(persisted.getUpdatedAt()).isEqualTo(NOW.plusSeconds(60));
            });
  }

  @Test
  @DisplayName("ActivityRepository: should filter by status and use status-specific ordering")
  void should_filter_by_status_with_status_specific_ordering() {
    insertActivity(
        10211L, 10101L, "Pending later", "pending", NOW.plusSeconds(20), NOW.plusSeconds(600));
    insertActivity(10212L, 10101L, "Pending earlier", "pending", NOW, NOW.plusSeconds(500));
    insertActivity(
        10213L, 10101L, "Approved later", "approved", NOW.plusSeconds(30), NOW.plusSeconds(900));
    insertActivity(
        10214L, 10101L, "Approved earlier", "approved", NOW.plusSeconds(40), NOW.plusSeconds(800));
    insertActivity(
        10215L, 10101L, "Rejected", "rejected", NOW.plusSeconds(10), NOW.plusSeconds(700));

    List<Activity> pending =
        activityRepository.findByStatus(ActivityStatus.PENDING).stream()
            .filter(activity -> List.of(10211L, 10212L).contains(activity.getId()))
            .toList();
    List<Activity> approved =
        activityRepository.findByStatus(ActivityStatus.APPROVED).stream()
            .filter(activity -> List.of(10213L, 10214L).contains(activity.getId()))
            .toList();

    assertThat(pending).extracting(Activity::getId).containsExactly(10212L, 10211L);
    assertThat(approved).extracting(Activity::getId).containsExactly(10214L, 10213L);
  }

  @Test
  @DisplayName("ActivityRepository: should page approved activities by id cursor")
  void should_page_approved_activities_by_id_cursor() {
    insertActivity(10221L, 10101L, "Approved low", "approved", NOW, NOW.plusSeconds(100));
    insertActivity(10222L, 10101L, "Pending ignored", "pending", NOW, NOW.plusSeconds(200));
    insertActivity(10223L, 10101L, "Approved high", "approved", NOW, NOW.plusSeconds(300));
    insertActivity(10224L, 10101L, "Approved top", "approved", NOW, NOW.plusSeconds(400));

    List<Activity> firstPage = activityRepository.findApprovedActivities(2, 10230L);
    List<Activity> secondPage = activityRepository.findApprovedActivities(2, 10223L);

    assertThat(firstPage).extracting(Activity::getId).containsExactly(10224L, 10223L);
    assertThat(firstPage).extracting(Activity::getStatus).containsOnly(ActivityStatus.APPROVED);
    assertThat(secondPage).extracting(Activity::getId).containsExactly(10221L);
  }

  @Test
  @DisplayName("ActivityRepository: should search approved activities by keyword")
  void should_search_approved_activities_by_keyword() {
    insertActivity(
        10231L,
        10101L,
        "Lantern Workshop",
        "Make paper lanterns",
        "approved",
        NOW,
        NOW.plusSeconds(100));
    insertActivity(
        10232L,
        10101L,
        "Craft Night",
        "Bring lantern supplies",
        "approved",
        NOW.plusSeconds(1),
        NOW.plusSeconds(200));
    insertActivity(
        10233L,
        10101L,
        "Park Meetup",
        "Outdoor games",
        "approved",
        NOW.plusSeconds(2),
        NOW.plusSeconds(300),
        "Lantern Hall");
    insertActivity(
        10234L,
        10101L,
        "Lantern Pending",
        "Should not appear",
        "pending",
        NOW.plusSeconds(3),
        NOW.plusSeconds(400));

    List<Activity> activities = activityRepository.searchApprovedActivities("lantern", 10, 10240L);

    assertThat(activities).extracting(Activity::getId).containsExactly(10233L, 10232L, 10231L);
    assertThat(activities).extracting(Activity::getStatus).containsOnly(ActivityStatus.APPROVED);
  }

  @Test
  @DisplayName("ActivityRepository: should find activities by creator ordered by created time")
  void should_find_activities_by_creator_ordered_by_created_time() {
    insertActivity(10241L, 10101L, "First own", "pending", NOW, NOW.plusSeconds(100));
    insertActivity(
        10242L, 10101L, "Second own", "approved", NOW.plusSeconds(10), NOW.plusSeconds(200));
    insertActivity(
        10243L, 10102L, "Other creator", "approved", NOW.plusSeconds(20), NOW.plusSeconds(300));

    List<Activity> activities =
        activityRepository.findByCreatorAccountId(10101L).stream()
            .filter(activity -> List.of(10241L, 10242L).contains(activity.getId()))
            .toList();

    assertThat(activities).extracting(Activity::getId).containsExactly(10242L, 10241L);
  }

  @Test
  @DisplayName("ActivityRegistrationRepository: should manage registrations and enforce uniqueness")
  void should_manage_registrations_and_enforce_uniqueness() {
    insertActivity(10251L, 10101L, "Registered activity", "approved", NOW, NOW.plusSeconds(100));
    insertActivity(10252L, 10101L, "Later activity", "approved", NOW, NOW.plusSeconds(200));

    activityRegistrationRepository.save(10251L, 10101L, NOW);
    activityRegistrationRepository.save(10251L, 10104L, NOW.plusSeconds(10));
    activityRegistrationRepository.save(10252L, 10104L, NOW.plusSeconds(20));

    assertThat(activityRegistrationRepository.exists(10251L, 10104L)).isTrue();
    assertThat(activityRegistrationRepository.countByActivityId(10251L)).isEqualTo(2);
    assertThat(activityRegistrationRepository.findAccountIdsByActivityId(10251L))
        .containsExactly(10101L, 10104L);
    assertThat(activityRegistrationRepository.findActivityIdsByAccountId(10104L))
        .containsExactly(10252L, 10251L);

    activityRegistrationRepository.delete(10251L, 10101L);

    assertThat(activityRegistrationRepository.exists(10251L, 10101L)).isFalse();
    assertThat(activityRegistrationRepository.countByActivityId(10251L)).isEqualTo(1);
    assertThatThrownBy(() -> activityRegistrationRepository.save(10251L, 10104L, NOW))
        .isInstanceOf(DuplicateKeyException.class);
  }

  private Activity createActivity(Long id, Long creatorAccountId, String title, Instant now) {
    return Activity.create(
        id,
        creatorAccountId,
        title,
        "Community activity",
        "Community Center",
        now.plusSeconds(3600),
        now.plusSeconds(7200),
        now.plusSeconds(10800),
        now);
  }

  private void insertActivity(
      Long id,
      Long creatorAccountId,
      String title,
      String status,
      Instant createdAt,
      Instant startTime) {
    insertActivity(id, creatorAccountId, title, "Community activity", status, createdAt, startTime);
  }

  private void insertActivity(
      Long id,
      Long creatorAccountId,
      String title,
      String description,
      String status,
      Instant createdAt,
      Instant startTime) {
    insertActivity(
        id, creatorAccountId, title, description, status, createdAt, startTime, "Community Center");
  }

  private void insertActivity(
      Long id,
      Long creatorAccountId,
      String title,
      String description,
      String status,
      Instant createdAt,
      Instant startTime,
      String location) {
    jdbcTemplate.update(
        """
        insert into activities(
          id, creator_account_id, title, description, location, registration_deadline,
          start_time, end_time, status, created_at, updated_at
        )
        values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        id,
        creatorAccountId,
        title,
        description,
        location,
        ts(startTime.minusSeconds(3600)),
        ts(startTime),
        ts(startTime.plusSeconds(3600)),
        status,
        ts(createdAt),
        ts(createdAt));
  }

  private void insertAccount(Long id, String username, String nickname) {
    jdbcTemplate.update(
        """
        insert into accounts(id, username, nickname, password_hash, status, role, created_at, updated_at)
        values (?, ?, ?, 'hash', 'normal', 'user', ?, ?)
        """,
        id,
        username,
        nickname,
        ts(NOW),
        ts(NOW));
  }

  private Timestamp ts(Instant instant) {
    return instant == null ? null : Timestamp.from(instant);
  }
}
