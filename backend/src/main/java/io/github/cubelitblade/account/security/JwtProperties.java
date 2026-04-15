package io.github.cubelitblade.account.security;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
  private String secret;
  private Duration expiration;
  private String cookieName = "AUTH_TOKEN";
  private String cookiePath = "/";
  private boolean cookieSecure = false;
  private String cookieSameSite = "Lax";
}
