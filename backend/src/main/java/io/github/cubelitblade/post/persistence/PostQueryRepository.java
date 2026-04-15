package io.github.cubelitblade.post.persistence;

import static org.mybatis.dynamic.sql.SqlBuilder.*;

import io.github.cubelitblade.account.persistence.AccountDynamicSqlSupport;
import io.github.cubelitblade.post.model.Status;
import io.github.cubelitblade.post.persistence.query.PostWithAuthorVo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class PostQueryRepository {

  private final PostQueryMapper postQueryMapper;

  @Transactional(readOnly = true)
  public List<PostWithAuthorVo> getRecentPosts(int count, Long lastId) {

    SelectStatementProvider selectStatement =
        select(
                PostDynamicSqlSupport.id,
                PostDynamicSqlSupport.authorId,
                PostDynamicSqlSupport.title,
                PostDynamicSqlSupport.content,
                PostDynamicSqlSupport.status,
                PostDynamicSqlSupport.createdAt,
                PostDynamicSqlSupport.updatedAt,
                AccountDynamicSqlSupport.username.as("username"),
                AccountDynamicSqlSupport.nickname.as("nickname"))
            .from(PostDynamicSqlSupport.posts)
            .leftJoin(AccountDynamicSqlSupport.accounts)
            .on(PostDynamicSqlSupport.authorId, isEqualTo(AccountDynamicSqlSupport.id))
            .where(PostDynamicSqlSupport.status, isEqualTo(Status.NORMAL.getValue()))
            .and(PostDynamicSqlSupport.id, isLessThanWhenPresent(lastId))
            .orderBy(PostDynamicSqlSupport.id.descending())
            .limit(count)
            .build()
            .render(RenderingStrategies.MYBATIS3);

    return postQueryMapper.selectPostsWith(selectStatement);
  }

  @Transactional(readOnly = true)
  public PostWithAuthorVo getPostById(Long id) {
    SelectStatementProvider selectStatement =
        select(
                PostDynamicSqlSupport.id,
                PostDynamicSqlSupport.authorId,
                PostDynamicSqlSupport.title,
                PostDynamicSqlSupport.content,
                PostDynamicSqlSupport.status,
                PostDynamicSqlSupport.createdAt,
                PostDynamicSqlSupport.updatedAt,
                AccountDynamicSqlSupport.username.as("username"),
                AccountDynamicSqlSupport.nickname.as("nickname"))
            .from(PostDynamicSqlSupport.posts)
            .leftJoin(AccountDynamicSqlSupport.accounts)
            .on(PostDynamicSqlSupport.authorId, isEqualTo(AccountDynamicSqlSupport.id))
            .where(PostDynamicSqlSupport.id, isEqualTo(id))
            .and(PostDynamicSqlSupport.status, isEqualTo(Status.NORMAL.getValue()))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    return postQueryMapper.selectPostWithAuthor(selectStatement).orElse(null);
  }
}
