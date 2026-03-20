package io.github.cubelitblade.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "worker.retry")
public class RetryConfig {
    private Duration baseDelay;
    private Duration maxDelay;
    private int maxRetries;
}
