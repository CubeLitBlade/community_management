package io.github.cubelitblade.account.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtTokenProvider jwtTokenProvider;
  private final StringRedisTemplate stringRedisTemplate;
  private final AuthCookieService authCookieService;
  private final AuthenticationEntryPoint authenticationEntryPoint;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    String token = authCookieService.resolveToken(request);
    if (token == null) {
      filterChain.doFilter(request, response);
      return;
    }

    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      String blackListKey = "jwt:blacklist:" + token;

      if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(blackListKey))) {
        log.warn(
            "JWT token is blacklisted: method={}, uri={}, remoteIp={}, userAgent={}",
            request.getMethod(),
            request.getRequestURI(),
            request.getRemoteAddr(),
            request.getHeader(HttpHeaders.USER_AGENT));
        authenticationEntryPoint.commence(
            request,
            response,
            new InsufficientAuthenticationException("JWT token is no longer valid."));
        return;
      }

      try {
        JwtAuthenticatedUser user = jwtTokenProvider.parseToken(token);
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.role().name());

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(user, null, List.of(authority));
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (JwtException | IllegalArgumentException e) {
        log.warn(
            "JWT authentication failed: method={}, uri={}, remoteIp={}, userAgent={}, reason={}",
            request.getMethod(),
            request.getRequestURI(),
            request.getRemoteAddr(),
            request.getHeader(HttpHeaders.USER_AGENT),
            e.getClass().getSimpleName() + ": " + e.getMessage());
        log.debug("JWT authentication failure stacktrace", e);

        SecurityContextHolder.clearContext();
        authenticationEntryPoint.commence(
            request, response, new InsufficientAuthenticationException("JWT token is invalid.", e));
        return;
      }
    }

    filterChain.doFilter(request, response);
  }
}
