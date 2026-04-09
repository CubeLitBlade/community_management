package io.github.cubelitblade.post.persistence;

import io.github.cubelitblade.post.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
}
