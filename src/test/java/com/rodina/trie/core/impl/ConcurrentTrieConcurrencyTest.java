package com.rodina.trie.core.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rodina.trie.contract.Trie;

@DisplayName("Concurrent Trie Concurrency Tests")
class ConcurrentTrieConcurrencyTest {
  @Test
  @DisplayName("Should preserve all entries during concurrent inserts")
  void concurrentInsertsPreserveAllEntries() throws InterruptedException {
    int threadCount = 50;
    int itemsPerThread = 1000;
    Trie<Integer> trie = new ConcurrentTrie<>();
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    for (int i = 0; i < threadCount; i++) {
      final int threadId = i;
      executor.submit(
          () -> {
            try {
              for (int j = 0; j < itemsPerThread; j++) {
                String key = "key-" + threadId + "-" + j;
                trie.insert(key, j);
              }
            } finally {
              latch.countDown();
            }
          });
    }
    latch.await();
    executor.shutdown();
    assertThat(trie.size()).isEqualTo(threadCount * itemsPerThread);
    for (int i = 0; i < threadCount; i++) {
      for (int j = 0; j < itemsPerThread; j++) {
        String key = "key-" + i + "-" + j;
        assertThat(trie.has(key)).isTrue();
        assertThat(trie.search(key)).contains(j);
      }
    }
  }

  @Test
  @DisplayName("Should remove all entries during concurrent deletes")
  void concurrentDeletesRemoveAllEntries() throws InterruptedException {
    int threadCount = 20;
    int itemsPerThread = 500;
    Trie<Integer> trie = new ConcurrentTrie<>();
    for (int i = 0; i < threadCount; i++) {
      for (int j = 0; j < itemsPerThread; j++) {
        trie.insert("key-" + i + "-" + j, j);
      }
    }
    assertThat(trie.size()).isEqualTo(threadCount * itemsPerThread);
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    for (int i = 0; i < threadCount; i++) {
      final int threadId = i;
      executor.submit(
          () -> {
            try {
              for (int j = 0; j < itemsPerThread; j++) {
                String key = "key-" + threadId + "-" + j;
                trie.delete(key);
              }
            } finally {
              latch.countDown();
            }
          });
    }
    latch.await();
    executor.shutdown();
    assertThat(trie.size()).isZero();
    assertThat(trie.isEmpty()).isTrue();
  }

  @Test
  @DisplayName("Should produce consistent state with mixed operations")
  void mixedOperationsProduceConsistentState() throws InterruptedException {
    Trie<Integer> trie = new ConcurrentTrie<>();
    int threadCount = 100;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch endLatch = new CountDownLatch(threadCount);
    AtomicInteger insertedCount = new AtomicInteger(0);
    for (int i = 0; i < threadCount; i++) {
      final int id = i;
      executor.submit(
          () -> {
            try {
              startLatch.await();
              String key = "key-" + id;
              if (id % 2 == 0) {
                trie.insert(key, id);
                insertedCount.incrementAndGet();
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
    assertThat(trie.size()).isEqualTo(threadCount / 2);
  }

  @Test
  @DisplayName("Should not corrupt state during concurrent prefix searches")
  void concurrentPrefixSearchDoesNotCorruptState() throws InterruptedException {
    Trie<Integer> trie = new ConcurrentTrie<>();
    int writeThreads = 10;
    int readThreads = 10;
    int items = 1000;
    ExecutorService executor = Executors.newFixedThreadPool(writeThreads + readThreads);
    CountDownLatch latch = new CountDownLatch(writeThreads + readThreads);
    List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
    for (int i = 0; i < writeThreads; i++) {
      final int threadId = i;
      executor.submit(
          () -> {
            try {
              for (int j = 0; j < items; j++) {
                trie.insert("group" + threadId + "-" + j, j);
              }
            } catch (Exception e) {
              exceptions.add(e);
            } finally {
              latch.countDown();
            }
          });
    }
    for (int i = 0; i < readThreads; i++) {
      executor.submit(
          () -> {
            try {
              while (latch.getCount() > readThreads) {
                trie.searchByPrefix("group");
                trie.autocomplete("group", 50);
                trie.size();
              }
            } catch (Exception e) {
              exceptions.add(e);
            } finally {
              latch.countDown();
            }
          });
    }
    latch.await();
    executor.shutdown();
    assertThat(exceptions).as("Exceptions occurred during concurrent execution").isEmpty();
    assertThat(trie.size()).isEqualTo(writeThreads * items);
  }
}
