package io.github.cubelitblade.post.application;

import io.github.cubelitblade.common.time.TimeProvider;
import io.github.cubelitblade.post.dto.PostResponse;
import io.github.cubelitblade.post.dto.PublishPostRequest;
import io.github.cubelitblade.post.dto.RecentPostsResponse;
import io.github.cubelitblade.post.model.Post;
import io.github.cubelitblade.post.persistence.PostQueryRepository;
import io.github.cubelitblade.post.persistence.PostRepository;
import io.github.cubelitblade.post.persistence.query.PostWithAuthorVo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final PostQueryRepository postQueryRepository;
  private final TimeProvider timeProvider;

  public Long publishPost(Long authorId, PublishPostRequest request) {
    Post post = Post.createPost(authorId, request.title(), request.content(), timeProvider.now());
    postRepository.publishPost(post);

    return post.getId();
  }

  public RecentPostsResponse getRecentPosts(int count, Long lastId) {
    int fetchSize = count + 1; // Determine whether it has next

    List<PostWithAuthorVo> fetchedPosts = postQueryRepository.getRecentPosts(fetchSize, lastId);

    return new RecentPostsResponse(
        fetchedPosts.stream().limit(count).map(PostResponse::from).toList(),
        fetchedPosts.size() > count);
  }
}
