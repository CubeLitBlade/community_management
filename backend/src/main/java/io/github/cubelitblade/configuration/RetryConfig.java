package io.github.cubelitblade.configuration;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "worker.retry")
public record RetryConfig(Duration baseDelay, Duration maxDelay, int maxRetries) {}
