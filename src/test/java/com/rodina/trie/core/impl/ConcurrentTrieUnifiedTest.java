package com.rodina.trie.core.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rodina.trie.contract.Trie;
import com.rodina.trie.exception.InvalidKeyException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

abstract class ConcurrentTrieUnifiedTest<V> {
  protected Trie<V> trie;

  protected abstract V createValue(int index);

  protected abstract V createValue(String val);

  @BeforeEach
  void setUp() {
    trie = new ConcurrentTrie<>();
  }

  @Test
  @DisplayName("Should make key searchable after insertion")
  void insertMakesKeySearchable() {
    V val = createValue("fruit");
    trie.insert("apple", val);

    Optional<V> lookup = trie.search("apple");
    assertThat(lookup).isPresent().contains(val);
    assertThat(trie.has("apple")).isTrue();
    assertThat(trie.size()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should update value when inserting existing key")
  void insertUpdatesExistingValue() {
    V val1 = createValue(1);
    V val2 = createValue(2);

    trie.insert("key", val1);
    assertThat(trie.search("key")).contains(val1);

    trie.insert("key", val2);
    assertThat(trie.search("key")).contains(val2);
  }

  @Test
  @DisplayName("Should throw exception when inserting null value")
  void insertNullValueThrowsException() {
    assertThatThrownBy(() -> trie.insert("key", null)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("Should throw InvalidKeyException when inserting null key")
  void insertNullKeyThrowsException() {
    assertThatThrownBy(() -> trie.insert(null, createValue(1)))
        .isInstanceOf(InvalidKeyException.class);
  }

  @Test
  @DisplayName("Should remove key after deletion")
  void deleteRemovesKey() {
    trie.insert("banana", createValue(1));

    boolean deleted = trie.delete("banana");

    assertThat(deleted).isTrue();
    assertThat(trie.search("banana")).isEmpty();
    assertThat(trie.size()).isZero();
    assertThat(trie.isEmpty()).isTrue();
  }

  @Test
  @DisplayName("Should return false when deleting non-existent key")
  void deleteNonExistentKeyReturnsFalse() {
    trie.insert("exist", createValue(1));
    assertThat(trie.delete("missing")).isFalse();
    assertThat(trie.size()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should keep children when deleting middle node")
  void deleteMiddleNodeKeepsChildren() {
    trie.insert("apple", createValue(1));
    trie.insert("app", createValue(2));

    assertThat(trie.delete("app")).isTrue();
    assertThat(trie.has("app")).isFalse();
    assertThat(trie.has("apple")).isTrue();
    assertThat(trie.size()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should return matching entries for prefix search")
  void prefixSearchMatchesInsertedData() {
    V v1 = createValue("c1");
    V v2 = createValue("c2");
    trie.insert("cat", v1);
    trie.insert("car", v2);
    trie.insert("dog", createValue("d1"));

    List<Map.Entry<String, V>> matches = trie.searchByPrefix("ca");

    assertThat(matches).hasSize(2);
    assertThat(matches).extracting(Map.Entry::getKey).containsExactlyInAnyOrder("cat", "car");
  }

  @Test
  @DisplayName("Should honor limit in autocomplete results")
  void autocompleteHonorsLimit() {
    trie.insert("alpha", createValue(1));
    trie.insert("alpine", createValue(2));
    trie.insert("alps", createValue(3));

    List<String> suggestions = trie.autocomplete("al", 2);

    assertThat(suggestions).hasSize(2);
    assertThat(suggestions).containsExactly("alpha", "alpine");
  }

  @Test
  @DisplayName("Should correctly calculate longest common prefix")
  void longestCommonPrefixWorks() {
    assertThat(trie.longestCommonPrefix()).isEmpty();

    trie.insert("apple", createValue(1));
    trie.insert("application", createValue(2));
    assertThat(trie.longestCommonPrefix()).isEqualTo("appl");

    trie.insert("banana", createValue(3));
    assertThat(trie.longestCommonPrefix()).isEmpty();
  }

  @Test
  @DisplayName("Should traverse all entries via iterator")
  void iteratorTraversesAllEntries() {
    trie.insert("one", createValue(1));
    trie.insert("two", createValue(2));
    int count = 0;
    for (Map.Entry<String, V> entry : trie) {
      count++;
      assertThat(entry.getKey()).isNotNull();
      assertThat(entry.getValue()).isNotNull();
    }
    assertThat(count).isEqualTo(2);
  }

  @Nested
  @DisplayName("Snapshot Tests")
  class SnapshotTests {
    @Test
    @DisplayName("Should rollback global state to snapshot")
    void rollbackRestoresState() {
      trie.insert("v1", createValue("initial"));
      long snapshotId = trie.createSnapshot();

      trie.insert("v2", createValue("modified"));
      trie.delete("v1");

      assertThat(trie.has("v1")).isFalse();
      assertThat(trie.has("v2")).isTrue();

      boolean success = trie.rollbackToSnapshot(snapshotId);

      assertThat(success).isTrue();
      assertThat(trie.has("v1")).isTrue();
      assertThat(trie.has("v2")).isFalse();
      assertThat(trie.size()).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("Integer Value Tests")
  class IntegerTest extends ConcurrentTrieUnifiedTest<Integer> {
    @Override
    protected Integer createValue(int index) {
      return index;
    }

    @Override
    protected Integer createValue(String val) {
      return val.hashCode();
    }
  }

  @Nested
  @DisplayName("String Value Tests")
  class StringTest extends ConcurrentTrieUnifiedTest<String> {
    @Override
    protected String createValue(int index) {
      return String.valueOf(index);
    }

    @Override
    protected String createValue(String val) {
      return val;
    }
  }
}
