package io.github.cubelitblade.common.advice;

import io.github.cubelitblade.common.exception.ApiErrorCode;
import io.github.cubelitblade.common.exception.DomainException;
import java.util.Objects;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(value = DomainException.class)
  public ResponseEntity<ProblemDetail> handleErrorResponse(DomainException e) {
    return ResponseEntity.status(e.getStatusCode()).body(e.getBody());
  }

  @ExceptionHandler(value = MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e) {
    ProblemDetail problem = ProblemDetail.forStatus(ApiErrorCode.INVALID_REQUEST.statusCode());
    problem.setTitle(ApiErrorCode.INVALID_REQUEST.getTitle());
    problem.setDetail(
        Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage());
    problem.setProperty("code", ApiErrorCode.INVALID_REQUEST.name());

    return ResponseEntity.status(ApiErrorCode.INVALID_REQUEST.statusCode()).body(problem);
  }
}
