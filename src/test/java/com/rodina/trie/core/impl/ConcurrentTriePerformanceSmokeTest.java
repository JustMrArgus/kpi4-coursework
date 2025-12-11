package com.rodina.trie.core.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Concurrent Trie Performance Smoke Tests")
class ConcurrentTriePerformanceSmokeTest {
  @Test
  @DisplayName("Should complete bulk insert and lookup within time threshold")
  void bulkInsertAndLookupCompletesUnderThreshold() {
    ConcurrentTrie<Integer> trie = new ConcurrentTrie<>();
    assertTimeoutPreemptively(
        Duration.ofSeconds(2),
        () -> {
          IntStream.range(0, 5_000).forEach(i -> trie.insert("key-" + i, i));
          IntStream.range(0, 5_000).forEach(i -> assertThat(trie.search("key-" + i)).contains(i));
        });
  }
}
