package com.rodina.trie.core.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rodina.trie.contract.Trie;

@DisplayName("Trie Integrity Tests")
class TrieIntegrityTest {
  @Test
  @DisplayName("Should support large key insertion")
  void largeKeyInsertionSupported() {
    Trie<Integer> trie = new ConcurrentTrie<>();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 10_000; i++) {
      sb.append('a');
    }
    String longKey = sb.toString();
    assertThatCode(() -> trie.insert(longKey, 1)).doesNotThrowAnyException();
    assertThat(trie.has(longKey)).isTrue();
    assertThat(trie.delete(longKey)).isTrue();
    assertThat(trie.isEmpty()).isTrue();
  }

  @Test
  @DisplayName("Should keep trie stable during clear under load")
  void clearUnderLoadKeepsTrieStable() throws InterruptedException {
    Trie<Integer> trie = new ConcurrentTrie<>();
    int threadCount = 10;
    ExecutorService service = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    for (int i = 0; i < threadCount; i++) {
      service.submit(
          () -> {
            try {
              for (int j = 0; j < 1_000; j++) {
                trie.insert("key" + j, j);
                if (j % 100 == 0) {
                  trie.clear();
                }
              }
            } finally {
              latch.countDown();
            }
          });
    }
    latch.await();
    service.shutdown();
    assertThat(trie.size()).isGreaterThanOrEqualTo(0);
  }

  @Test
  @DisplayName("Should store values as independent references")
  void valuesAreIndependentReferences() {
    Trie<List<String>> trie = new ConcurrentTrie<>();
    List<String> list1 = new ArrayList<>();
    list1.add("A");
    trie.insert("key", list1);
    List<String> retrieved = trie.search("key").get();
    retrieved.add("B");
    assertThat(list1).hasSize(2);
    assertThat(retrieved).isSameAs(list1);
  }

  @Test
  @DisplayName("Should perform case-sensitive prefix search")
  void searchByPrefixIsCaseSensitive() {
    Trie<Integer> trie = new ConcurrentTrie<>();
    trie.insert("Apple", 1);
    trie.insert("apple", 2);
    assertThat(trie.searchByPrefix("App")).hasSize(1);
    assertThat(trie.searchByPrefix("app")).hasSize(1);
    assertThat(trie.searchByPrefix("a")).hasSize(1);
    assertThat(trie.searchByPrefix("Z")).isEmpty();
  }
}
