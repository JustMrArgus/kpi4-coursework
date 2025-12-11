package com.rodina.trie.core.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import com.rodina.trie.contract.Trie;
import com.rodina.trie.core.impl.ConcurrentTrie;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Concurrent Trie Load Integration Tests")
class ConcurrentTrieLoadIT {

  private Trie<String> trie;

  @BeforeEach
  void setUp() {
    trie = new ConcurrentTrie<>();
  }

  @Test
  @DisplayName("Should preserve all data during parallel insertions")
  void parallelInsertionsPreserveAllData() throws InterruptedException {
    int totalTasks = 2_000;
    ExecutorService pool = Executors.newFixedThreadPool(16);
    CountDownLatch latch = new CountDownLatch(totalTasks);
    for (int i = 0; i < totalTasks; i++) {
      final int value = i;
      pool.submit(
          () -> {
            try {
              trie.insert("key-" + value, String.valueOf(value));
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

  @Test
  @DisplayName("Should delete key exactly once when multiple threads try simultaneously")
  void deletionRaceCondition() throws InterruptedException {
    int keyCount = 1000;
    int threadCount = 10;
    List<String> keys = new ArrayList<>();
    ConcurrentHashMap<String, AtomicInteger> successfulDeletes = new ConcurrentHashMap<>();

    for (int i = 0; i < keyCount; i++) {
      String key = "key-" + i;
      keys.add(key);
      trie.insert(key, "value-" + i);
      successfulDeletes.put(key, new AtomicInteger(0));
    }

    assertThat(trie.size()).isEqualTo(keyCount);

    ExecutorService service = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);

    for (int t = 0; t < threadCount; t++) {
      service.submit(
          () -> {
            for (String key : keys) {
              if (trie.delete(key)) {
                successfulDeletes.get(key).incrementAndGet();
              }
            }
            latch.countDown();
          });
    }

    latch.await(10, TimeUnit.SECONDS);
    service.shutdown();

    for (String key : keys) {
      assertThat(successfulDeletes.get(key).get())
          .withFailMessage("Key %s was not deleted exactly once", key)
          .isEqualTo(1);
    }

    assertThat(trie.isEmpty()).isTrue();
    assertThat(trie.size()).isZero();
  }
}
