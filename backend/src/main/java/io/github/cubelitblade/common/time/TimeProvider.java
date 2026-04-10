package io.github.cubelitblade.common.time;

import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class TimeProvider {

  private final Clock clock;

  public TimeProvider() {
    this.clock = Clock.systemDefaultZone();
  }

  public TimeProvider(Clock clock) {
    this.clock = clock;
  }

  public Instant now() {
    return Instant.now(clock);
  }
}
