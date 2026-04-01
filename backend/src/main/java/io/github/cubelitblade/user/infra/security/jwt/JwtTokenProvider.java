package io.github.cubelitblade.user.infra.security.jwt;

import io.github.cubelitblade.user.domain.model.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

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
}
