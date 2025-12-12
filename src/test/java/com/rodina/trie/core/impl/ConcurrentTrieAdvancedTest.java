package com.rodina.trie.core.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import com.rodina.trie.exception.InvalidKeyException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Concurrent Trie Advanced Tests")
class ConcurrentTrieAdvancedTest {
  private ConcurrentTrie<Object> trie;

  @BeforeEach
  void setUp() {
    trie = new ConcurrentTrie<>();
  }

  @Test
  @DisplayName("Should support Unicode keys in insert and search")
  void insertAndSearchSupportUnicodeKeys() {
    String unicodeKey = "привіт-你好";
    trie.insert(unicodeKey, "hello");
    assertThat(trie.search(unicodeKey)).isPresent().contains("hello");
    assertThat(trie.has(unicodeKey)).isTrue();
  }

  @Test
  @DisplayName("Should throw InvalidKeyException when using empty string key")
  void emptyStringKeyBehavior() {
    String key = "";
    String value = "root-value";

    assertThatThrownBy(() -> trie.insert(key, value))
        .isInstanceOf(InvalidKeyException.class)
        .hasMessage("Key cannot be empty");

    assertThatThrownBy(() -> trie.search(key))
        .isInstanceOf(InvalidKeyException.class)
        .hasMessage("Key cannot be empty");

    assertThatThrownBy(() -> trie.has(key))
        .isInstanceOf(InvalidKeyException.class)
        .hasMessage("Key cannot be empty");

    assertThatThrownBy(() -> trie.delete(key))
        .isInstanceOf(InvalidKeyException.class)
        .hasMessage("Key cannot be empty");

    assertThat(trie.size()).isZero();
  }

  @Test
  @DisplayName("Should throw exception when deleting empty key if not present")
  void deleteEmptyKeyWhenNotPresent() {
    assertThatThrownBy(() -> trie.delete(""))
        .isInstanceOf(InvalidKeyException.class)
        .hasMessage("Key cannot be empty");
  }

  @Test
  @DisplayName("Should handle large keys (1000 chars)")
  void largeKeyOperations() {
    String key = "a".repeat(1000);
    Integer value = 123;

    trie.insert(key, value);

    assertThat(trie.search(key)).isPresent().contains(value);
    assertThat(trie.size()).isEqualTo(1);

    boolean deleted = trie.delete(key);

    assertThat(deleted).isTrue();
    assertThat(trie.search(key)).isEmpty();
    assertThat(trie.size()).isZero();
  }

  @Test
  @DisplayName("Should handle extremely large keys (5000 chars)")
  void extremelyLargeKeyOperations() {
    String key = "b".repeat(5000);
    Integer value = 999;

    trie.insert(key, value);

    assertThat(trie.search(key)).isPresent().contains(value);
    assertThat(trie.size()).isEqualTo(1);

    trie.delete(key);
    assertThat(trie.isEmpty()).isTrue();
  }

  @Test
  @DisplayName("Should return exactly limit number of results")
  void autocompleteRespectsExactLimit() {
    int totalItems = 100;
    int limit = 10;
    String prefix = "pref-";

    for (int i = 0; i < totalItems; i++) {
      trie.insert(String.format("%s%03d", prefix, i), i);
    }

    List<String> results = trie.autocomplete(prefix, limit);

    assertThat(results).hasSize(limit);
    assertThat(results).allMatch(s -> s.startsWith(prefix));
  }

  @Test
  @DisplayName("Should prune dangling nodes but keep shared prefixes on delete")
  void deletePrunesDanglingNodesButKeepsSharedPrefixes() {
    trie.insert("car", "car");
    trie.insert("cart", "cart");
    trie.insert("carbon", "carbon");
    assertThat(trie.delete("cart")).isTrue();
    assertThat(trie.has("cart")).isFalse();
    assertThat(trie.has("car")).isTrue();
    assertThat(trie.has("carbon")).isTrue();
    assertThat(trie.size()).isEqualTo(2);
  }

  @Test
  @DisplayName("Should honor limit and ordering in autocomplete")
  void autocompleteHonorsLimitAndOrdering() {
    trie.insert("alpha", 1);
    trie.insert("alpine", 2);
    trie.insert("albatross", 3);
    List<String> suggestions = trie.autocomplete("al", 2);
    assertThat(suggestions).containsExactly("albatross", "alpha");
  }

  @Test
  @DisplayName("Should reject non-positive limit in autocomplete")
  void autocompleteRejectsNonPositiveLimit() {
    assertThatThrownBy(() -> trie.autocomplete("a", 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Limit");
  }

  @Test
  @DisplayName("Should return entries with values in prefix search")
  void searchByPrefixReturnsEntriesWithValues() {
    trie.insert("color", "red");
    trie.insert("cold", "ice");
    trie.insert("heat", "fire");
    List<Map.Entry<String, Object>> entries = trie.searchByPrefix("co");
    assertThat(entries)
        .hasSize(2)
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactlyInAnyOrder(tuple("color", "red"), tuple("cold", "ice"));
  }

  @Test
  @DisplayName("Should stop longest common prefix at end of word or branch")
  void longestCommonPrefixStopsAtEndOfWordOrBranch() {
    trie.insert("flower", 1);
    trie.insert("flow", 2);
    trie.insert("flight", 3);
    assertThat(trie.longestCommonPrefix()).isEqualTo("fl");
  }

  @Test
  @DisplayName("Should reset size, state and data on clear")
  void clearResetsSizeStateAndData() {
    trie.insert("alpha", 1);
    trie.insert("beta", 2);
    trie.clear();
    assertThat(trie.isEmpty()).isTrue();
    assertThat(trie.size()).isZero();
    assertThat(trie.getAllKeys()).isEmpty();
  }

  @Test
  @DisplayName("Should correctly reflect key presence with has and startsWith")
  void hasAndStartsWithReflectsPresence() {
    trie.insert("start", 42);
    assertThat(trie.has("start")).isTrue();
    assertThat(trie.startsWith("sta")).isTrue();
    assertThat(trie.startsWith("zzz")).isFalse();
  }

  @Test
  @DisplayName("Should reject null value on insert")
  void insertingNullValueIsRejected() {
    assertThatThrownBy(() -> trie.insert("alpha", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Value cannot be null");
  }

  @Test
  @DisplayName("Should throw InvalidKeyException for null key operations")
  void nullKeyOperationsThrowInvalidKeyException() {
    assertThatThrownBy(() -> trie.insert(null, "value")).isInstanceOf(InvalidKeyException.class);
    assertThatThrownBy(() -> trie.search(null)).isInstanceOf(InvalidKeyException.class);
    assertThatThrownBy(() -> trie.delete(null)).isInstanceOf(InvalidKeyException.class);
    assertThatThrownBy(() -> trie.startsWith(null)).isInstanceOf(InvalidKeyException.class);
  }

  @Test
  @DisplayName("Should return sorted snapshot of all keys")
  void getAllKeysReturnsSortedSnapshot() {
    trie.insert("bravo", 2);
    trie.insert("alpha", 1);
    trie.insert("band", 3);
    assertThat(trie.getAllKeys()).containsExactly("alpha", "band", "bravo");
  }
}
