package io.github.cubelitblade.configuration;

import io.github.cubelitblade.common.id.SnowflakeIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdConfig {

  @Bean
  public SnowflakeIdGenerator snowflakeIdGenerator() {
    return new SnowflakeIdGenerator(1);
  }
}
