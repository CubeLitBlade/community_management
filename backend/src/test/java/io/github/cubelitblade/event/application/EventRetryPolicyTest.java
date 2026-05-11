package io.github.cubelitblade.event.application;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.cubelitblade.configuration.RetryConfig;
import io.github.cubelitblade.event.exception.DownstreamTimeoutException;
import io.github.cubelitblade.event.exception.RejectedEventException;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;

class EventRetryPolicyTest {
  private EventRetryPolicy retryPolicy;

  @BeforeEach
  void setUp() {
    retryPolicy =
        new EventRetryPolicy(new RetryConfig(Duration.ofSeconds(1), Duration.ofSeconds(5), 3));
  }

  @Test
  @DisplayName("Retry: should allow retries before max retry count")
  void should_allow_retries_before_max_retry_count() {
    assertThat(retryPolicy.canRetry(0)).isTrue();
    assertThat(retryPolicy.canRetry(2)).isTrue();
    assertThat(retryPolicy.canRetry(3)).isFalse();
  }

  @Test
  @DisplayName("Backoff: should calculate fixed and capped exponential durations")
  void should_calculate_fixed_and_capped_exponential_durations() {
    assertThat(retryPolicy.getFixedBackoffRetryDuration()).isEqualTo(Duration.ofSeconds(1));
    assertThat(retryPolicy.getExponentialBackoffDuration(0)).isEqualTo(Duration.ofSeconds(1));
    assertThat(retryPolicy.getExponentialBackoffDuration(2)).isEqualTo(Duration.ofSeconds(4));
    assertThat(retryPolicy.getExponentialBackoffDuration(4)).isEqualTo(Duration.ofSeconds(5));
  }

  @Test
  @DisplayName("Transient: should identify transient exception causes")
  void should_identify_transient_exception_causes() {
    assertThat(EventRetryPolicy.isTransient(null)).isFalse();
    assertThat(EventRetryPolicy.isTransient(new DownstreamTimeoutException("timeout"))).isTrue();
    assertThat(
            EventRetryPolicy.isTransient(
                new RuntimeException("wrapped", new DownstreamTimeoutException("timeout"))))
        .isTrue();
    assertThat(
            EventRetryPolicy.isTransient(
                new TransientDataAccessResourceException("database unavailable")))
        .isTrue();
    assertThat(
            EventRetryPolicy.isTransient(
                HttpServerErrorException.create(
                    HttpStatus.BAD_GATEWAY, "bad gateway", null, null, null)))
        .isTrue();
    assertThat(EventRetryPolicy.isTransient(new RejectedEventException("fatal"))).isFalse();
  }
}
