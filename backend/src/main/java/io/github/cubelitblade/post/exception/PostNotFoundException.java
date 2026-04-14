package io.github.cubelitblade.post.exception;

import io.github.cubelitblade.common.exception.ApiErrorCode;
import io.github.cubelitblade.common.exception.DomainException;

public class PostNotFoundException extends DomainException {
  private PostNotFoundException(String detail) {
    super(ApiErrorCode.POST_NOT_FOUND, detail);
  }

  public static PostNotFoundException notFound() {
    return new PostNotFoundException("Post not found");
  }
}
