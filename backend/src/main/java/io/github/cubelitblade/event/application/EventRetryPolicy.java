package io.github.cubelitblade.event.application;

import io.github.cubelitblade.configuration.RetryConfig;
import io.github.cubelitblade.event.exception.TransientEventException;
import java.time.Duration;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;

@Component
@RequiredArgsConstructor
public class EventRetryPolicy {

  private final RetryConfig retryConfig;

  public boolean canRetry(int retriedCount) {
    return retriedCount < retryConfig.maxRetries();
  }

  public Duration getFixedBackoffRetryDuration() {
    return Duration.ofMillis(
        Math.min(
            retryConfig.baseDelay().toMillis(), // fixed backoff
            retryConfig.maxDelay().toMillis() // max backoff
            ));
  }

  public Duration getExponentialBackoffDuration(int retriedCount) {
    return Duration.ofMillis(
        Math.min(
            retryConfig.baseDelay().toMillis() * (1L << retriedCount), // exponential backoff
            retryConfig.maxDelay().toMillis() // max backoff
            ));
  }

  public static boolean isTransient(Throwable throwable) {
    if (throwable == null) {
      return false;
    }

    for (Class<? extends Throwable> transientType : TRANSIENT_CAUSES) {
      if (transientType.isInstance(throwable)) {
        return true;
      }
    }

    return isTransient(throwable.getCause());
  }

  private static final Set<Class<? extends Throwable>> TRANSIENT_CAUSES =
      Set.of(
          TransientDataAccessException.class,
          HttpServerErrorException.class,
          TransientEventException.class);
}
