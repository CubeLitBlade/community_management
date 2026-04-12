package io.github.cubelitblade.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  public void commence(@NonNull HttpServletRequest request, HttpServletResponse response,
                       AuthenticationException authException) throws IOException {

    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);

    problem.setTitle("Unauthorized");
    problem.setDetail(authException.getMessage());
    problem.setInstance(URI.create(request.getRequestURI()));
    problem.setProperty("code", "UNAUTHORIZED");

    response.setContentType("application/problem+json");
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setCharacterEncoding("UTF-8");

    objectMapper.writeValue(response.getOutputStream(), problem);
  }
}
