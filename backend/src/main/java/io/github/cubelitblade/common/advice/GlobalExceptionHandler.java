package io.github.cubelitblade.common.advice;

import io.github.cubelitblade.common.exception.InvalidParameterException;
import io.github.cubelitblade.user.domain.exception.UsernameAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    @ExceptionHandler(value = UsernameAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleUsernameAlreadyExists(UsernameAlreadyExistsException e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);

        problem.setTitle("Username already exists");
        problem.setDetail(e.getMessage());
        problem.setProperty("code", UsernameAlreadyExistsException.ERROR_CODE);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }
}
