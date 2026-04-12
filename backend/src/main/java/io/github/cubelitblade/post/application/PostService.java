package io.github.cubelitblade.post.application;

import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.common.time.TimeProvider;
import io.github.cubelitblade.post.dto.EditPostRequest;
import io.github.cubelitblade.post.dto.PostDetailView;
import io.github.cubelitblade.post.dto.PublishPostRequest;
import io.github.cubelitblade.post.dto.RecentPostsResponse;
import io.github.cubelitblade.post.exception.PostForbiddenException;
import io.github.cubelitblade.post.exception.PostNotFoundException;
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

  public void archivePost(JwtAuthenticatedUser authenticatedUser, long postId) {
    Post post =
        postRepository
            .getPost(postId)
            .orElseThrow(() -> new PostNotFoundException("Post not found"));

    switch (authenticatedUser.role()) {
      case USER -> {
        if (!post.getAuthorId().equals(authenticatedUser.accountId())) {
          throw new PostForbiddenException("You are not allowed to archive post");
        }
      }
      case ADMIN, OWNER -> {
        /* Admin can archive any post, so no additional check is needed */
      }
      default -> throw new IllegalStateException("Unexpected value: " + authenticatedUser.role());
    }

    post.archive(timeProvider.now());
    postRepository.updatePost(post);
  }

  public Post editPost(
      JwtAuthenticatedUser authenticatedUser, long postId, EditPostRequest request) {
    Post post =
        postRepository
            .getPost(postId)
            .orElseThrow(() -> new PostNotFoundException("Post not found"));

    if (!post.getAuthorId().equals(authenticatedUser.accountId())) {
      throw new PostForbiddenException("You are not allowed to edit post");
    }

    post.edit(request.title(), request.content(), timeProvider.now());
    postRepository.updatePost(post);

    return post;
  }

  public RecentPostsResponse getRecentPosts(int count, Long lastId) {
    int fetchSize = count + 1; // Determine whether it has next

    List<PostWithAuthorVo> fetchedPosts = postQueryRepository.getRecentPosts(fetchSize, lastId);

    return new RecentPostsResponse(
        fetchedPosts.stream().limit(count).map(PostDetailView::from).toList(),
        fetchedPosts.size() > count);
  }
}
