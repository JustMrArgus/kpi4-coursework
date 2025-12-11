package com.rodina.trie.core.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rodina.trie.contract.Trie;
import com.rodina.trie.exception.InvalidKeyException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Concurrent Trie Basic Tests")
class ConcurrentTrieBasicTest {
  private Trie<Integer> trie;

  @BeforeEach
  void setUp() {
    trie = new ConcurrentTrie<>();
  }

  @Test
  @DisplayName("Should return stored value after insert and search")
  void insertAndSearchReturnsStoredValue() {
    trie.insert("apple", 100);
    assertThat(trie.search("apple")).isPresent().contains(100);
  }

  @Test
  @DisplayName("Should return empty when searching for non-existent key")
  void searchNonExistentReturnsEmpty() {
    assertThat(trie.search("apple")).isEmpty();
    trie.insert("app", 1);
    assertThat(trie.search("apple")).isEmpty();
  }

  @Test
  @DisplayName("Should throw exception when inserting null value")
  void insertNullValueThrowsException() {
    assertThatThrownBy(() -> trie.insert("key", null)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("Should throw InvalidKeyException when inserting null key")
  void insertInvalidKeyThrowsException() {
    assertThatThrownBy(() -> trie.insert(null, 1)).isInstanceOf(InvalidKeyException.class);
  }

  @Test
  @DisplayName("Should update value when inserting existing key")
  void insertUpdatesExistingValue() {
    trie.insert("key", 1);
    assertThat(trie.search("key")).contains(1);
    trie.insert("key", 2);
    assertThat(trie.search("key")).contains(2);
  }

  @Test
  @DisplayName("Should remove leaf node entry when deleting")
  void deleteLeafNodeRemovesEntry() {
    trie.insert("apple", 1);
    assertThat(trie.has("apple")).isTrue();
    boolean deleted = trie.delete("apple");
    assertThat(deleted).isTrue();
    assertThat(trie.has("apple")).isFalse();
    assertThat(trie.size()).isZero();
  }

  @Test
  @DisplayName("Should keep children when deleting middle node")
  void deleteMiddleNodeKeepsChildren() {
    trie.insert("apple", 1);
    trie.insert("app", 2);
    assertThat(trie.delete("app")).isTrue();
    assertThat(trie.has("app")).isFalse();
    assertThat(trie.has("apple")).isTrue();
    assertThat(trie.size()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should empty trie when deleting single root-like node")
  void deleteRootLikeNodeEmptiesTrie() {
    trie.insert("a", 1);
    assertThat(trie.delete("a")).isTrue();
    assertThat(trie.isEmpty()).isTrue();
  }

  @Test
  @DisplayName("Should return false when deleting non-existent key")
  void deleteNonExistentReturnsFalse() {
    trie.insert("apple", 1);
    assertThat(trie.delete("banana")).isFalse();
    assertThat(trie.delete("app")).isFalse();
    assertThat(trie.size()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should correctly check prefix existence with startsWith")
  void startsWithChecksPrefix() {
    trie.insert("table", 1);
    trie.insert("tablet", 2);
    trie.insert("tab", 3);
    assertThat(trie.startsWith("t")).isTrue();
    assertThat(trie.startsWith("ta")).isTrue();
    assertThat(trie.startsWith("tab")).isTrue();
    assertThat(trie.startsWith("table")).isTrue();
    assertThat(trie.startsWith("tablet")).isTrue();
    assertThat(trie.startsWith("tabs")).isFalse();
    assertThat(trie.startsWith("z")).isFalse();
  }

  @Test
  @DisplayName("Should return all matching entries for prefix search")
  void searchByPrefixReturnsMatchingEntries() {
    trie.insert("car", 1);
    trie.insert("cart", 2);
    trie.insert("cat", 3);
    trie.insert("dog", 4);
    List<Map.Entry<String, Integer>> results = trie.searchByPrefix("ca");
    assertThat(results).hasSize(3);
    assertThat(results)
        .extracting(Map.Entry::getKey)
        .containsExactlyInAnyOrder("car", "cart", "cat");
  }

  @Test
  @DisplayName("Should limit autocomplete suggestions to specified count")
  void autocompleteReturnsLimitedSuggestions() {
    trie.insert("hello", 1);
    trie.insert("help", 2);
    trie.insert("hell", 3);
    trie.insert("helmet", 4);
    trie.insert("helicopter", 5);
    List<String> suggestions = trie.autocomplete("hel", 3);
    assertThat(suggestions).hasSize(3).contains("hell");
  }

  @Test
  @DisplayName("Should respect limit in autocomplete results")
  void autocompleteLimitRestrictsResults() {
    trie.insert("a1", 1);
    trie.insert("a2", 2);
    List<String> res = trie.autocomplete("a", 1);
    assertThat(res).hasSize(1);
  }

  @Test
  @DisplayName("Should correctly track size and handle clear operation")
  void sizeAndClearWorkCorrectly() {
    assertThat(trie.isEmpty()).isTrue();
    trie.insert("one", 1);
    trie.insert("two", 2);
    assertThat(trie.size()).isEqualTo(2);
    assertThat(trie.isEmpty()).isFalse();
    trie.clear();
    assertThat(trie.size()).isZero();
    assertThat(trie.isEmpty()).isTrue();
    assertThat(trie.has("one")).isFalse();
  }

  @Test
  @DisplayName("Should correctly calculate longest common prefix")
  void longestCommonPrefixCalculatesCorrectly() {
    trie.insert("flower", 1);
    trie.insert("flow", 2);
    trie.insert("flight", 3);
    assertThat(trie.longestCommonPrefix()).isEqualTo("fl");
    trie.clear();
    trie.insert("dog", 1);
    trie.insert("racecar", 2);
    assertThat(trie.longestCommonPrefix()).isEmpty();
    trie.clear();
    trie.insert("interstellar", 1);
    assertThat(trie.longestCommonPrefix()).isEqualTo("interstellar");
  }

  @Test
  @DisplayName("Should return all stored keys via getAllKeys")
  void getAllKeysReturnsAllStoredKeys() {
    trie.insert("a", 1);
    trie.insert("b", 2);
    List<String> keys = trie.getAllKeys();
    assertThat(keys).hasSize(2).contains("a", "b");
  }

  @Test
  @DisplayName("Should traverse all entries via iterator")
  void iteratorTraversesAllEntries() {
    trie.insert("one", 1);
    trie.insert("two", 2);
    int count = 0;
    for (Map.Entry<String, Integer> entry : trie) {
      count++;
      assertThat(entry.getKey()).isNotNull();
      assertThat(entry.getValue()).isNotNull();
    }
    assertThat(count).isEqualTo(2);
  }
}
