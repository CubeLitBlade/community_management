package io.github.cubelitblade.post.exception;

import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponse;

public class PostForbiddenException extends RuntimeException implements ErrorResponse {
  public PostForbiddenException(String message) {
    super(message);
  }

  @Override
  public @NonNull HttpStatusCode getStatusCode() {
    return HttpStatus.FORBIDDEN;
  }

  @Override
  public @NonNull ProblemDetail getBody() {
    ProblemDetail problem = ProblemDetail.forStatus(getStatusCode());

    problem.setTitle("Not allowed to access");
    problem.setDetail(getMessage());
    problem.setProperty("code", "POST_FORBIDDEN");

    return problem;
  }
}
