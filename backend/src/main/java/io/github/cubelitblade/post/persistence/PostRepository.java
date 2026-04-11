package io.github.cubelitblade.post.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.cubelitblade.post.model.Post;
import io.github.cubelitblade.post.model.Status;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class PostRepository {

  private final PostMapper postMapper;

  @Transactional
  public void publishPost(Post post) {
    postMapper.insert(PostPo.of(post));
  }

  @Transactional(readOnly = true)
  public List<Post> getRecentPosts(int count, Long lastId) {
    LambdaQueryWrapper<PostPo> query = new LambdaQueryWrapper<>();

    if (lastId != null) {
      query.lt(PostPo::getId, lastId);
    }

    query
        .orderByDesc(PostPo::getId)
        .eq(PostPo::getStatus, Status.NORMAL.getValue())
        .last("limit " + count);

    return postMapper.selectList(query).stream().map(PostPo::toPost).toList();
  }
}
