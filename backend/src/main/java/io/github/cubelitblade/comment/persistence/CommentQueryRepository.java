package io.github.cubelitblade.comment.persistence;

import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.isIn;
import static org.mybatis.dynamic.sql.SqlBuilder.select;

import io.github.cubelitblade.account.persistence.AccountDynamicSqlSupport;
import io.github.cubelitblade.comment.model.Status;
import io.github.cubelitblade.comment.model.TargetType;
import io.github.cubelitblade.comment.persistence.query.CommentWithAuthorVo;
import java.sql.JDBCType;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class CommentQueryRepository {

  private static final CommentDynamicSqlSupport.Comments parentComments =
      CommentDynamicSqlSupport.comments.withAlias("parent_comments");
  private static final AccountDynamicSqlSupport.Accounts authors =
      AccountDynamicSqlSupport.accounts.withAlias("authors");
  private static final AccountDynamicSqlSupport.Accounts replyToAuthors =
      AccountDynamicSqlSupport.accounts.withAlias("reply_to_authors");

  private static final SqlColumn<Long> parentAccountId =
      parentComments.column("account_id", JDBCType.BIGINT);

  private final CommentQueryMapper commentQueryMapper;

  @Transactional(readOnly = true)
  public List<CommentWithAuthorVo> getCommentsByTarget(TargetType targetType, Long targetId) {
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
                CommentDynamicSqlSupport.updatedAt,
                authors.username.as("username"),
                authors.nickname.as("nickname"),
                parentAccountId.as("reply_to_account_id"),
                replyToAuthors.username.as("reply_to_username"),
                replyToAuthors.nickname.as("reply_to_nickname"))
            .from(CommentDynamicSqlSupport.comments)
            .leftJoin(authors)
            .on(CommentDynamicSqlSupport.accountId, isEqualTo(authors.id))
            .leftJoin(parentComments)
            .on(CommentDynamicSqlSupport.parentId, isEqualTo(parentComments.id))
            .leftJoin(replyToAuthors)
            .on(parentAccountId, isEqualTo(replyToAuthors.id))
            .where(CommentDynamicSqlSupport.targetType, isEqualTo(targetType.getValue()))
            .and(CommentDynamicSqlSupport.targetId, isEqualTo(targetId))
            .and(CommentDynamicSqlSupport.status, isEqualTo(Status.NORMAL.getValue()))
            .orderBy(CommentDynamicSqlSupport.id)
            .build()
            .render(RenderingStrategies.MYBATIS3);

    return commentQueryMapper.selectCommentsWith(selectStatement);
  }

  @Transactional(readOnly = true)
  public List<CommentWithAuthorVo> getCommentsByIds(List<Long> commentIds) {
    if (commentIds.isEmpty()) {
      return Collections.emptyList();
    }

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
                CommentDynamicSqlSupport.updatedAt,
                authors.username.as("username"),
                authors.nickname.as("nickname"),
                parentAccountId.as("reply_to_account_id"),
                replyToAuthors.username.as("reply_to_username"),
                replyToAuthors.nickname.as("reply_to_nickname"))
            .from(CommentDynamicSqlSupport.comments)
            .leftJoin(authors)
            .on(CommentDynamicSqlSupport.accountId, isEqualTo(authors.id))
            .leftJoin(parentComments)
            .on(CommentDynamicSqlSupport.parentId, isEqualTo(parentComments.id))
            .leftJoin(replyToAuthors)
            .on(parentAccountId, isEqualTo(replyToAuthors.id))
            .where(CommentDynamicSqlSupport.id, isIn(commentIds))
            .and(CommentDynamicSqlSupport.status, isEqualTo(Status.NORMAL.getValue()))
            .orderBy(CommentDynamicSqlSupport.id)
            .build()
            .render(RenderingStrategies.MYBATIS3);

    return commentQueryMapper.selectCommentsWith(selectStatement);
  }
}
