package io.github.cubelitblade.activity.persistence;

import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ActivityRegistrationRepository {
  private final ActivityRegistrationMapper activityRegistrationMapper;

  @Transactional
  public void save(Long activityId, Long accountId, Instant now) {
    ActivityRegistrationPo registration =
        ActivityRegistrationPo.builder()
            .activityId(activityId)
            .accountId(accountId)
            .createdAt(now)
            .build();

    InsertStatementProvider<ActivityRegistrationPo> insertStatement =
        insert(registration)
            .into(ActivityRegistrationDynamicSqlSupport.activityRegistrations)
            .map(ActivityRegistrationDynamicSqlSupport.activityId)
            .toProperty("activityId")
            .map(ActivityRegistrationDynamicSqlSupport.accountId)
            .toProperty("accountId")
            .map(ActivityRegistrationDynamicSqlSupport.createdAt)
            .toProperty("createdAt")
            .build()
            .render(RenderingStrategies.MYBATIS3);

    activityRegistrationMapper.insert(insertStatement);
  }

  @Transactional
  public void delete(Long activityId, Long accountId) {
    DeleteStatementProvider deleteStatement =
        deleteFrom(ActivityRegistrationDynamicSqlSupport.activityRegistrations)
            .where(ActivityRegistrationDynamicSqlSupport.activityId, isEqualTo(activityId))
            .and(ActivityRegistrationDynamicSqlSupport.accountId, isEqualTo(accountId))
            .build()
            .render(RenderingStrategies.MYBATIS3);
    activityRegistrationMapper.delete(deleteStatement);
  }

  @Transactional(readOnly = true)
  public boolean exists(Long activityId, Long accountId) {
    SelectStatementProvider selectStatement =
        countFrom(ActivityRegistrationDynamicSqlSupport.activityRegistrations)
            .where(ActivityRegistrationDynamicSqlSupport.activityId, isEqualTo(activityId))
            .and(ActivityRegistrationDynamicSqlSupport.accountId, isEqualTo(accountId))
            .build()
            .render(RenderingStrategies.MYBATIS3);
    return activityRegistrationMapper.count(selectStatement) > 0;
  }

  @Transactional(readOnly = true)
  public long countByActivityId(Long activityId) {
    SelectStatementProvider selectStatement =
        countFrom(ActivityRegistrationDynamicSqlSupport.activityRegistrations)
            .where(ActivityRegistrationDynamicSqlSupport.activityId, isEqualTo(activityId))
            .build()
            .render(RenderingStrategies.MYBATIS3);
    return activityRegistrationMapper.count(selectStatement);
  }

  @Transactional(readOnly = true)
  public List<Long> findAccountIdsByActivityId(Long activityId) {
    return findByActivityId(activityId).stream().map(ActivityRegistrationPo::accountId).toList();
  }

  @Transactional(readOnly = true)
  public List<ActivityRegistrationPo> findByActivityId(Long activityId) {
    SelectStatementProvider selectStatement =
        select(
                ActivityRegistrationDynamicSqlSupport.activityId,
                ActivityRegistrationDynamicSqlSupport.accountId,
                ActivityRegistrationDynamicSqlSupport.createdAt)
            .from(ActivityRegistrationDynamicSqlSupport.activityRegistrations)
            .where(ActivityRegistrationDynamicSqlSupport.activityId, isEqualTo(activityId))
            .orderBy(ActivityRegistrationDynamicSqlSupport.createdAt)
            .build()
            .render(RenderingStrategies.MYBATIS3);
    return activityRegistrationMapper.selectMany(selectStatement);
  }

  @Transactional(readOnly = true)
  public List<Long> findActivityIdsByAccountId(Long accountId) {
    SelectStatementProvider selectStatement =
        select(
                ActivityRegistrationDynamicSqlSupport.activityId,
                ActivityRegistrationDynamicSqlSupport.accountId,
                ActivityRegistrationDynamicSqlSupport.createdAt)
            .from(ActivityRegistrationDynamicSqlSupport.activityRegistrations)
            .where(ActivityRegistrationDynamicSqlSupport.accountId, isEqualTo(accountId))
            .orderBy(ActivityRegistrationDynamicSqlSupport.createdAt.descending())
            .build()
            .render(RenderingStrategies.MYBATIS3);
    return activityRegistrationMapper.selectMany(selectStatement).stream()
        .map(ActivityRegistrationPo::activityId)
        .toList();
  }
}
