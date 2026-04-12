package io.github.cubelitblade.post.persistence;

import static org.mybatis.dynamic.sql.SqlBuilder.*;

import io.github.cubelitblade.post.model.Post;
import lombok.RequiredArgsConstructor;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
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
}
