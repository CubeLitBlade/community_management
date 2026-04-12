package io.github.cubelitblade.post.web;

import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.post.application.PostService;
import io.github.cubelitblade.post.dto.PublishPostRequest;
import io.github.cubelitblade.post.dto.RecentPostsResponse;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;

  @PostMapping
  public ResponseEntity<Void> publishPost(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser,
      @RequestBody PublishPostRequest request) {
    Long postId = postService.publishPost(authenticatedUser.accountId(), request);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(postId)
            .toUri();
    return ResponseEntity.created(location).build();
  }

  @GetMapping("/recent")
  public ResponseEntity<RecentPostsResponse> getRecentPosts(
      @RequestParam(defaultValue = "10") int count, @RequestParam(required = false) Long lastId) {
    if (count <= 0 || count > 50) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Count must be between 1 and 50");
    }

    return ResponseEntity.ok(postService.getRecentPosts(count, lastId));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePost(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser, @PathVariable Long id) {
    postService.archivePost(authenticatedUser, id);
    return ResponseEntity.noContent().build();
  }
}
