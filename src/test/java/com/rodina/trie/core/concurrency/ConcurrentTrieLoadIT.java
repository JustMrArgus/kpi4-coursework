package com.rodina.trie.core.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rodina.trie.contract.Trie;
import com.rodina.trie.core.impl.ConcurrentTrie;

@DisplayName("Concurrent Trie Load Integration Tests")
class ConcurrentTrieLoadIT {
  @Test
  @DisplayName("Should preserve all data during parallel insertions")
  void parallelInsertionsPreserveAllData() throws InterruptedException {
    Trie<Integer> trie = new ConcurrentTrie<>();
    int totalTasks = 2_000;
    ExecutorService pool = Executors.newFixedThreadPool(16);
    CountDownLatch latch = new CountDownLatch(totalTasks);
    for (int i = 0; i < totalTasks; i++) {
      final int value = i;
      pool.submit(
          () -> {
            try {
              trie.insert("key-" + value, value);
            } finally {
              latch.countDown();
            }
          });
    }
    boolean completed = latch.await(30, TimeUnit.SECONDS);
    pool.shutdownNow();
    assertThat(completed).as("insertion tasks completed in time").isTrue();
    assertThat(trie.size()).isEqualTo(totalTasks);
    List<String> keys = trie.autocomplete("key-", totalTasks + 10);
    assertThat(keys).hasSize(totalTasks);
  }

  @Test
  @DisplayName("Should eventually drain trie with interleaved deletes")
  void interleavedDeletesEventuallyDrainTrie() throws InterruptedException {
    ConcurrentTrie<String> trie = new ConcurrentTrie<>();
    int iterations = 1_000;
    List<String> keys =
        IntStream.range(0, iterations).mapToObj(i -> "word-" + i).collect(Collectors.toList());
    keys.forEach(key -> trie.insert(key, key));
    ExecutorService pool = Executors.newFixedThreadPool(12);
    CountDownLatch latch = new CountDownLatch(iterations);
    keys.forEach(
        key ->
            pool.submit(
                () -> {
                  try {
                    trie.delete(key);
                  } finally {
                    latch.countDown();
                  }
                }));
    boolean completed = latch.await(15, TimeUnit.SECONDS);
    pool.shutdownNow();
    assertThat(completed).isTrue();
    assertThat(trie.size()).isZero();
    assertThat(trie.isEmpty()).isTrue();
  }
}
