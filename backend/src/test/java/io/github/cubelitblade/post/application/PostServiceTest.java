package io.github.cubelitblade.post.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.github.cubelitblade.account.model.Role;
import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.common.id.SnowflakeIdGenerator;
import io.github.cubelitblade.common.time.TimeProvider;
import io.github.cubelitblade.post.dto.EditPostRequest;
import io.github.cubelitblade.post.dto.PublishPostRequest;
import io.github.cubelitblade.post.exception.PostForbiddenException;
import io.github.cubelitblade.post.exception.PostNotFoundException;
import io.github.cubelitblade.post.model.Post;
import io.github.cubelitblade.post.model.Status;
import io.github.cubelitblade.post.persistence.PostQueryRepository;
import io.github.cubelitblade.post.persistence.PostRepository;
import io.github.cubelitblade.post.persistence.query.PostWithAuthorVo;
import io.github.cubelitblade.reaction.model.TargetType;
import io.github.cubelitblade.reaction.persistence.ReactionQueryRepository;
import io.github.cubelitblade.reaction.persistence.query.ReactionCountVo;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
  private static final Instant NOW = Instant.parse("2026-04-20T08:00:00Z");

  @Mock private PostRepository postRepository;
  @Mock private PostQueryRepository postQueryRepository;
  @Mock private ReactionQueryRepository reactionQueryRepository;
  @Mock private SnowflakeIdGenerator idGenerator;
  @Mock private TimeProvider timeProvider;

  private PostService postService;

  @BeforeEach
  void setUp() {
    postService =
        new PostService(
            postRepository,
            postQueryRepository,
            reactionQueryRepository,
            idGenerator,
            timeProvider);
  }

  @Test
  @DisplayName("Publish: should persist post with generated id and current time")
  void should_publish_post() {
    given(idGenerator.nextId()).willReturn(99L);
    given(timeProvider.now()).willReturn(NOW);

    Long postId = postService.publishPost(7L, new PublishPostRequest("Title", "Content"));

    ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
    verify(postRepository).publishPost(captor.capture());
    assertThat(postId).isEqualTo(99L);
    assertThat(captor.getValue().getAuthorId()).isEqualTo(7L);
    assertThat(captor.getValue().getStatus()).isEqualTo(Status.NORMAL);
  }

  @Test
  @DisplayName("Archive: should reject normal user archiving another user's post")
  void should_reject_user_archiving_foreign_post() {
    given(postRepository.getPost(101L)).willReturn(Optional.of(post(101L, 7L)));

    assertThatThrownBy(() -> postService.archivePost(new JwtAuthenticatedUser(9L, Role.USER), 101L))
        .isInstanceOf(PostForbiddenException.class);

    verify(postRepository, never()).updatePost(any());
  }

  @Test
  @DisplayName("Archive: should allow admin to archive any post")
  void should_allow_admin_to_archive_post() {
    Post post = post(101L, 7L);
    given(postRepository.getPost(101L)).willReturn(Optional.of(post));
    given(timeProvider.now()).willReturn(NOW);

    postService.archivePost(new JwtAuthenticatedUser(2L, Role.ADMIN), 101L);

    verify(postRepository).updatePost(post);
    assertThat(post.getStatus()).isEqualTo(Status.ARCHIVED);
  }

  @Test
  @DisplayName("Archive: should allow author and owner to archive post")
  void should_allow_author_and_owner_to_archive_post() {
    Post authorPost = post(102L, 7L);
    Post ownerPost = post(103L, 8L);
    given(postRepository.getPost(102L)).willReturn(Optional.of(authorPost));
    given(postRepository.getPost(103L)).willReturn(Optional.of(ownerPost));
    given(timeProvider.now()).willReturn(NOW);

    postService.archivePost(new JwtAuthenticatedUser(7L, Role.USER), 102L);
    postService.archivePost(new JwtAuthenticatedUser(2L, Role.OWNER), 103L);

    verify(postRepository).updatePost(authorPost);
    verify(postRepository).updatePost(ownerPost);
    assertThat(authorPost.getStatus()).isEqualTo(Status.ARCHIVED);
    assertThat(ownerPost.getStatus()).isEqualTo(Status.ARCHIVED);
  }

  @Test
  @DisplayName("Archive: should reject missing post")
  void should_reject_archiving_missing_post() {
    given(postRepository.getPost(404L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> postService.archivePost(new JwtAuthenticatedUser(7L, Role.USER), 404L))
        .isInstanceOf(PostNotFoundException.class);

    verify(postRepository, never()).updatePost(any());
  }

  @Test
  @DisplayName("Edit: should reject non-author")
  void should_reject_non_author_editing_post() {
    given(postRepository.getPost(101L)).willReturn(Optional.of(post(101L, 7L)));

    assertThatThrownBy(
            () ->
                postService.editPost(
                    new JwtAuthenticatedUser(9L, Role.USER),
                    101L,
                    new EditPostRequest("New", "Content")))
        .isInstanceOf(PostForbiddenException.class);

    verify(postRepository, never()).updatePost(any());
  }

  @Test
  @DisplayName("Edit: should update author post and preserve blank fields")
  void should_edit_author_post() {
    Post post = post(101L, 7L);
    given(postRepository.getPost(101L)).willReturn(Optional.of(post));
    given(timeProvider.now()).willReturn(NOW);

    Post result =
        postService.editPost(
            new JwtAuthenticatedUser(7L, Role.USER), 101L, new EditPostRequest("New title", " "));

    verify(postRepository).updatePost(post);
    assertThat(result).isSameAs(post);
    assertThat(post.getTitle()).isEqualTo("New title");
    assertThat(post.getContent()).isEqualTo("Content");
    assertThat(post.getUpdatedAt()).isEqualTo(NOW);
  }

  @Test
  @DisplayName("Edit: should reject missing post")
  void should_reject_editing_missing_post() {
    given(postRepository.getPost(404L)).willReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                postService.editPost(
                    new JwtAuthenticatedUser(7L, Role.USER),
                    404L,
                    new EditPostRequest("New", "Content")))
        .isInstanceOf(PostNotFoundException.class);

    verify(postRepository, never()).updatePost(any());
  }

  @Test
  @DisplayName("Recent posts: should assemble reaction counts and current viewer reaction")
  void should_assemble_recent_posts_with_reactions() {
    PostWithAuthorVo first = postView(101L, 7L, "First");
    PostWithAuthorVo second = postView(100L, 8L, "Second");
    given(postQueryRepository.getRecentPosts(3, null)).willReturn(List.of(first, second));
    given(reactionQueryRepository.selectReactionsByTargets(TargetType.POST, List.of(101L, 100L)))
        .willReturn(Map.of(101L, List.of(new ReactionCountVo("like", 2L))));
    given(
            reactionQueryRepository.findUserReactionsByTargets(
                9L, TargetType.POST, List.of(101L, 100L)))
        .willReturn(Map.of(101L, "like"));

    var response = postService.getRecentPosts(new JwtAuthenticatedUser(9L, Role.USER), 2, null);

    assertThat(response.hasMore()).isFalse();
    assertThat(response.items()).hasSize(2);
    assertThat(response.items().getFirst().id()).isEqualTo(101L);
    assertThat(response.items().getFirst().reactions())
        .extracting("reactionType", "count")
        .containsExactly(org.assertj.core.groups.Tuple.tuple("like", 2L));
    assertThat(response.items().getFirst().viewerReaction()).isEqualTo("like");
    assertThat(response.items().get(1).reactions()).isEmpty();
    verify(reactionQueryRepository).selectReactionsByTargets(TargetType.POST, List.of(101L, 100L));
  }

  @Test
  @DisplayName("Recent posts: should support anonymous viewer and hasMore")
  void should_assemble_recent_posts_for_anonymous_viewer_with_has_more() {
    PostWithAuthorVo first = postView(101L, 7L, "First");
    PostWithAuthorVo second = postView(100L, 8L, "Second");
    given(postQueryRepository.getRecentPosts(2, 101L)).willReturn(List.of(first, second));
    given(reactionQueryRepository.selectReactionsByTargets(TargetType.POST, List.of(101L)))
        .willReturn(Map.of());

    var response = postService.getRecentPosts(1, 101L);

    assertThat(response.hasMore()).isTrue();
    assertThat(response.items()).extracting("id").containsExactly(101L);
    assertThat(response.items().getFirst().viewerReaction()).isNull();
    verify(reactionQueryRepository, never()).findUserReactionsByTargets(any(), any(), any());
  }

  @Test
  @DisplayName("Recent posts: should handle empty page")
  void should_handle_empty_recent_posts() {
    given(postQueryRepository.getRecentPosts(6, null)).willReturn(List.of());
    given(reactionQueryRepository.selectReactionsByTargets(TargetType.POST, List.of()))
        .willReturn(Map.of());

    var response = postService.getRecentPosts(null, 5, null);

    assertThat(response.items()).isEmpty();
    assertThat(response.hasMore()).isFalse();
    verify(reactionQueryRepository, never()).findUserReactionsByTargets(any(), any(), any());
  }

  @Test
  @DisplayName("Post detail: should assemble anonymous detail")
  void should_assemble_post_detail_for_anonymous_viewer() {
    PostWithAuthorVo post = postView(101L, 7L, "First");
    given(postQueryRepository.getPostById(101L)).willReturn(post);
    given(reactionQueryRepository.selectReactionsByTargets(TargetType.POST, List.of(101L)))
        .willReturn(Map.of(101L, List.of(new ReactionCountVo("like", 2L))));

    var response = postService.getPostDetail(null, 101L);

    assertThat(response.id()).isEqualTo(101L);
    assertThat(response.reactions())
        .extracting("reactionType", "count")
        .containsExactly(org.assertj.core.groups.Tuple.tuple("like", 2L));
    assertThat(response.viewerReaction()).isNull();
    verify(reactionQueryRepository, never()).findUserReactionsByTargets(any(), any(), any());
  }

  @Test
  @DisplayName("Post detail: should include current viewer reaction")
  void should_assemble_post_detail_with_viewer_reaction() {
    PostWithAuthorVo post = postView(101L, 7L, "First");
    given(postQueryRepository.getPostById(101L)).willReturn(post);
    given(reactionQueryRepository.selectReactionsByTargets(TargetType.POST, List.of(101L)))
        .willReturn(Map.of());
    given(reactionQueryRepository.findUserReactionsByTargets(9L, TargetType.POST, List.of(101L)))
        .willReturn(Map.of(101L, "like"));

    var response = postService.getPostDetail(new JwtAuthenticatedUser(9L, Role.USER), 101L);

    assertThat(response.id()).isEqualTo(101L);
    assertThat(response.viewerReaction()).isEqualTo("like");
  }

  @Test
  @DisplayName("Post detail: should reject missing post")
  void should_reject_missing_post_detail() {
    given(postQueryRepository.getPostById(404L)).willReturn(null);

    assertThatThrownBy(() -> postService.getPostDetail(null, 404L))
        .isInstanceOf(PostNotFoundException.class);

    verify(reactionQueryRepository, never()).selectReactionsByTargets(any(), any());
  }

  private Post post(Long id, Long authorId) {
    return Post.reconstitute(
        Post.Snapshot.builder()
            .id(id)
            .authorId(authorId)
            .title("Title")
            .content("Content")
            .status(Status.NORMAL)
            .createdAt(NOW.minusSeconds(3600))
            .updatedAt(NOW.minusSeconds(1800))
            .build());
  }

  private PostWithAuthorVo postView(Long id, Long authorId, String title) {
    return new PostWithAuthorVo(
        id,
        authorId,
        title,
        "Content",
        Status.NORMAL.getValue(),
        NOW.minusSeconds(id),
        NOW.minusSeconds(id - 1),
        "user" + authorId,
        "User " + authorId);
  }
}
