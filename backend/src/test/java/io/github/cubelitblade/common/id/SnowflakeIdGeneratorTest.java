package io.github.cubelitblade.common.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class SnowflakeIdGeneratorTest {

  @Test
  void should_generate_ids_and_validate_worker_range() {
    SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1L);

    long id = generator.nextId();

    assertThat(id).isPositive();
    assertThatThrownBy(() -> new SnowflakeIdGenerator(-1L))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new SnowflakeIdGenerator(1024L))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_reject_clock_rollback() throws Exception {
    SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1L);
    Field lastTimestamp = SnowflakeIdGenerator.class.getDeclaredField("lastTimestamp");
    lastTimestamp.setAccessible(true);
    lastTimestamp.setLong(generator, System.currentTimeMillis() + 60_000);

    assertThatThrownBy(generator::nextId).isInstanceOf(IllegalStateException.class);
  }
}
