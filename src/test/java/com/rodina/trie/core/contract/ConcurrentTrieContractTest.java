package com.rodina.trie.core.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rodina.trie.contract.Trie;
import com.rodina.trie.core.impl.ConcurrentTrie;
import com.rodina.trie.exception.InvalidKeyException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Concurrent Trie Contract Tests")
class ConcurrentTrieContractTest {
  private Trie<String> trie;

  @BeforeEach
  void setUp() {
    trie = new ConcurrentTrie<>();
  }

  @Test
  @DisplayName("Should make key searchable after insertion")
  void insertMakesKeySearchable() {
    trie.insert("apple", "fruit");

    Optional<String> lookup = trie.search("apple");
    assertThat(lookup).isPresent().contains("fruit");
    assertThat(trie.has("apple")).isTrue();
    assertThat(trie.size()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should throw exception when inserting null value")
  void insertNullValueThrowsException() {
    assertThatThrownBy(() -> trie.insert("key", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Value cannot be null");
  }

  @Test
  @DisplayName("Should throw exception when inserting null key")
  void insertNullKeyThrowsException() {
    assertThatThrownBy(() -> trie.insert(null, "value"))
        .isInstanceOf(InvalidKeyException.class)
        .hasMessage("Key cannot be null");
  }

  @Test
  @DisplayName("Should remove key after deletion")
  void deleteRemovesKey() {
    trie.insert("banana", "yellow");

    boolean deleted = trie.delete("banana");

    assertThat(deleted).isTrue();
    assertThat(trie.search("banana")).isEmpty();
    assertThat(trie.size()).isZero();
    assertThat(trie.isEmpty()).isTrue();
  }

  @Test
  @DisplayName("Should return false when deleting non-existent key")
  void deleteNonExistentKeyReturnsFalse() {
    trie.insert("exist", "val");
    boolean deleted = trie.delete("missing");
    assertThat(deleted).isFalse();
    assertThat(trie.size()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should return matching entries for prefix search")
  void prefixSearchMatchesInsertedData() {
    trie.insert("cat", "c1");
    trie.insert("car", "c2");
    trie.insert("dog", "d1");

    List<Map.Entry<String, String>> matches = trie.searchByPrefix("ca");

    assertThat(matches)
        .hasSize(2)
        .extracting(Map.Entry::getKey)
        .containsExactlyInAnyOrder("cat", "car");
  }

  @Test
  @DisplayName("Should honor limit in autocomplete results")
  void autocompleteHonorsLimit() {
    trie.insert("alpha", "a");
    trie.insert("alpine", "b");
    trie.insert("alps", "c");

    List<String> suggestions = trie.autocomplete("al", 2);

    assertThat(suggestions).hasSize(2);
    assertThat(suggestions).allMatch(s -> s.startsWith("al"));
  }

  @Test
  @DisplayName("Should return all inserted keys")
  void getAllKeysReturnsAllData() {
    trie.insert("one", "1");
    trie.insert("two", "2");
    trie.insert("three", "3");

    List<String> keys = trie.getAllKeys();

    assertThat(keys).hasSize(3).containsExactlyInAnyOrder("one", "two", "three");
  }

  @Test
  @DisplayName("Should calculate longest common prefix correctly")
  void longestCommonPrefixWorks() {
    assertThat(trie.longestCommonPrefix()).isEmpty();

    trie.insert("apple", "1");
    trie.insert("application", "2");
    assertThat(trie.longestCommonPrefix()).isEqualTo("appl");

    trie.insert("banana", "3");
    assertThat(trie.longestCommonPrefix()).isEmpty();
  }

  @Nested
  @DisplayName("Empty State Tests")
  class EmptyState {
    @Test
    @DisplayName("Should empty trie after clear operation")
    void clearEmptiesTrie() {
      trie.insert("alpha", "1");
      trie.insert("beta", "2");

      trie.clear();

      assertThat(trie.isEmpty()).isTrue();
      assertThat(trie.size()).isZero();
      assertThat(trie.getAllKeys()).isEmpty();
    }

    @Test
    @DisplayName("Should correctly reflect entry count in size")
    void sizeReflectsEntries() {
      assertThat(trie.size()).isZero();
      trie.insert("one", "1");
      assertThat(trie.size()).isEqualTo(1);
      trie.insert("two", "2");
      assertThat(trie.size()).isEqualTo(2);
    }
  }

  @Nested
  @DisplayName("Snapshot & Rollback Contract Tests")
  class SnapshotContractTests {
    @Test
    @DisplayName("Should rollback global state to snapshot")
    void rollbackRestoresState() {
      trie.insert("v1", "initial");
      long snapshotId = trie.createSnapshot();

      trie.insert("v2", "modified");
      trie.delete("v1");

      assertThat(trie.has("v1")).isFalse();
      assertThat(trie.has("v2")).isTrue();

      boolean success = trie.rollbackToSnapshot(snapshotId);

      assertThat(success).isTrue();
      assertThat(trie.has("v1")).isTrue();
      assertThat(trie.has("v2")).isFalse();
      assertThat(trie.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should manage snapshots correctly")
    void manageSnapshots() {
      trie.insert("a", "1");
      long id1 = trie.createSnapshot();

      trie.insert("b", "2");
      long id2 = trie.createSnapshot();

      assertThat(trie.getSnapshotCount()).isEqualTo(2);
      assertThat(trie.getSnapshots()).containsKeys(id1, id2);

      trie.deleteSnapshot(id1);
      assertThat(trie.getSnapshotCount()).isEqualTo(1);
      assertThat(trie.getSnapshots()).doesNotContainKey(id1);
    }
  }
}
