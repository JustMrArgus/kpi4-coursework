package com.rodina.trie.core.node;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TrieNode<V> {
  private static final AtomicLong VERSION_GENERATOR = new AtomicLong(0);
  private final ReadWriteLock lock;
  private final Lock readLock;
  private final Lock writeLock;
  private final Map<Character, TrieNode<V>> children;
  private V value;
  private boolean isEndOfWord;
  private long version;
  private long createdAt;
  private long modifiedAt;
  private final AtomicLong accessCount;
  private NodeSnapshot<V> previousSnapshot;

  public TrieNode() {
    this.lock = new ReentrantReadWriteLock();
    this.readLock = this.lock.readLock();
    this.writeLock = this.lock.writeLock();
    this.children = new HashMap<>();
    this.value = null;
    this.isEndOfWord = false;
    this.version = VERSION_GENERATOR.incrementAndGet();
    this.createdAt = System.currentTimeMillis();
    this.modifiedAt = this.createdAt;
    this.accessCount = new AtomicLong(0);
    this.previousSnapshot = null;
  }

  public void lockRead() {
    this.readLock.lock();
  }

  public void unlockRead() {
    this.readLock.unlock();
  }

  public void lockWrite() {
    this.writeLock.lock();
  }

  public void unlockWrite() {
    this.writeLock.unlock();
  }

  public void readLockFromWriteLock() {
    this.readLock.lock();
  }

  public V getValue() {
    return value;
  }

  public void setValue(V value) {
    saveSnapshot();
    this.value = value;
    updateModified();
  }

  public boolean isEndOfWord() {
    return isEndOfWord;
  }

  public void setEndOfWord(boolean endOfWord) {
    if (this.isEndOfWord != endOfWord) {
      saveSnapshot();
      this.isEndOfWord = endOfWord;
      updateModified();
    }
  }

  public long getVersion() {
    return version;
  }

  public long getCreatedAt() {
    return createdAt;
  }

  public long getModifiedAt() {
    return modifiedAt;
  }

  private void updateModified() {
    this.modifiedAt = System.currentTimeMillis();
    this.version = VERSION_GENERATOR.incrementAndGet();
  }

  public void saveSnapshot() {
    this.previousSnapshot =
        new NodeSnapshot<>(
            this.version, this.value, this.isEndOfWord, this.children, this.previousSnapshot);
  }

  public boolean rollback() {
    if (previousSnapshot == null) {
      return false;
    }
    this.value = previousSnapshot.getValue();
    this.isEndOfWord = previousSnapshot.isEndOfWord();
    this.children.clear();
    this.children.putAll(previousSnapshot.getChildrenSnapshot());
    this.previousSnapshot = previousSnapshot.getPreviousSnapshot();
    updateModified();
    return true;
  }

  public boolean rollbackToVersion(long targetVersion) {
    NodeSnapshot<V> snapshot = previousSnapshot;
    while (snapshot != null && snapshot.getVersion() > targetVersion) {
      snapshot = snapshot.getPreviousSnapshot();
    }
    if (snapshot == null || snapshot.getVersion() != targetVersion) {
      return false;
    }
    this.value = snapshot.getValue();
    this.isEndOfWord = snapshot.isEndOfWord();
    this.children.clear();
    this.children.putAll(snapshot.getChildrenSnapshot());
    this.previousSnapshot = snapshot.getPreviousSnapshot();
    updateModified();
    return true;
  }

  public boolean hasPreviousSnapshot() {
    return previousSnapshot != null;
  }

  public int getSnapshotDepth() {
    int depth = 0;
    NodeSnapshot<V> snapshot = previousSnapshot;
    while (snapshot != null) {
      depth++;
      snapshot = snapshot.getPreviousSnapshot();
    }
    return depth;
  }

  public void clearSnapshotHistory() {
    this.previousSnapshot = null;
  }

  public void incrementAccessCount() {
    this.accessCount.incrementAndGet();
  }

  public long getAccessCount() {
    return this.accessCount.get();
  }

  public void resetAccessCount() {
    this.accessCount.set(0);
  }

  public Map<Character, TrieNode<V>> getChildrenMapDirect() {
    return this.children;
  }

  public TrieNode<V> getChild(char character) {
    return this.children.get(character);
  }

  public TrieNode<V> addChild(char character) {
    if (!this.children.containsKey(character)) {
      this.children.put(character, new TrieNode<>());
    }
    return this.children.get(character);
  }

  public void setChild(char character, TrieNode<V> node) {
    this.children.put(character, node);
  }

  public void removeChild(char character) {
    this.children.remove(character);
  }

  public boolean hasChild(char character) {
    return this.children.containsKey(character);
  }

  public boolean hasChildren() {
    return !this.children.isEmpty();
  }

  public int getChildrenCount() {
    return this.children.size();
  }

  public Set<Character> getChildrenKeys() {
    return this.children.keySet();
  }

  public Map<Character, TrieNode<V>> getChildrenMap() {
    lockRead();
    try {
      return new HashMap<>(this.children);
    } finally {
      unlockRead();
    }
  }

  public void clear() {
    saveSnapshot();
    this.children.clear();
    this.value = null;
    this.isEndOfWord = false;
    updateModified();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("TrieNode{");
    builder.append("version=").append(version);
    builder.append(", isEndOfWord=").append(isEndOfWord);
    builder.append(", value=").append(value);
    builder.append(", childrenCount=").append(children.size());
    builder.append(", accessCount=").append(accessCount.get());
    builder.append('}');
    return builder.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TrieNode<?> trieNode = (TrieNode<?>) o;
    if (isEndOfWord != trieNode.isEndOfWord) return false;
    if (value != null ? !value.equals(trieNode.value) : trieNode.value != null) return false;
    return children.equals(trieNode.children);
  }

  @Override
  public int hashCode() {
    int result = children.hashCode();
    result = 31 * result + (value != null ? value.hashCode() : 0);
    result = 31 * result + (isEndOfWord ? 1 : 0);
    return result;
  }
}
