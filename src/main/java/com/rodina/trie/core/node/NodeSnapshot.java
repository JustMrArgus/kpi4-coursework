package com.rodina.trie.core.node;

import java.util.HashMap;
import java.util.Map;

public class NodeSnapshot<V> {
  private final long version;
  private final long timestamp;
  private final V value;
  private final boolean isEndOfWord;
  private final Map<Character, TrieNode<V>> childrenSnapshot;
  private final NodeSnapshot<V> previousSnapshot;

  public NodeSnapshot(TrieNode<V> node, NodeSnapshot<V> previousSnapshot) {
    this.version = node.getVersion();
    this.timestamp = System.currentTimeMillis();
    this.value = node.getValue();
    this.isEndOfWord = node.isEndOfWord();
    this.childrenSnapshot = new HashMap<>(node.getChildrenMapDirect());
    this.previousSnapshot = previousSnapshot;
  }

  public NodeSnapshot(
      long version,
      V value,
      boolean isEndOfWord,
      Map<Character, TrieNode<V>> children,
      NodeSnapshot<V> previousSnapshot) {
    this.version = version;
    this.timestamp = System.currentTimeMillis();
    this.value = value;
    this.isEndOfWord = isEndOfWord;
    this.childrenSnapshot = new HashMap<>(children);
    this.previousSnapshot = previousSnapshot;
  }

  public long getVersion() {
    return version;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public V getValue() {
    return value;
  }

  public boolean isEndOfWord() {
    return isEndOfWord;
  }

  public Map<Character, TrieNode<V>> getChildrenSnapshot() {
    return new HashMap<>(childrenSnapshot);
  }

  public NodeSnapshot<V> getPreviousSnapshot() {
    return previousSnapshot;
  }

  public boolean isBeforeVersion(long targetVersion) {
    return this.version < targetVersion;
  }

  public boolean isBeforeTimestamp(long targetTimestamp) {
    return this.timestamp < targetTimestamp;
  }

  @Override
  public String toString() {
    return "NodeSnapshot{"
        + "version="
        + version
        + ", timestamp="
        + timestamp
        + ", isEndOfWord="
        + isEndOfWord
        + ", value="
        + value
        + ", childrenCount="
        + childrenSnapshot.size()
        + '}';
  }
}
