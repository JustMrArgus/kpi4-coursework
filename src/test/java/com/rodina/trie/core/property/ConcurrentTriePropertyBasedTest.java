package com.rodina.trie.core.property;

import static org.assertj.core.api.Assertions.assertThat;

import com.rodina.trie.contract.Trie;
import com.rodina.trie.core.impl.ConcurrentTrie;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;

@Label("ConcurrentTrie Property-Based Tests")
class ConcurrentTriePropertyBasedTest {
  @Property
  @Label("Unique insertions should increase trie size")
  boolean uniqueInsertionsIncreaseSize(
      @ForAll @StringLength(min = 1, max = 20) @AlphaChars String key, @ForAll int value) {
    Trie<Integer> trie = new ConcurrentTrie<>();
    int initialSize = trie.size();
    trie.insert(key, value);
    if (initialSize == 0) {
      return trie.size() == 1;
    }
    return trie.size() >= 1;
  }

  @Property
  @Label("Trie should be consistent with map operations")
  void mapConsistency(@ForAll Map<String, Integer> inputMap) {
    Trie<Integer> trie = new ConcurrentTrie<>();
    for (Map.Entry<String, Integer> entry : inputMap.entrySet()) {
      if (entry.getKey() != null && !entry.getKey().isEmpty()) {
        trie.insert(entry.getKey(), entry.getValue());
      }
    }
    Map<String, Integer> cleanInput = new HashMap<>();
    inputMap.forEach(
        (k, v) -> {
          if (k != null && !k.isEmpty()) {
            cleanInput.put(k, v);
          }
        });
    if (cleanInput.size() != trie.size()) {
      throw new AssertionError(
          "Size mismatch. Expected: " + cleanInput.size() + ", Actual: " + trie.size());
    }
    for (String key : cleanInput.keySet()) {
      if (!trie.search(key).isPresent()) {
        throw new AssertionError("Key not found: " + key);
      }
      if (!trie.search(key).get().equals(cleanInput.get(key))) {
        throw new AssertionError("Value mismatch for key: " + key);
      }
    }
  }

  @Property
  @Label("Prefix search should return only keys starting with prefix")
  void prefixSearchConsistency(
      @ForAll List<String> keys,
      @ForAll @StringLength(min = 1, max = 3) @AlphaChars String prefix) {
    Trie<String> trie = new ConcurrentTrie<>();
    Set<String> addedKeys = new HashSet<>();
    for (String key : keys) {
      if (key != null && !key.isEmpty()) {
        trie.insert(key, key);
        addedKeys.add(key);
      }
    }
    List<Map.Entry<String, String>> results = trie.searchByPrefix(prefix);
    long expectedCount = addedKeys.stream().filter(k -> k.startsWith(prefix)).count();
    if (results.size() != expectedCount) {
      throw new AssertionError(
          "Prefix count mismatch. Expected: " + expectedCount + ", Actual: " + results.size());
    }
    for (Map.Entry<String, String> entry : results) {
      if (!entry.getKey().startsWith(prefix)) {
        throw new AssertionError("Result " + entry.getKey() + " does not start with " + prefix);
      }
    }
  }

  @Property
  @Label("Delete should remove keys and eventually empty the trie")
  void deleteConsistency(@ForAll Map<String, Integer> inputMap) {
    Trie<Integer> trie = new ConcurrentTrie<>();
    Map<String, Integer> cleanInput = new HashMap<>();
    inputMap.forEach(
        (k, v) -> {
          if (k != null && !k.isEmpty()) {
            trie.insert(k, v);
            cleanInput.put(k, v);
          }
        });
    for (String key : cleanInput.keySet()) {
      boolean deleted = trie.delete(key);
      if (!deleted) {
        throw new AssertionError("Failed to delete key: " + key);
      }
      if (trie.search(key).isPresent()) {
        throw new AssertionError("Key still exists after delete: " + key);
      }
    }
    if (!trie.isEmpty() || trie.size() != 0) {
      throw new AssertionError("Trie is not empty after deleting all keys. Size: " + trie.size());
    }
  }

  @Property
  @Label("Autocomplete should respect result limit")
  void autocompleteLimits(
      @ForAll List<String> keys, @ForAll @IntRange(min = 1, max = 100) int limit) {
    Trie<String> trie = new ConcurrentTrie<>();
    for (String key : keys) {
      if (key != null && !key.isEmpty()) {
        trie.insert(key, key);
      }
    }
    String prefix = "";
    List<String> result = trie.autocomplete(prefix, limit);
    if (result.size() > limit) {
      throw new AssertionError(
          "Autocomplete returned more items than limit. Limit: "
              + limit
              + ", Actual: "
              + result.size());
    }
  }

  @Property
  @Label("Trie maintains integrity with deep branches (up to 1200 chars)")
  void verifyLargeKeyInvariants(@ForAll @StringLength(min = 1, max = 1200) String key) {
    Trie<String> trie = new ConcurrentTrie<>();
    String value = "value";

    trie.insert(key, value);

    assertThat(trie.search(key)).isPresent().contains(value);
    assertThat(trie.has(key)).isTrue();

    boolean deleted = trie.delete(key);

    assertThat(deleted).isTrue();
    assertThat(trie.search(key)).isEmpty();
    assertThat(trie.has(key)).isFalse();
  }
}
