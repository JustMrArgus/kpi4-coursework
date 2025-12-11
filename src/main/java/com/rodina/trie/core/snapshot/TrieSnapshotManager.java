package com.rodina.trie.core.snapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.rodina.trie.core.node.TrieNode;

public class TrieSnapshotManager<V> {
  private static final AtomicLong SNAPSHOT_ID_GENERATOR = new AtomicLong(0);
  private final ConcurrentHashMap<Long, TrieSnapshot<V>> snapshots;
  private final int maxSnapshots;

  public TrieSnapshotManager() {
    this(10);
  }

  public TrieSnapshotManager(int maxSnapshots) {
    this.snapshots = new ConcurrentHashMap<>();
    this.maxSnapshots = maxSnapshots;
  }

  public long createSnapshot(TrieNode<V> root, int size) {
    long snapshotId = SNAPSHOT_ID_GENERATOR.incrementAndGet();
    TrieNode<V> clonedRoot = deepClone(root);
    TrieSnapshot<V> snapshot = new TrieSnapshot<>(snapshotId, clonedRoot, size);
    snapshots.put(snapshotId, snapshot);
    cleanupOldSnapshots();
    return snapshotId;
  }

  private TrieNode<V> deepClone(TrieNode<V> node) {
    if (node == null) {
      return null;
    }
    TrieNode<V> clone = new TrieNode<>();
    clone.setValue(node.getValue());
    clone.setEndOfWord(node.isEndOfWord());
    node.lockRead();
    try {
      for (Map.Entry<Character, TrieNode<V>> entry : node.getChildrenMapDirect().entrySet()) {
        clone.setChild(entry.getKey(), deepClone(entry.getValue()));
      }
    } finally {
      node.unlockRead();
    }
    return clone;
  }

  public TrieSnapshot<V> getSnapshot(long snapshotId) {
    return snapshots.get(snapshotId);
  }

  public boolean hasSnapshot(long snapshotId) {
    return snapshots.containsKey(snapshotId);
  }

  public boolean deleteSnapshot(long snapshotId) {
    return snapshots.remove(snapshotId) != null;
  }

  public Map<Long, TrieSnapshot<V>> getAllSnapshots() {
    return new HashMap<>(snapshots);
  }

  public int getSnapshotCount() {
    return snapshots.size();
  }

  public void clearAllSnapshots() {
    snapshots.clear();
  }

  private void cleanupOldSnapshots() {
    while (snapshots.size() > maxSnapshots) {
      Long oldestId = snapshots.keySet().stream().min(Long::compare).orElse(null);
      if (oldestId != null) {
        snapshots.remove(oldestId);
      }
    }
  }

  public Long getLatestSnapshotId() {
    return snapshots.keySet().stream().max(Long::compare).orElse(null);
  }
}
