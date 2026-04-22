package io.github.cubelitblade.message.persistence;

import static org.mybatis.dynamic.sql.SqlBuilder.insert;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.select;
import static org.mybatis.dynamic.sql.SqlBuilder.update;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class PrivateMessageRepository {
  private static final BasicColumn[] SELECT_COLUMNS = {
    PrivateMessageDynamicSqlSupport.id,
    PrivateMessageDynamicSqlSupport.senderAccountId,
    PrivateMessageDynamicSqlSupport.recipientAccountId,
    PrivateMessageDynamicSqlSupport.content,
    PrivateMessageDynamicSqlSupport.isRead,
    PrivateMessageDynamicSqlSupport.readAt,
    PrivateMessageDynamicSqlSupport.createdAt
  };

  private final PrivateMessageMapper privateMessageMapper;

  @Transactional
  public void save(PrivateMessagePo message) {
    InsertStatementProvider<PrivateMessagePo> insertStatement =
        insert(message)
            .into(PrivateMessageDynamicSqlSupport.privateMessages)
            .map(PrivateMessageDynamicSqlSupport.id)
            .toProperty("id")
            .map(PrivateMessageDynamicSqlSupport.senderAccountId)
            .toProperty("senderAccountId")
            .map(PrivateMessageDynamicSqlSupport.recipientAccountId)
            .toProperty("recipientAccountId")
            .map(PrivateMessageDynamicSqlSupport.content)
            .toProperty("content")
            .map(PrivateMessageDynamicSqlSupport.isRead)
            .toProperty("isRead")
            .map(PrivateMessageDynamicSqlSupport.readAt)
            .toProperty("readAt")
            .map(PrivateMessageDynamicSqlSupport.createdAt)
            .toProperty("createdAt")
            .build()
            .render(RenderingStrategies.MYBATIS3);

    privateMessageMapper.insert(insertStatement);
  }

  @Transactional(readOnly = true)
  public List<PrivateMessagePo> findByParticipantAccountId(Long accountId) {
    SelectStatementProvider selectStatement =
        select(SELECT_COLUMNS)
            .from(PrivateMessageDynamicSqlSupport.privateMessages)
            .where(PrivateMessageDynamicSqlSupport.senderAccountId, isEqualTo(accountId))
            .or(PrivateMessageDynamicSqlSupport.recipientAccountId, isEqualTo(accountId))
            .orderBy(PrivateMessageDynamicSqlSupport.createdAt.descending())
            .build()
            .render(RenderingStrategies.MYBATIS3);

    return privateMessageMapper.selectMany(selectStatement);
  }

  @Transactional(readOnly = true)
  public List<PrivateMessagePo> findConversation(Long accountId, Long contactAccountId) {
    SelectStatementProvider selectStatement =
        select(SELECT_COLUMNS)
            .from(PrivateMessageDynamicSqlSupport.privateMessages)
            .where(PrivateMessageDynamicSqlSupport.senderAccountId, isEqualTo(accountId))
            .and(PrivateMessageDynamicSqlSupport.recipientAccountId, isEqualTo(contactAccountId))
            .or(PrivateMessageDynamicSqlSupport.senderAccountId, isEqualTo(contactAccountId))
            .and(PrivateMessageDynamicSqlSupport.recipientAccountId, isEqualTo(accountId))
            .orderBy(PrivateMessageDynamicSqlSupport.createdAt)
            .build()
            .render(RenderingStrategies.MYBATIS3);

    return privateMessageMapper.selectMany(selectStatement);
  }

  @Transactional
  public void markConversationRead(Long accountId, Long contactAccountId, Instant readAt) {
    UpdateStatementProvider updateStatement =
        update(PrivateMessageDynamicSqlSupport.privateMessages)
            .set(PrivateMessageDynamicSqlSupport.isRead)
            .equalTo(true)
            .set(PrivateMessageDynamicSqlSupport.readAt)
            .equalTo(readAt)
            .where(PrivateMessageDynamicSqlSupport.recipientAccountId, isEqualTo(accountId))
            .and(PrivateMessageDynamicSqlSupport.senderAccountId, isEqualTo(contactAccountId))
            .and(PrivateMessageDynamicSqlSupport.isRead, isEqualTo(false))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    privateMessageMapper.update(updateStatement);
  }
}
