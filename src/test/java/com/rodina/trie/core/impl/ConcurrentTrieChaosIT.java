package com.rodina.trie.core.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Concurrent Trie Chaos Integration Tests")
class ConcurrentTrieChaosIT {
  @Test
  @DisplayName("Should maintain trie consistency under chaotic concurrent operations")
  void chaosMonkeyStillKeepsTrieConsistent() throws InterruptedException {
    ConcurrentTrie<Integer> trie = new ConcurrentTrie<>();
    int threads = 32;
    int operationsPerThread = 2_000;
    ExecutorService executor = Executors.newFixedThreadPool(threads);
    CountDownLatch latch = new CountDownLatch(threads);
    AtomicInteger insertCount = new AtomicInteger(0);
    AtomicInteger deleteCount = new AtomicInteger(0);
    for (int i = 0; i < threads; i++) {
      executor.submit(
          () -> {
            Random random = new Random();
            try {
              for (int j = 0; j < operationsPerThread; j++) {
                String key = String.valueOf(random.nextInt(1_000));
                int op = random.nextInt(100);
                if (op < 60) {
                  trie.insert(key, j);
                  insertCount.incrementAndGet();
                } else if (op < 80) {
                  trie.search(key);
                } else if (op < 95) {
                  if (trie.delete(key)) {
                    deleteCount.incrementAndGet();
                  }
                } else {
                  trie.autocomplete(key.substring(0, Math.min(key.length(), 1)), 10);
                }
              }
            } finally {
              latch.countDown();
            }
          });
    }
    latch.await();
    executor.shutdown();
    int finalSize = trie.size();
    assertThat(finalSize).isGreaterThanOrEqualTo(0);
    assertThat(finalSize).isLessThanOrEqualTo(insertCount.get());
    for (String key : trie.getAllKeys()) {
      assertThat(trie.search(key)).isNotNull();
    }
  }

  @Test
  @DisplayName("Should maintain integrity with deeply nested branches under stress")
  void deepBranchStressMaintainsIntegrity() {
    ConcurrentTrie<Integer> trie = new ConcurrentTrie<>();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 500; i++) {
      sb.append("a");
      trie.insert(sb.toString(), i);
    }
    assertThat(trie.size()).isEqualTo(500);
    assertThat(trie.search(sb.toString())).contains(499);
    trie.delete(sb.toString());
    assertThat(trie.size()).isEqualTo(499);
    assertThat(trie.has(sb.toString())).isFalse();
  }
}
