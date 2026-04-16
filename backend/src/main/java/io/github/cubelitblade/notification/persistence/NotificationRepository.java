package io.github.cubelitblade.notification.persistence;

import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class NotificationRepository {
  private final NotificationMapper notificationMapper;

  @Transactional
  public void save(NotificationPo notificationPo) {
    InsertStatementProvider<NotificationPo> insertStatement =
        insert(notificationPo)
            .into(NotificationDynamicSqlSupport.notifications)
            .map(NotificationDynamicSqlSupport.id)
            .toProperty("id")
            .map(NotificationDynamicSqlSupport.recipientAccountId)
            .toProperty("recipientAccountId")
            .map(NotificationDynamicSqlSupport.actorAccountId)
            .toProperty("actorAccountId")
            .map(NotificationDynamicSqlSupport.type)
            .toProperty("type")
            .map(NotificationDynamicSqlSupport.targetType)
            .toProperty("targetType")
            .map(NotificationDynamicSqlSupport.targetId)
            .toProperty("targetId")
            .map(NotificationDynamicSqlSupport.content)
            .toProperty("content")
            .map(NotificationDynamicSqlSupport.isRead)
            .toProperty("isRead")
            .map(NotificationDynamicSqlSupport.readAt)
            .toProperty("readAt")
            .map(NotificationDynamicSqlSupport.createdAt)
            .toProperty("createdAt")
            .build()
            .render(RenderingStrategies.MYBATIS3);

    notificationMapper.insert(insertStatement);
  }

  @Transactional(readOnly = true)
  public List<NotificationPo> findByRecipientAccountId(Long recipientAccountId) {
    return findByRecipientAccountIdAndTypes(recipientAccountId, null);
  }

  @Transactional(readOnly = true)
  public List<NotificationPo> findByRecipientAccountIdAndTypes(
      Long recipientAccountId, List<String> typeValues) {
    if (typeValues != null && typeValues.isEmpty()) {
      return List.of();
    }

    var selectBuilder =
        select(
                NotificationDynamicSqlSupport.id,
                NotificationDynamicSqlSupport.recipientAccountId,
                NotificationDynamicSqlSupport.actorAccountId,
                NotificationDynamicSqlSupport.type,
                NotificationDynamicSqlSupport.targetType,
                NotificationDynamicSqlSupport.targetId,
                NotificationDynamicSqlSupport.content,
                NotificationDynamicSqlSupport.isRead,
                NotificationDynamicSqlSupport.readAt,
                NotificationDynamicSqlSupport.createdAt)
            .from(NotificationDynamicSqlSupport.notifications)
            .where(NotificationDynamicSqlSupport.recipientAccountId, isEqualTo(recipientAccountId));

    SelectStatementProvider selectStatement =
        (typeValues == null
                ? selectBuilder
                : selectBuilder.and(NotificationDynamicSqlSupport.type, isIn(typeValues)))
            .orderBy(NotificationDynamicSqlSupport.createdAt.descending())
            .build()
            .render(RenderingStrategies.MYBATIS3);

    return notificationMapper.selectMany(selectStatement);
  }

  @Transactional(readOnly = true)
  public Optional<NotificationPo> findOwnedById(Long id, Long recipientAccountId) {
    SelectStatementProvider selectStatement =
        select(
                NotificationDynamicSqlSupport.id,
                NotificationDynamicSqlSupport.recipientAccountId,
                NotificationDynamicSqlSupport.actorAccountId,
                NotificationDynamicSqlSupport.type,
                NotificationDynamicSqlSupport.targetType,
                NotificationDynamicSqlSupport.targetId,
                NotificationDynamicSqlSupport.content,
                NotificationDynamicSqlSupport.isRead,
                NotificationDynamicSqlSupport.readAt,
                NotificationDynamicSqlSupport.createdAt)
            .from(NotificationDynamicSqlSupport.notifications)
            .where(NotificationDynamicSqlSupport.id, isEqualTo(id))
            .and(NotificationDynamicSqlSupport.recipientAccountId, isEqualTo(recipientAccountId))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    return notificationMapper.selectOne(selectStatement);
  }

  @Transactional(readOnly = true)
  public Optional<NotificationPo> findById(Long id) {
    SelectStatementProvider selectStatement =
        select(
                NotificationDynamicSqlSupport.id,
                NotificationDynamicSqlSupport.recipientAccountId,
                NotificationDynamicSqlSupport.actorAccountId,
                NotificationDynamicSqlSupport.type,
                NotificationDynamicSqlSupport.targetType,
                NotificationDynamicSqlSupport.targetId,
                NotificationDynamicSqlSupport.content,
                NotificationDynamicSqlSupport.isRead,
                NotificationDynamicSqlSupport.readAt,
                NotificationDynamicSqlSupport.createdAt)
            .from(NotificationDynamicSqlSupport.notifications)
            .where(NotificationDynamicSqlSupport.id, isEqualTo(id))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    return notificationMapper.selectOne(selectStatement);
  }

  @Transactional(readOnly = true)
  public long countUnreadByRecipientAccountId(Long recipientAccountId) {
    return countUnreadByRecipientAccountIdAndTypes(recipientAccountId, null);
  }

  @Transactional(readOnly = true)
  public long countUnreadByRecipientAccountIdAndTypes(
      Long recipientAccountId, List<String> typeValues) {
    if (typeValues != null && typeValues.isEmpty()) {
      return 0;
    }

    var countBuilder =
        countFrom(NotificationDynamicSqlSupport.notifications)
            .where(NotificationDynamicSqlSupport.recipientAccountId, isEqualTo(recipientAccountId))
            .and(NotificationDynamicSqlSupport.isRead, isEqualTo(false));

    SelectStatementProvider selectStatement =
        (typeValues == null
                ? countBuilder
                : countBuilder.and(NotificationDynamicSqlSupport.type, isIn(typeValues)))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    return notificationMapper.count(selectStatement);
  }

  @Transactional
  public void updateReadStatus(NotificationPo notificationPo) {
    UpdateStatementProvider updateStatement =
        update(NotificationDynamicSqlSupport.notifications)
            .set(NotificationDynamicSqlSupport.isRead)
            .equalTo(notificationPo::isRead)
            .set(NotificationDynamicSqlSupport.readAt)
            .equalTo(notificationPo::readAt)
            .where(NotificationDynamicSqlSupport.id, isEqualTo(notificationPo.id()))
            .and(
                NotificationDynamicSqlSupport.recipientAccountId,
                isEqualTo(notificationPo.recipientAccountId()))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    notificationMapper.update(updateStatement);
  }

  @Transactional
  public void markAllRead(Long recipientAccountId, Instant readAt) {
    UpdateStatementProvider updateStatement =
        update(NotificationDynamicSqlSupport.notifications)
            .set(NotificationDynamicSqlSupport.isRead)
            .equalTo(true)
            .set(NotificationDynamicSqlSupport.readAt)
            .equalTo(readAt)
            .where(NotificationDynamicSqlSupport.recipientAccountId, isEqualTo(recipientAccountId))
            .and(NotificationDynamicSqlSupport.isRead, isEqualTo(false))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    notificationMapper.update(updateStatement);
  }
}
