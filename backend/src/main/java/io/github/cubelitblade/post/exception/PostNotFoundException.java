package io.github.cubelitblade.post.exception;

import io.github.cubelitblade.common.exception.DomainException;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponse;

public class PostNotFoundException extends DomainException {
  public PostNotFoundException(String message) {
    super(message);
  }

  @Override
  public @NonNull HttpStatusCode getStatusCode() {
    return HttpStatus.NOT_FOUND;
  }

  @Override
  public @NonNull ProblemDetail getBody() {
    ProblemDetail problem = ProblemDetail.forStatus(getStatusCode());

    problem.setTitle("Post not found");
    problem.setDetail(getMessage());
    problem.setProperty("code", "POST_NOT_FOUND");

    return problem;
  }
}
