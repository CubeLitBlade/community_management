package io.github.cubelitblade.post.application;

import io.github.cubelitblade.common.time.TimeProvider;
import io.github.cubelitblade.post.dto.PublishPostRequest;
import io.github.cubelitblade.post.model.Post;
import io.github.cubelitblade.post.persistence.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final TimeProvider timeProvider;

  public Long publishPost(Long authorId, PublishPostRequest request) {
    Post post = Post.createPost(authorId, request.title(), request.content(), timeProvider.now());
    postRepository.publishPost(post);

    return post.getId();
  }
}
