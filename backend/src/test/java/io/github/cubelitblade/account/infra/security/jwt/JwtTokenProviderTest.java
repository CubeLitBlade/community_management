package io.github.cubelitblade.account.infra.security.jwt;

import io.github.cubelitblade.account.domain.model.Role;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String TEST_JWT_SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
    private static final String ALTERNATE_JWT_SECRET = "ZmVkY2JhOTg3NjU0MzIxMGZlZGNiYTk4NzY1NDMyMTA=";

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret(TEST_JWT_SECRET);
        jwtProperties.setExpiration(Duration.ofHours(1));

        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
    }

    @Test
    void should_parse_generated_token() {
        Instant now = Instant.now();

        String token = jwtTokenProvider.generateToken(7L, Role.USER, now);

        JwtAuthenticatedUser user = jwtTokenProvider.parseToken(token);

        assertThat(user.accountId()).isEqualTo(7L);
        assertThat(user.role()).isEqualTo(Role.USER);
    }

    @Test
    void should_reject_token_signed_with_another_secret() {
        JwtProperties anotherProperties = new JwtProperties();
        anotherProperties.setSecret(ALTERNATE_JWT_SECRET);
        anotherProperties.setExpiration(Duration.ofHours(1));

        JwtTokenProvider anotherProvider = new JwtTokenProvider(anotherProperties);
        String token = anotherProvider.generateToken(7L, Role.USER, Instant.now());

        assertThatThrownBy(() -> jwtTokenProvider.parseToken(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void should_reject_expired_token() {
        Instant issuedAt = Instant.now().minus(Duration.ofHours(2));

        String token = jwtTokenProvider.generateToken(7L, Role.USER, issuedAt);

        assertThatThrownBy(() -> jwtTokenProvider.parseToken(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
