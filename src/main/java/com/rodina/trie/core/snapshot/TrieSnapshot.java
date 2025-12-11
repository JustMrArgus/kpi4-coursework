package com.rodina.trie.core.snapshot;

import com.rodina.trie.core.node.TrieNode;

public class TrieSnapshot<V> {
  private final long id;
  private final long timestamp;
  private final TrieNode<V> root;
  private final int size;
  private final String description;

  public TrieSnapshot(long id, TrieNode<V> root, int size) {
    this(id, root, size, null);
  }

  public TrieSnapshot(long id, TrieNode<V> root, int size, String description) {
    this.id = id;
    this.timestamp = System.currentTimeMillis();
    this.root = root;
    this.size = size;
    this.description = description;
  }

  public long getId() {
    return id;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public TrieNode<V> getRoot() {
    return root;
  }

  public int getSize() {
    return size;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public String toString() {
    return "TrieSnapshot{"
        + "id="
        + id
        + ", timestamp="
        + timestamp
        + ", size="
        + size
        + ", description='"
        + description
        + '\''
        + '}';
  }
}
