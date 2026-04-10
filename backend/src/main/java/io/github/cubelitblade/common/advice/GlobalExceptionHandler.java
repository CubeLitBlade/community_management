package io.github.cubelitblade.common.advice;

import io.github.cubelitblade.account.exception.*;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(value = LoginFailedException.class)
  public ResponseEntity<ProblemDetail> handleLoginFailedException(LoginFailedException e) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);

    problem.setTitle("Failed to login");
    problem.setDetail(e.getMessage());
    problem.setProperty("code", e.getErrorCode());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
  }

  @ExceptionHandler(value = ConflictFieldsException.class)
  public ResponseEntity<ProblemDetail> handleConflictFieldsException(ConflictFieldsException e) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);

    problem.setTitle("Fields already exist");
    problem.setDetail(e.getMessage());
    problem.setProperty("code", e.getErrorCode());

    return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
  }

  @ExceptionHandler(value = InputValidationException.class)
  public ResponseEntity<ProblemDetail> handleInputValidationException(InputValidationException e) {
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
