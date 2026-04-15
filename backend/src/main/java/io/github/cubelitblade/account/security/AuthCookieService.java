package io.github.cubelitblade.account.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthCookieService {
  private final JwtProperties jwtProperties;

  public void writeAuthCookie(HttpHeaders headers, String token) {
    headers.add(HttpHeaders.SET_COOKIE, createAuthCookie(token).toString());
  }

  public void clearAuthCookie(HttpHeaders headers) {
    headers.add(
        HttpHeaders.SET_COOKIE,
        ResponseCookie.from(jwtProperties.getCookieName(), "")
            .httpOnly(true)
            .secure(jwtProperties.isCookieSecure())
            .sameSite(jwtProperties.getCookieSameSite())
            .path(jwtProperties.getCookiePath())
            .maxAge(0)
            .build()
            .toString());
  }

  public String resolveToken(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }

    for (Cookie cookie : cookies) {
      if (jwtProperties.getCookieName().equals(cookie.getName())) {
        String value = cookie.getValue();
        return value == null || value.isBlank() ? null : value;
      }
    }

    return null;
  }

  private ResponseCookie createAuthCookie(String token) {
    return ResponseCookie.from(jwtProperties.getCookieName(), token)
        .httpOnly(true)
        .secure(jwtProperties.isCookieSecure())
        .sameSite(jwtProperties.getCookieSameSite())
        .path(jwtProperties.getCookiePath())
        .maxAge(jwtProperties.getExpiration())
        .build();
  }
}
