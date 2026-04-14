package io.github.cubelitblade.common.exception;

import java.util.Arrays;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponse;

@Getter
public abstract class DomainException extends RuntimeException implements ErrorResponse {
  private final ApiErrorCode errorCode;

  protected DomainException(ApiErrorCode errorCode, String detail) {
    super(detail);
    this.errorCode = errorCode;
  }

  @Override
  public final @NonNull HttpStatusCode getStatusCode() {
    return errorCode.statusCode();
  }

  @Override
  public final @NonNull ProblemDetail getBody() {
    ProblemDetail problem = ProblemDetail.forStatus(getStatusCode());
    problem.setTitle(errorCode.getTitle());
    problem.setDetail(getMessage());
    problem.setProperty("code", errorCode.name());
    return problem;
  }

  protected static void ensureCategory(
      ApiErrorCode errorCode, ApiErrorCode.Category allowed, ApiErrorCode.Category... others) {
    if (errorCode.getCategory() == allowed) {
      return;
    }

    if (Arrays.stream(others).anyMatch(category -> category == errorCode.getCategory())) {
      return;
    }

    throw new IllegalArgumentException(
        "Error code " + errorCode.name() + " is not allowed for this exception type");
  }
}
