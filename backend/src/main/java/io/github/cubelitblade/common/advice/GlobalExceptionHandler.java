package io.github.cubelitblade.common.advice;

import io.github.cubelitblade.common.exception.InvalidParameterException;
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
}
