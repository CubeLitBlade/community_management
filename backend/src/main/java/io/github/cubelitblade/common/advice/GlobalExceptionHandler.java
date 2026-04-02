package io.github.cubelitblade.common.advice;

import io.github.cubelitblade.common.exception.InvalidParameterException;
import io.github.cubelitblade.user.domain.exception.AccountArchivedException;
import io.github.cubelitblade.user.domain.exception.AccountSuspendedException;
import io.github.cubelitblade.user.domain.exception.InvalidCredentialsException;
import io.github.cubelitblade.user.domain.exception.UsernameAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = InvalidParameterException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(InvalidParameterException e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        if (e.getMessage() != null && !e.getMessage().isEmpty()) {
            problem.setTitle(e.getMessage());
        }

        if (e.getDetail() != null && !e.getDetail().isEmpty()) {
            problem.setDetail(e.getDetail());
        }

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(value = AccountArchivedException.class)
    public ResponseEntity<ProblemDetail> handleAccountArchived(AccountArchivedException e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);

        problem.setTitle("Account archived");
        problem.setDetail(e.getMessage());
        problem.setProperty("code", AccountArchivedException.ERROR_CODE);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(value = AccountSuspendedException.class)
    public ResponseEntity<ProblemDetail> handleAccountSuspended(AccountSuspendedException e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);

        problem.setTitle("Account suspended");
        problem.setDetail(e.getMessage());
        problem.setProperty("code", AccountSuspendedException.ERROR_CODE);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(value = InvalidCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleInvalidCredentials(InvalidCredentialsException e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);

        problem.setTitle("Invalid credentials");
        problem.setDetail(e.getMessage());
        problem.setProperty("code", InvalidCredentialsException.ERROR_CODE);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(value = UsernameAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleUsernameAlreadyExists(UsernameAlreadyExistsException e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);

        problem.setTitle("Username already exists");
        problem.setDetail(e.getMessage());
        problem.setProperty("code", UsernameAlreadyExistsException.ERROR_CODE);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problem.setTitle("Method argument not valid");
        problem.setDetail(Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage());

        return ResponseEntity.badRequest().body(problem);
    }
}
