package io.github.cubelitblade.post.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cubelitblade.post.model.Post;
import io.github.cubelitblade.post.model.Status;
import io.github.cubelitblade.post.persistence.query.PostWithAuthorVo;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class PostPersistenceIntegrationTest {
  private static final Instant NOW = Instant.parse("2026-04-20T08:00:00Z");

  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private PostRepository postRepository;
  @Autowired private PostQueryRepository postQueryRepository;

  @BeforeEach
  void setUp() {
    insertAccount(9001L, "post_author", "Post Author");
    insertAccount(9002L, "other_author", "Other Author");
  }

  @Test
  @DisplayName("PostRepository: should publish and read normal post")
  void should_publish_and_read_normal_post() {
    Post post = Post.createPost(9101L, 9001L, "Hello", "Content", NOW);

    postRepository.publishPost(post);

    assertThat(postRepository.getPost(9101L))
        .hasValueSatisfying(
            persisted -> {
              assertThat(persisted.getAuthorId()).isEqualTo(9001L);
              assertThat(persisted.getTitle()).isEqualTo("Hello");
              assertThat(persisted.getContent()).isEqualTo("Content");
              assertThat(persisted.getStatus()).isEqualTo(Status.NORMAL);
              assertThat(persisted.getCreatedAt()).isEqualTo(NOW);
            });
  }

  @Test
  @DisplayName("PostRepository: should update post and hide archived post from getPost")
  void should_update_post_and_hide_archived_post() {
    Post post = Post.createPost(9102L, 9001L, "Old", "Old content", NOW);
    postRepository.publishPost(post);
    post.edit("New", "New content", NOW.plusSeconds(60));
    postRepository.updatePost(post);

    assertThat(postRepository.getPost(9102L))
        .hasValueSatisfying(
            persisted -> {
              assertThat(persisted.getTitle()).isEqualTo("New");
              assertThat(persisted.getContent()).isEqualTo("New content");
              assertThat(persisted.getUpdatedAt()).isEqualTo(NOW.plusSeconds(60));
            });

    post.archive(NOW.plusSeconds(120));
    postRepository.updatePost(post);

    assertThat(postRepository.getPost(9102L)).isEmpty();
  }

  @Test
  @DisplayName(
      "PostQueryRepository: should return recent normal posts by id descending with cursor")
  void should_return_recent_normal_posts_by_id_descending_with_cursor() {
    insertPost(9101L, 9001L, "First", "First content", "normal", NOW.minusSeconds(30), null);
    insertPost(
        9102L, 9002L, "Archived", "Archived content", "archived", NOW.minusSeconds(20), null);
    insertPost(9103L, 9002L, "Second", "Second content", "normal", NOW.minusSeconds(10), null);
    insertPost(9104L, 9001L, "Third", "Third content", "normal", NOW, null);

    List<PostWithAuthorVo> firstPage = postQueryRepository.getRecentPosts(2, 9200L);
    List<PostWithAuthorVo> secondPage = postQueryRepository.getRecentPosts(1, 9103L);

    assertThat(firstPage).extracting(PostWithAuthorVo::id).containsExactly(9104L, 9103L);
    assertThat(firstPage).extracting(PostWithAuthorVo::status).containsOnly("normal");
    assertThat(secondPage).extracting(PostWithAuthorVo::id).containsExactly(9101L);
  }

  @Test
  @DisplayName("PostQueryRepository: should join author for post detail and hide archived post")
  void should_join_author_for_post_detail_and_hide_archived_post() {
    insertPost(9105L, 9001L, "Visible", "Visible content", "normal", NOW, null);
    insertPost(9106L, 9002L, "Hidden", "Hidden content", "archived", NOW, null);

    PostWithAuthorVo visible = postQueryRepository.getPostById(9105L);
    PostWithAuthorVo hidden = postQueryRepository.getPostById(9106L);

    assertThat(visible.id()).isEqualTo(9105L);
    assertThat(visible.username()).isEqualTo("post_author");
    assertThat(visible.nickname()).isEqualTo("Post Author");
    assertThat(hidden).isNull();
  }

  private void insertAccount(Long id, String username, String nickname) {
    jdbcTemplate.update(
        """
        insert into accounts(id, username, nickname, password_hash, status, role, created_at, updated_at)
        values (?, ?, ?, 'hash', 'normal', 'user', ?, ?)
        """,
        id,
        username,
        nickname,
        ts(NOW),
        ts(NOW));
  }

  private void insertPost(
      Long id,
      Long authorId,
      String title,
      String content,
      String status,
      Instant createdAt,
      Instant updatedAt) {
    jdbcTemplate.update(
        """
        insert into posts(id, author_id, title, content, status, created_at, updated_at)
        values (?, ?, ?, ?, ?, ?, ?)
        """,
        id,
        authorId,
        title,
        content,
        status,
        ts(createdAt),
        ts(updatedAt));
  }

  private Timestamp ts(Instant instant) {
    return instant == null ? null : Timestamp.from(instant);
  }
}
