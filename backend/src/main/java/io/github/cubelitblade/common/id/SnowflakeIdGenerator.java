package io.github.cubelitblade.common.id;

import java.time.Instant;

public class SnowflakeIdGenerator {

  private static final long EPOCH = Instant.parse("2026-03-11T00:00:00Z").toEpochMilli();

  private static final long SEQUENCE_BITS = 12L;
  private static final long WORKER_ID_BITS = 10L;

  private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
  private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

  private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
  private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

  private final long workerId;

  private long sequence = 0L;
  private long lastTimestamp = -1L;

  public SnowflakeIdGenerator(long workerId) {
    if (workerId < 0 || workerId > MAX_WORKER_ID) {
      throw new IllegalArgumentException("Worker Id is out of range");
    }
    this.workerId = workerId;
  }

  public synchronized long nextId() {
    long currentTimestamp = System.currentTimeMillis();

    if (currentTimestamp < lastTimestamp) {
      throw new IllegalStateException(
          "The clock has been rolled back, ID generation has been disabled.");
    }

    long seq;

    if (currentTimestamp == lastTimestamp) {
      seq = (sequence + 1) & MAX_SEQUENCE;
      if (seq == 0) {
        currentTimestamp = waitNextMillis(lastTimestamp);
      }
      sequence = seq;
    } else {
      seq = 0;
      sequence = 0;
    }

    lastTimestamp = currentTimestamp;

    return ((currentTimestamp - EPOCH) << TIMESTAMP_SHIFT) | (workerId << WORKER_ID_SHIFT) | seq;
  }

  private long waitNextMillis(long lastTimestamp) {
    long timestamp = System.currentTimeMillis();
    while (timestamp <= lastTimestamp) {
      Thread.onSpinWait();
      timestamp = System.currentTimeMillis();
    }
    return timestamp;
  }
}
