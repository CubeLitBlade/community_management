package io.github.cubelitblade.comment.web;

import io.github.cubelitblade.account.security.JwtAuthenticatedUser;
import io.github.cubelitblade.comment.application.CommentService;
import io.github.cubelitblade.comment.dto.CommentListResponse;
import io.github.cubelitblade.comment.dto.CreateCommentRequest;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;

  @PostMapping
  public ResponseEntity<Void> createComment(
      @AuthenticationPrincipal JwtAuthenticatedUser authenticatedUser,
      @RequestBody CreateCommentRequest request) {
    Long commentId = commentService.createComment(authenticatedUser, request);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(commentId)
            .toUri();

    return ResponseEntity.created(location).build();
  }

  @GetMapping
  public ResponseEntity<CommentListResponse> getComments(
      @RequestParam String targetType, @RequestParam Long targetId) {
    return ResponseEntity.ok(commentService.getComments(targetType, targetId));
  }
}
