package io.github.cubelitblade.common.exception;

import org.springframework.web.ErrorResponse;

public abstract class DomainException extends RuntimeException implements ErrorResponse {
  public DomainException(String message) {
    super(message);
  }
}
