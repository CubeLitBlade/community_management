package io.github.cubelitblade.post.persistence;

import static org.mybatis.dynamic.sql.SqlBuilder.*;

import io.github.cubelitblade.post.model.Post;
import io.github.cubelitblade.post.model.Status;
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

  @Transactional
  public void updatePost(Post post) {
    PostPo postPo = PostPo.of(post);

    UpdateStatementProvider updateStatement =
        update(PostDynamicSqlSupport.posts)
            .set(PostDynamicSqlSupport.title)
            .equalTo(postPo::title)
            .set(PostDynamicSqlSupport.content)
            .equalTo(postPo::content)
            .set(PostDynamicSqlSupport.status)
            .equalTo(postPo::status)
            .set(PostDynamicSqlSupport.updatedAt)
            .equalTo(postPo::updatedAt)
            .where(PostDynamicSqlSupport.id, isEqualTo(post.getId()))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    postMapper.update(updateStatement);
  }

  @Transactional(readOnly = true)
  public Optional<Post> getPost(long id) {
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
            .where(PostDynamicSqlSupport.id, isEqualTo(id))
            .and(PostDynamicSqlSupport.status, isEqualTo(Status.NORMAL.getValue()))
            .build()
            .render(RenderingStrategies.MYBATIS3);

    Optional<PostPo> postPo = postMapper.selectOne(selectStatement);

    return postPo.map(PostPo::toPost);
  }
}
