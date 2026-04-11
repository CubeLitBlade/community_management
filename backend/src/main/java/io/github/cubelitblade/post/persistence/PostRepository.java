package io.github.cubelitblade.post.persistence;

import static org.mybatis.dynamic.sql.SqlBuilder.*;

import io.github.cubelitblade.post.model.Post;
import io.github.cubelitblade.post.model.Status;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class PostRepository {

  private final PostMapper postMapper;

  @Transactional
  public void publishPost(Post post) {
    PostPo postPo = PostPo.of(post);

    InsertStatementProvider<PostPo> insertStatement =
        insert(postPo)
            .into(PostDynamicSqlSupport.posts)
            .map(PostDynamicSqlSupport.authorId)
            .toProperty("authorId")
            .map(PostDynamicSqlSupport.title)
            .toProperty("title")
            .map(PostDynamicSqlSupport.content)
            .toProperty("content")
            .map(PostDynamicSqlSupport.status)
            .toProperty("status")
            .map(PostDynamicSqlSupport.createdAt)
            .toProperty("createdAt")
            .map(PostDynamicSqlSupport.updatedAt)
            .toProperty("updatedAt")
            .build()
            .render(RenderingStrategies.MYBATIS3);

    postMapper.insert(insertStatement);
  }

  @Transactional(readOnly = true)
  public List<Post> getRecentPosts(int count, Long lastId) {

    SelectStatementProvider selectStatement =
        select(
                PostDynamicSqlSupport.id,
                PostDynamicSqlSupport.authorId,
                PostDynamicSqlSupport.title,
                PostDynamicSqlSupport.content,
                PostDynamicSqlSupport.status,
                PostDynamicSqlSupport.createdAt,
                PostDynamicSqlSupport.updatedAt)
            .from(PostDynamicSqlSupport.posts)
            .where(PostDynamicSqlSupport.status, isEqualTo(Status.NORMAL.getValue()))
            .and(PostDynamicSqlSupport.id, isLessThanWhenPresent(lastId))
            .orderBy(PostDynamicSqlSupport.id.descending())
            .limit(count)
            .build()
            .render(RenderingStrategies.MYBATIS3);

    return postMapper.selectMany(selectStatement).stream().map(PostPo::toPost).toList();
  }
}
