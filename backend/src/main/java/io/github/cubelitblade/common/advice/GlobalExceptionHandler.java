package io.github.cubelitblade.common.advice;

import io.github.cubelitblade.account.exception.*;
import io.github.cubelitblade.common.exception.DomainException;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(value = DomainException.class)
  public ResponseEntity<ProblemDetail> handleErrorResponse(DomainException e) {
    return ResponseEntity.status(e.getStatusCode()).body(e.getBody());
  }

  @ExceptionHandler(value = ResponseStatusException.class)
  public ResponseEntity<ProblemDetail> handleResponseStatusException(ResponseStatusException e) {
    ProblemDetail problem = ProblemDetail.forStatus(e.getStatusCode());

    problem.setTitle("Request failed");
    problem.setDetail(e.getReason());
    problem.setProperty("code", HttpStatus.valueOf(e.getStatusCode().value()).name());

    return ResponseEntity.status(e.getStatusCode()).body(problem);
  }

  @ExceptionHandler(value = LoginFailedExceptionLegacy.class)
  public ResponseEntity<ProblemDetail> handleLoginFailedException(LoginFailedExceptionLegacy e) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);

    problem.setTitle("Failed to login");
    problem.setDetail(e.getMessage());
    problem.setProperty("code", e.getErrorCode());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
  }

  @ExceptionHandler(value = ConflictFieldsExceptionLegacy.class)
  public ResponseEntity<ProblemDetail> handleConflictFieldsException(
      ConflictFieldsExceptionLegacy e) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);

    problem.setTitle("Fields already exist");
    problem.setDetail(e.getMessage());
    problem.setProperty("code", e.getErrorCode());

    return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
  }

  @ExceptionHandler(value = InputValidationExceptionLegacy.class)
  public ResponseEntity<ProblemDetail> handleInputValidationException(
      InputValidationExceptionLegacy e) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

    problem.setTitle("Invalid input");
    problem.setDetail(e.getMessage());
    problem.setProperty("code", e.getErrorCode());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
  }

  @ExceptionHandler(value = MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

    problem.setTitle("Method argument not valid");
    problem.setDetail(
        Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage());

    return ResponseEntity.badRequest().body(problem);
  }
}
