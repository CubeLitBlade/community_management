package io.github.cubelitblade.account.security;

import io.github.cubelitblade.account.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

  private final JwtProperties jwtProperties;
  private final SecretKey key;

  public JwtTokenProvider(JwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
    this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
  }

  public String generateToken(Long accountId, Role role, Instant now) {
    Instant expirationDate = now.plus(jwtProperties.getExpiration());

    return Jwts.builder()
        .subject(accountId.toString())
        .claim("role", role.getValue())
        .issuedAt(Date.from(now))
        .expiration(Date.from(expirationDate))
        .signWith(key)
        .compact();
  }

  public JwtAuthenticatedUser parseToken(String token) {
    Claims claims = parseClaims(token);

    Long accountId =
        Long.valueOf(Objects.requireNonNull(claims.getSubject(), "Account ID is null. "));
    Role role =
        Role.from(Objects.requireNonNull(claims.get("role", String.class), "Role is null. "));

    return new JwtAuthenticatedUser(accountId, role);
  }

  public Date getExpirationDate(String token) {
    Claims claims = parseClaims(token);
    return claims.getExpiration();
  }

  private Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }
}
