package io.github.cubelitblade.configuration;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Instant;

@Data
@Configuration
public class TimeConfig {
    private Clock clock = Clock.systemDefaultZone();

    public Instant now() {
        return Instant.now(clock);
    }
}
