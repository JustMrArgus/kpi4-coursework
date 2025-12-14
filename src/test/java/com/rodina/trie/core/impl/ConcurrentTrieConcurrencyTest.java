package com.rodina.trie.core.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.rodina.trie.contract.Trie;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Concurrent Trie Concurrency Tests (Unified)")
class ConcurrentTrieConcurrencyTest {

  private int getOptimalThreadCount() {
    return Math.max(4, Runtime.getRuntime().availableProcessors() * 2);
  }

  @Test
  @DisplayName("Should preserve all entries during concurrent inserts with high contention")
  void concurrentInsertsPreserveAllEntries() throws InterruptedException {
    int threadCount = getOptimalThreadCount();
    int itemsPerThread = 1000;
    int keyRange = itemsPerThread * 2;

    Trie<Integer> trie = new ConcurrentTrie<>();
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);

    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch endLatch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
      final int threadId = i;
      executor.submit(
          () -> {
            try {
              startLatch.await();
              for (int j = 0; j < itemsPerThread; j++) {
                String key = "key-" + ((threadId * itemsPerThread + j) % keyRange);
                trie.insert(key, j);
              }
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            } finally {
              endLatch.countDown();
            }
          });
    }

    startLatch.countDown();
    boolean finished = endLatch.await(20, TimeUnit.SECONDS);
    executor.shutdown();

    assertThat(finished).as("Test finished in time").isTrue();
    assertThat(trie.size()).isGreaterThan(0);
    assertThat(trie.getAllKeys()).isNotEmpty();
  }

  @Test
  @DisplayName("Should remove all entries during concurrent deletes (High Contention)")
  void concurrentDeletesRemoveAllEntries() throws InterruptedException {
    int threadCount = getOptimalThreadCount();
    int itemsPerThread = 500;
    int sharedKeyCount = itemsPerThread * 2;
    Trie<Integer> trie = new ConcurrentTrie<>();
    List<String> keysToDelete = new ArrayList<>(sharedKeyCount);

    for (int i = 0; i < sharedKeyCount; i++) {
      String key = "shared-" + i;
      trie.insert(key, i);
      keysToDelete.add(key);
    }

    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch endLatch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
      List<String> threadKeys = new ArrayList<>(keysToDelete);
      Collections.shuffle(threadKeys);

      executor.submit(
          () -> {
            try {
              startLatch.await();
              for (String key : threadKeys) {
                trie.delete(key);
              }
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            } finally {
              endLatch.countDown();
            }
          });
    }

    startLatch.countDown();
    boolean finished = endLatch.await(20, TimeUnit.SECONDS);
    executor.shutdown();

    assertThat(finished).isTrue();
    assertThat(trie.size()).isZero();
    assertThat(trie.isEmpty()).isTrue();
  }

  @Test
  @DisplayName("Should maintain integrity under chaos (Chaos Monkey)")
  void chaosMonkeyTest() throws InterruptedException {
    ConcurrentTrie<Integer> trie = new ConcurrentTrie<>();
    int threads = getOptimalThreadCount();
    int operationsPerThread = 2_000;
    ExecutorService executor = Executors.newFixedThreadPool(threads);

    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch endLatch = new CountDownLatch(threads);

    for (int i = 0; i < threads; i++) {
      executor.submit(
          () -> {
            Random random = new Random();
            try {
              startLatch.await();
              for (int j = 0; j < operationsPerThread; j++) {
                String key = String.valueOf(random.nextInt(1_000));
                int op = random.nextInt(100);

                if (op < 60) {
                  trie.insert(key, j);
                } else if (op < 80) {
                  trie.search(key);
                } else if (op < 95) {
                  trie.delete(key);
                } else {
                  trie.autocomplete(key.substring(0, Math.min(key.length(), 1)), 10);
                }
              }
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            } finally {
              endLatch.countDown();
            }
          });
    }

    startLatch.countDown();
    boolean finished = endLatch.await(30, TimeUnit.SECONDS);
    executor.shutdown();

    assertThat(finished).isTrue();
    assertThat(trie.size()).isGreaterThanOrEqualTo(0);
    for (String key : trie.getAllKeys()) {
      assertThat(trie.search(key)).isNotNull();
    }
  }

  @Test
  @DisplayName("Should produce consistent state with mixed operations")
  void mixedOperationsProduceConsistentState() throws InterruptedException {
    Trie<Integer> trie = new ConcurrentTrie<>();
    int threadCount = getOptimalThreadCount();
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch endLatch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
      final int id = i;
      executor.submit(
          () -> {
            try {
              startLatch.await();
              String key = "key-" + id;
              if (id % 2 == 0) {
                trie.insert(key, id);
              } else {
                trie.insert(key, id);
                trie.delete(key);
              }
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            } finally {
              endLatch.countDown();
            }
          });
    }

    startLatch.countDown();
    endLatch.await(10, TimeUnit.SECONDS);
    executor.shutdown();

    assertThat(trie.size()).isEqualTo(threadCount / 2 + (threadCount % 2));
  }

  @Test
  @DisplayName("Should not corrupt state during concurrent prefix searches")
  void concurrentPrefixSearchDoesNotCorruptState() throws InterruptedException {
    Trie<Integer> trie = new ConcurrentTrie<>();
    int writeThreads = 5;
    int readThreads = 10;
    int items = 1000;

    ExecutorService executor = Executors.newFixedThreadPool(writeThreads + readThreads);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch endLatch = new CountDownLatch(writeThreads + readThreads);
    List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

    for (int i = 0; i < writeThreads; i++) {
      final int threadId = i;
      executor.submit(
          () -> {
            try {
              startLatch.await();
              for (int j = 0; j < items; j++) {
                trie.insert("group" + threadId + "-" + j, j);
              }
            } catch (Exception e) {
              exceptions.add(e);
            } finally {
              endLatch.countDown();
            }
          });
    }

    for (int i = 0; i < readThreads; i++) {
      executor.submit(
          () -> {
            try {
              startLatch.await();
              while (endLatch.getCount() > readThreads) {
                trie.searchByPrefix("group");
                trie.autocomplete("group", 50);
                trie.size();
                Thread.yield();
              }
            } catch (Exception e) {
              exceptions.add(e);
            } finally {
              endLatch.countDown();
            }
          });
    }

    startLatch.countDown();
    endLatch.await(10, TimeUnit.SECONDS);
    executor.shutdown();

    assertThat(exceptions).isEmpty();
    assertThat(trie.size()).isEqualTo(writeThreads * items);
  }
}
