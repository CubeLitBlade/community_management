package io.github.cubelitblade.common.security;

import io.github.cubelitblade.common.exception.ApiErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
  private final ObjectMapper objectMapper;

  @Override
  public void handle(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull AccessDeniedException accessDeniedException)
      throws IOException {
    ProblemDetail problem = ProblemDetail.forStatus(ApiErrorCode.FORBIDDEN.statusCode());
    problem.setTitle(ApiErrorCode.FORBIDDEN.getTitle());
    problem.setDetail(accessDeniedException.getMessage());
    problem.setInstance(URI.create(request.getRequestURI()));
    problem.setProperty("code", ApiErrorCode.FORBIDDEN.name());

    response.setContentType("application/problem+json");
    response.setStatus(ApiErrorCode.FORBIDDEN.getStatus().value());
    response.setCharacterEncoding("UTF-8");

    objectMapper.writeValue(response.getOutputStream(), problem);
  }
}
