package io.github.cubelitblade.common.security;

import io.github.cubelitblade.common.exception.ApiErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  public void commence(
      @NonNull HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {

    ProblemDetail problem = ProblemDetail.forStatus(ApiErrorCode.UNAUTHORIZED.statusCode());
    problem.setTitle(ApiErrorCode.UNAUTHORIZED.getTitle());
    problem.setDetail(authException.getMessage());
    problem.setInstance(URI.create(request.getRequestURI()));
    problem.setProperty("code", ApiErrorCode.UNAUTHORIZED.name());

    response.setContentType("application/problem+json");
    response.setStatus(ApiErrorCode.UNAUTHORIZED.getStatus().value());
    response.setCharacterEncoding("UTF-8");

    objectMapper.writeValue(response.getOutputStream(), problem);
  }
}
