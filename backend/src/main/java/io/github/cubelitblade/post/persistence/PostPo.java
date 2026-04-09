package io.github.cubelitblade.post.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.cubelitblade.post.model.Post;
import io.github.cubelitblade.post.model.Status;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("posts")
public class PostPo {
  @TableId(type = IdType.AUTO)
  private Long id;

  private Long authorId;
  private String title;
  private String content;
  private String status;
  private Instant createdAt;
  private Instant updatedAt;

  public static PostPo of(Post post) {
    if (post == null) {
      return null;
    }

    return PostPo.builder()
        .id(post.getId())
        .authorId(post.getAuthorId())
        .title(post.getTitle())
        .content(post.getContent())
        .status(post.getStatus().getValue())
        .createdAt(post.getCreatedAt())
        .updatedAt(post.getUpdatedAt())
        .build();
  }

  public Post toPost() {
    Post.Snapshot snapshot =
        Post.Snapshot.builder()
            .id(this.id)
            .authorId(this.authorId)
            .title(this.title)
            .content(this.content)
            .status(Status.from(this.status))
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();

    return Post.reconstitute(snapshot);
  }
}
