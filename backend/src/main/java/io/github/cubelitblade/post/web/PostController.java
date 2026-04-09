package io.github.cubelitblade.post.web;

import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.post.application.PostService;
import io.github.cubelitblade.post.dto.PublishPostRequest;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
