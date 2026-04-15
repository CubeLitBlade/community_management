package io.github.cubelitblade.post.application;

import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.common.time.TimeProvider;
import io.github.cubelitblade.post.dto.EditPostRequest;
import io.github.cubelitblade.post.dto.PostDetailView;
import io.github.cubelitblade.post.dto.PostReactionView;
import io.github.cubelitblade.post.dto.PublishPostRequest;
import io.github.cubelitblade.post.dto.RecentPostsResponse;
import io.github.cubelitblade.post.exception.PostForbiddenException;
import io.github.cubelitblade.post.exception.PostNotFoundException;
import io.github.cubelitblade.post.model.Post;
import io.github.cubelitblade.post.persistence.PostQueryRepository;
import io.github.cubelitblade.post.persistence.PostRepository;
import io.github.cubelitblade.post.persistence.query.PostWithAuthorVo;
import io.github.cubelitblade.reaction.model.TargetType;
import io.github.cubelitblade.reaction.persistence.ReactionQueryRepository;
import io.github.cubelitblade.reaction.persistence.query.ReactionCountVo;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final PostQueryRepository postQueryRepository;
  private final ReactionQueryRepository reactionQueryRepository;
  private final TimeProvider timeProvider;

  public Long publishPost(Long authorId, PublishPostRequest request) {
    Post post = Post.createPost(authorId, request.title(), request.content(), timeProvider.now());
    postRepository.publishPost(post);

    return post.getId();
  }

  public void archivePost(JwtAuthenticatedUser authenticatedUser, long postId) {
    Post post = postRepository.getPost(postId).orElseThrow(PostNotFoundException::notFound);

    switch (authenticatedUser.role()) {
      case USER -> {
        if (!post.getAuthorId().equals(authenticatedUser.accountId())) {
          throw PostForbiddenException.cannotArchive();
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
    Post post = postRepository.getPost(postId).orElseThrow(PostNotFoundException::notFound);

    if (!post.getAuthorId().equals(authenticatedUser.accountId())) {
      throw PostForbiddenException.cannotEdit();
    }

    post.edit(request.title(), request.content(), timeProvider.now());
    postRepository.updatePost(post);

    return post;
  }

  public RecentPostsResponse getRecentPosts(int count, Long lastId) {
    return getRecentPosts(null, count, lastId);
  }

  public RecentPostsResponse getRecentPosts(
      JwtAuthenticatedUser authenticatedUser, int count, Long lastId) {
    int fetchSize = count + 1; // Determine whether it has next

    List<PostWithAuthorVo> fetchedPosts = postQueryRepository.getRecentPosts(fetchSize, lastId);
    List<PostWithAuthorVo> visiblePosts = fetchedPosts.stream().limit(count).toList();
    List<Long> postIds = visiblePosts.stream().map(PostWithAuthorVo::id).toList();
    Map<Long, List<ReactionCountVo>> reactionsByPostId =
        reactionQueryRepository.selectReactionsByTargets(TargetType.POST, postIds);
    Map<Long, String> viewerReactionsByPostId =
        authenticatedUser == null
            ? Map.of()
            : reactionQueryRepository.findUserReactionsByTargets(
                authenticatedUser.accountId(), TargetType.POST, postIds);

    return new RecentPostsResponse(
        visiblePosts.stream()
            .map(post -> toPostDetailView(post, reactionsByPostId, viewerReactionsByPostId))
            .toList(),
        fetchedPosts.size() > count);
  }

  private PostDetailView toPostDetailView(
      PostWithAuthorVo postWithAuthorVo,
      Map<Long, List<ReactionCountVo>> reactionsByPostId,
      Map<Long, String> viewerReactionsByPostId) {
    List<PostReactionView> reactions =
        reactionsByPostId.getOrDefault(postWithAuthorVo.id(), List.of()).stream()
            .map(PostReactionView::from)
            .toList();
    String viewerReaction = viewerReactionsByPostId.get(postWithAuthorVo.id());

    return PostDetailView.from(postWithAuthorVo, reactions, viewerReaction);
  }
}
