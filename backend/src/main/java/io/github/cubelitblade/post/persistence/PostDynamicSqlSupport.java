package io.github.cubelitblade.post.persistence;

import java.sql.JDBCType;
import java.time.Instant;
import org.mybatis.dynamic.sql.AliasableSqlTable;
import org.mybatis.dynamic.sql.SqlColumn;

public final class PostDynamicSqlSupport {
  public static final Posts posts = new Posts();
  public static final SqlColumn<Long> id = posts.id;
  public static final SqlColumn<Long> authorId = posts.authorId;
  public static final SqlColumn<String> title = posts.title;
  public static final SqlColumn<String> content = posts.content;
  public static final SqlColumn<String> status = posts.status;
  public static final SqlColumn<Instant> createdAt = posts.createdAt;
  public static final SqlColumn<Instant> updatedAt = posts.updatedAt;

  public static final class Posts extends AliasableSqlTable<Posts> {
    public final SqlColumn<Long> id = column("id", JDBCType.BIGINT);
    public final SqlColumn<Long> authorId = column("author_id", JDBCType.BIGINT);
    public final SqlColumn<String> title = column("title", JDBCType.VARCHAR);
    public final SqlColumn<String> content = column("content", JDBCType.VARCHAR);
    public final SqlColumn<String> status = column("status", JDBCType.VARCHAR);
    public final SqlColumn<Instant> createdAt =
        column("created_at", JDBCType.TIMESTAMP_WITH_TIMEZONE);
    public final SqlColumn<Instant> updatedAt =
        column("updated_at", JDBCType.TIMESTAMP_WITH_TIMEZONE);

    Posts() {
      super("posts", Posts::new);
    }
  }
}
