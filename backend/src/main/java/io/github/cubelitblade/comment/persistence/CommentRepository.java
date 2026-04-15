package io.github.cubelitblade.comment.persistence;

import static org.mybatis.dynamic.sql.SqlBuilder.insert;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.select;
import static org.mybatis.dynamic.sql.SqlBuilder.update;

import io.github.cubelitblade.comment.model.Comment;
import io.github.cubelitblade.comment.model.Status;
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
public class CommentRepository {

  private final CommentMapper commentMapper;

  @Transactional
  public void createComment(Comment comment) {
    CommentPo commentPo = CommentPo.of(comment);

    InsertStatementProvider<CommentPo> insertStatement =
        insert(commentPo)
            .into(CommentDynamicSqlSupport.comments)
            .map(CommentDynamicSqlSupport.id)
            .toProperty("id")
            .map(CommentDynamicSqlSupport.targetType)
            .toProperty("targetType")
            .map(CommentDynamicSqlSupport.targetId)
            .toProperty("targetId")
            .map(CommentDynamicSqlSupport.accountId)
            .toProperty("accountId")
            .map(CommentDynamicSqlSupport.parentId)
            .toProperty("parentId")
            .map(CommentDynamicSqlSupport.content)
            .toProperty("content")
            .map(CommentDynamicSqlSupport.status)
            .toProperty("status")
            .map(CommentDynamicSqlSupport.createdAt)
            .toProperty("createdAt")
            .map(CommentDynamicSqlSupport.updatedAt)
            .toProperty("updatedAt")
            .build()
            .render(RenderingStrategies.MYBATIS3);

    commentMapper.insert(insertStatement);
  }

  @Transactional
  public void updateComment(Comment comment) {
    CommentPo commentPo = CommentPo.of(comment);

    UpdateStatementProvider updateStatement =
        update(CommentDynamicSqlSupport.comments)
            .set(CommentDynamicSqlSupport.content)
            .equalTo(commentPo::content)
            .set(CommentDynamicSqlSupport.status)
            .equalTo(commentPo::status)
            .set(CommentDynamicSqlSupport.updatedAt)
            .equalTo(commentPo::updatedAt)
            .where(CommentDynamicSqlSupport.id, isEqualTo(comment.getId()))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    commentMapper.update(updateStatement);
  }

  @Transactional(readOnly = true)
  public Optional<Comment> getComment(long id) {
    SelectStatementProvider selectStatement =
        select(
                CommentDynamicSqlSupport.id,
                CommentDynamicSqlSupport.targetType,
                CommentDynamicSqlSupport.targetId,
                CommentDynamicSqlSupport.accountId,
                CommentDynamicSqlSupport.parentId,
                CommentDynamicSqlSupport.content,
                CommentDynamicSqlSupport.status,
                CommentDynamicSqlSupport.createdAt,
                CommentDynamicSqlSupport.updatedAt)
            .from(CommentDynamicSqlSupport.comments)
            .where(CommentDynamicSqlSupport.id, isEqualTo(id))
            .and(CommentDynamicSqlSupport.status, isEqualTo(Status.NORMAL.getValue()))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    return commentMapper.selectOne(selectStatement).map(CommentPo::toComment);
  }
}
