package io.github.cubelitblade.post.exception;

import io.github.cubelitblade.common.exception.ApiErrorCode;
import io.github.cubelitblade.common.exception.DomainException;

public class PostForbiddenException extends DomainException {
  private PostForbiddenException(String detail) {
    super(ApiErrorCode.POST_FORBIDDEN, detail);
  }

  public static PostForbiddenException cannotArchive() {
    return new PostForbiddenException("You are not allowed to archive post");
  }

  public static PostForbiddenException cannotEdit() {
    return new PostForbiddenException("You are not allowed to edit post");
  }
}
