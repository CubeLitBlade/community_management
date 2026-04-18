package io.github.cubelitblade.activity.persistence;

import static org.mybatis.dynamic.sql.SqlBuilder.*;

import io.github.cubelitblade.activity.model.Activity;
import io.github.cubelitblade.activity.model.ActivityStatus;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ActivityRepository {
  private final ActivityMapper activityMapper;

  @Transactional
  public void save(Activity activity) {
    ActivityPo activityPo = ActivityPo.of(activity);
    InsertStatementProvider<ActivityPo> insertStatement =
        insert(activityPo)
            .into(ActivityDynamicSqlSupport.activities)
            .map(ActivityDynamicSqlSupport.id)
            .toProperty("id")
            .map(ActivityDynamicSqlSupport.creatorAccountId)
            .toProperty("creatorAccountId")
            .map(ActivityDynamicSqlSupport.title)
            .toProperty("title")
            .map(ActivityDynamicSqlSupport.description)
            .toProperty("description")
            .map(ActivityDynamicSqlSupport.location)
            .toProperty("location")
            .map(ActivityDynamicSqlSupport.registrationDeadline)
            .toProperty("registrationDeadline")
            .map(ActivityDynamicSqlSupport.startTime)
            .toProperty("startTime")
            .map(ActivityDynamicSqlSupport.endTime)
            .toProperty("endTime")
            .map(ActivityDynamicSqlSupport.status)
            .toProperty("status")
            .map(ActivityDynamicSqlSupport.approvedBy)
            .toProperty("approvedBy")
            .map(ActivityDynamicSqlSupport.approvedAt)
            .toProperty("approvedAt")
            .map(ActivityDynamicSqlSupport.rejectedBy)
            .toProperty("rejectedBy")
            .map(ActivityDynamicSqlSupport.rejectedAt)
            .toProperty("rejectedAt")
            .map(ActivityDynamicSqlSupport.rejectionReason)
            .toProperty("rejectionReason")
            .map(ActivityDynamicSqlSupport.createdAt)
            .toProperty("createdAt")
            .map(ActivityDynamicSqlSupport.updatedAt)
            .toProperty("updatedAt")
            .build()
            .render(RenderingStrategies.MYBATIS3);

    activityMapper.insert(insertStatement);
  }

  @Transactional
  public void update(Activity activity) {
    ActivityPo activityPo = ActivityPo.of(activity);
    UpdateStatementProvider updateStatement =
        SqlBuilder.update(ActivityDynamicSqlSupport.activities)
            .set(ActivityDynamicSqlSupport.title)
            .equalTo(activityPo::title)
            .set(ActivityDynamicSqlSupport.description)
            .equalTo(activityPo::description)
            .set(ActivityDynamicSqlSupport.location)
            .equalTo(activityPo::location)
            .set(ActivityDynamicSqlSupport.registrationDeadline)
            .equalTo(activityPo::registrationDeadline)
            .set(ActivityDynamicSqlSupport.startTime)
            .equalTo(activityPo::startTime)
            .set(ActivityDynamicSqlSupport.endTime)
            .equalTo(activityPo::endTime)
            .set(ActivityDynamicSqlSupport.status)
            .equalTo(activityPo::status)
            .set(ActivityDynamicSqlSupport.approvedBy)
            .equalTo(activityPo::approvedBy)
            .set(ActivityDynamicSqlSupport.approvedAt)
            .equalTo(activityPo::approvedAt)
            .set(ActivityDynamicSqlSupport.rejectedBy)
            .equalTo(activityPo::rejectedBy)
            .set(ActivityDynamicSqlSupport.rejectedAt)
            .equalTo(activityPo::rejectedAt)
            .set(ActivityDynamicSqlSupport.rejectionReason)
            .equalTo(activityPo::rejectionReason)
            .set(ActivityDynamicSqlSupport.updatedAt)
            .equalTo(activityPo::updatedAt)
            .where(ActivityDynamicSqlSupport.id, isEqualTo(activity.getId()))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    activityMapper.update(updateStatement);
  }

  @Transactional(readOnly = true)
  public Optional<Activity> findById(Long activityId) {
    SelectStatementProvider selectStatement =
        select(
                ActivityDynamicSqlSupport.id,
                ActivityDynamicSqlSupport.creatorAccountId,
                ActivityDynamicSqlSupport.title,
                ActivityDynamicSqlSupport.description,
                ActivityDynamicSqlSupport.location,
                ActivityDynamicSqlSupport.registrationDeadline,
                ActivityDynamicSqlSupport.startTime,
                ActivityDynamicSqlSupport.endTime,
                ActivityDynamicSqlSupport.status,
                ActivityDynamicSqlSupport.approvedBy,
                ActivityDynamicSqlSupport.approvedAt,
                ActivityDynamicSqlSupport.rejectedBy,
                ActivityDynamicSqlSupport.rejectedAt,
                ActivityDynamicSqlSupport.rejectionReason,
                ActivityDynamicSqlSupport.createdAt,
                ActivityDynamicSqlSupport.updatedAt)
            .from(ActivityDynamicSqlSupport.activities)
            .where(ActivityDynamicSqlSupport.id, isEqualTo(activityId))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    return activityMapper.selectOne(selectStatement).map(ActivityPo::toActivity);
  }

  @Transactional(readOnly = true)
  public List<Activity> findByStatus(ActivityStatus status) {
    var selectBuilder =
        select(
                ActivityDynamicSqlSupport.id,
                ActivityDynamicSqlSupport.creatorAccountId,
                ActivityDynamicSqlSupport.title,
                ActivityDynamicSqlSupport.description,
                ActivityDynamicSqlSupport.location,
                ActivityDynamicSqlSupport.registrationDeadline,
                ActivityDynamicSqlSupport.startTime,
                ActivityDynamicSqlSupport.endTime,
                ActivityDynamicSqlSupport.status,
                ActivityDynamicSqlSupport.approvedBy,
                ActivityDynamicSqlSupport.approvedAt,
                ActivityDynamicSqlSupport.rejectedBy,
                ActivityDynamicSqlSupport.rejectedAt,
                ActivityDynamicSqlSupport.rejectionReason,
                ActivityDynamicSqlSupport.createdAt,
                ActivityDynamicSqlSupport.updatedAt)
            .from(ActivityDynamicSqlSupport.activities)
            .where(ActivityDynamicSqlSupport.status, isEqualTo(status.getValue()));

    SelectStatementProvider selectStatement =
        (status == ActivityStatus.PENDING
                ? selectBuilder.orderBy(ActivityDynamicSqlSupport.createdAt)
                : selectBuilder.orderBy(ActivityDynamicSqlSupport.startTime))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    return activityMapper.selectMany(selectStatement).stream().map(ActivityPo::toActivity).toList();
  }

  @Transactional(readOnly = true)
  public List<Activity> findApprovedActivities(int count, Long lastId) {
    SelectStatementProvider selectStatement =
        select(
                ActivityDynamicSqlSupport.id,
                ActivityDynamicSqlSupport.creatorAccountId,
                ActivityDynamicSqlSupport.title,
                ActivityDynamicSqlSupport.description,
                ActivityDynamicSqlSupport.location,
                ActivityDynamicSqlSupport.registrationDeadline,
                ActivityDynamicSqlSupport.startTime,
                ActivityDynamicSqlSupport.endTime,
                ActivityDynamicSqlSupport.status,
                ActivityDynamicSqlSupport.approvedBy,
                ActivityDynamicSqlSupport.approvedAt,
                ActivityDynamicSqlSupport.rejectedBy,
                ActivityDynamicSqlSupport.rejectedAt,
                ActivityDynamicSqlSupport.rejectionReason,
                ActivityDynamicSqlSupport.createdAt,
                ActivityDynamicSqlSupport.updatedAt)
            .from(ActivityDynamicSqlSupport.activities)
            .where(ActivityDynamicSqlSupport.status, isEqualTo(ActivityStatus.APPROVED.getValue()))
            .and(ActivityDynamicSqlSupport.id, isLessThanWhenPresent(lastId))
            .orderBy(ActivityDynamicSqlSupport.id.descending())
            .limit(count)
            .build()
            .render(RenderingStrategies.MYBATIS3);

    return activityMapper.selectMany(selectStatement).stream().map(ActivityPo::toActivity).toList();
  }

  @Transactional(readOnly = true)
  public void searchApprovedActivities(String keyword) {
    String searchPattern = "%" + keyword + "%";
    SelectStatementProvider selectStatement =
        select(
                ActivityDynamicSqlSupport.id,
                ActivityDynamicSqlSupport.creatorAccountId,
                ActivityDynamicSqlSupport.title,
                ActivityDynamicSqlSupport.description,
                ActivityDynamicSqlSupport.location,
                ActivityDynamicSqlSupport.registrationDeadline,
                ActivityDynamicSqlSupport.startTime,
                ActivityDynamicSqlSupport.endTime,
                ActivityDynamicSqlSupport.status,
                ActivityDynamicSqlSupport.approvedBy,
                ActivityDynamicSqlSupport.approvedAt,
                ActivityDynamicSqlSupport.rejectedBy,
                ActivityDynamicSqlSupport.rejectedAt,
                ActivityDynamicSqlSupport.rejectionReason,
                ActivityDynamicSqlSupport.createdAt,
                ActivityDynamicSqlSupport.updatedAt)
            .from(ActivityDynamicSqlSupport.activities)
            .where(ActivityDynamicSqlSupport.status, isEqualTo(ActivityStatus.APPROVED.getValue()))
            .and(
                ActivityDynamicSqlSupport.title,
                isLikeCaseInsensitive(searchPattern),
                or(ActivityDynamicSqlSupport.location, isLikeCaseInsensitive(searchPattern)),
                or(ActivityDynamicSqlSupport.description, isLikeCaseInsensitive(searchPattern)))
            .orderBy(ActivityDynamicSqlSupport.startTime)
            .build()
            .render(RenderingStrategies.MYBATIS3);

    activityMapper.selectMany(selectStatement).stream().map(ActivityPo::toActivity).toList();
  }

  @Transactional(readOnly = true)
  public List<Activity> searchApprovedActivities(String keyword, int count, Long lastId) {
    String searchPattern = "%" + keyword + "%";
    SelectStatementProvider selectStatement =
        select(
                ActivityDynamicSqlSupport.id,
                ActivityDynamicSqlSupport.creatorAccountId,
                ActivityDynamicSqlSupport.title,
                ActivityDynamicSqlSupport.description,
                ActivityDynamicSqlSupport.location,
                ActivityDynamicSqlSupport.registrationDeadline,
                ActivityDynamicSqlSupport.startTime,
                ActivityDynamicSqlSupport.endTime,
                ActivityDynamicSqlSupport.status,
                ActivityDynamicSqlSupport.approvedBy,
                ActivityDynamicSqlSupport.approvedAt,
                ActivityDynamicSqlSupport.rejectedBy,
                ActivityDynamicSqlSupport.rejectedAt,
                ActivityDynamicSqlSupport.rejectionReason,
                ActivityDynamicSqlSupport.createdAt,
                ActivityDynamicSqlSupport.updatedAt)
            .from(ActivityDynamicSqlSupport.activities)
            .where(ActivityDynamicSqlSupport.status, isEqualTo(ActivityStatus.APPROVED.getValue()))
            .and(ActivityDynamicSqlSupport.id, isLessThanWhenPresent(lastId))
            .and(
                ActivityDynamicSqlSupport.title,
                isLikeCaseInsensitive(searchPattern),
                or(ActivityDynamicSqlSupport.location, isLikeCaseInsensitive(searchPattern)),
                or(ActivityDynamicSqlSupport.description, isLikeCaseInsensitive(searchPattern)))
            .orderBy(ActivityDynamicSqlSupport.id.descending())
            .limit(count)
            .build()
            .render(RenderingStrategies.MYBATIS3);

    return activityMapper.selectMany(selectStatement).stream().map(ActivityPo::toActivity).toList();
  }

  @Transactional(readOnly = true)
  public List<Activity> findByCreatorAccountId(Long creatorAccountId) {
    SelectStatementProvider selectStatement =
        select(
                ActivityDynamicSqlSupport.id,
                ActivityDynamicSqlSupport.creatorAccountId,
                ActivityDynamicSqlSupport.title,
                ActivityDynamicSqlSupport.description,
                ActivityDynamicSqlSupport.location,
                ActivityDynamicSqlSupport.registrationDeadline,
                ActivityDynamicSqlSupport.startTime,
                ActivityDynamicSqlSupport.endTime,
                ActivityDynamicSqlSupport.status,
                ActivityDynamicSqlSupport.approvedBy,
                ActivityDynamicSqlSupport.approvedAt,
                ActivityDynamicSqlSupport.rejectedBy,
                ActivityDynamicSqlSupport.rejectedAt,
                ActivityDynamicSqlSupport.rejectionReason,
                ActivityDynamicSqlSupport.createdAt,
                ActivityDynamicSqlSupport.updatedAt)
            .from(ActivityDynamicSqlSupport.activities)
            .where(ActivityDynamicSqlSupport.creatorAccountId, isEqualTo(creatorAccountId))
            .orderBy(ActivityDynamicSqlSupport.createdAt.descending())
            .build()
            .render(RenderingStrategies.MYBATIS3);

    return activityMapper.selectMany(selectStatement).stream().map(ActivityPo::toActivity).toList();
  }
}
