package com.rodina.trie.core.util;

import java.util.Map;

public class TrieEntry<V> implements Map.Entry<String, V> {
  private final String key;
  private V value;

  public TrieEntry(String key, V value) {
    this.key = key;
    this.value = value;
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public V getValue() {
    return value;
  }

  @Override
  public V setValue(V value) {
    V oldValue = this.value;
    this.value = value;
    return oldValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TrieEntry<?> trieEntry = (TrieEntry<?>) o;
    if (key != null ? !key.equals(trieEntry.key) : trieEntry.key != null) {
      return false;
    }
    return value != null ? value.equals(trieEntry.value) : trieEntry.value == null;
  }

  @Override
  public int hashCode() {
    int result = key != null ? key.hashCode() : 0;
    result = 31 * result + (value != null ? value.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return key + "=" + value;
  }
}
