package io.github.cubelitblade.message.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PrivateMessageModelBranchTest {

  @Test
  void should_reconstitute_null_snapshot_as_null() {
    assertThat(PrivateMessage.reconstitute(null)).isNull();
  }
}
