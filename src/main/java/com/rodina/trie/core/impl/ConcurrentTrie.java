package com.rodina.trie.core.impl;

import com.rodina.trie.contract.Trie;
import com.rodina.trie.core.node.TrieNode;
import com.rodina.trie.core.snapshot.TrieSnapshot;
import com.rodina.trie.core.snapshot.TrieSnapshotManager;
import com.rodina.trie.core.util.TrieIterator;
import com.rodina.trie.exception.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ConcurrentTrie<V> implements Trie<V> {
  private final TrieNode<V> root;
  private final AtomicInteger size;
  private final TrieSnapshotManager<V> snapshotManager;
  private final AtomicLong globalVersion;

  public ConcurrentTrie() {
    this(10);
  }

  public ConcurrentTrie(int maxSnapshots) {
    if (maxSnapshots <= 0) {
      throw new IllegalArgumentException("Max snapshots must be greater than 0");
    }
    this.root = new TrieNode<>();
    this.size = new AtomicInteger(0);
    this.snapshotManager = new TrieSnapshotManager<>(maxSnapshots);
    this.globalVersion = new AtomicLong(0);
  }

  @Override
  public void insert(String key, V value) {
    validateKey(key);
    if (value == null) {
      throw new IllegalArgumentException("Value cannot be null");
    }
    TrieNode<V> currentNode = root;
    currentNode.lockRead();
    try {
      for (int i = 0; i < key.length(); i++) {
        char charCode = key.charAt(i);
        TrieNode<V> nextNode = currentNode.getChild(charCode);
        if (nextNode == null) {
          currentNode.unlockRead();
          currentNode.lockWrite();
          try {
            nextNode = currentNode.getChild(charCode);
            if (nextNode == null) {
              nextNode = currentNode.addChild(charCode);
            }
          } finally {
            currentNode.readLockFromWriteLock();
            currentNode.unlockWrite();
          }
        }
        nextNode.lockRead();
        currentNode.unlockRead();
        currentNode = nextNode;
      }
      currentNode.unlockRead();
      currentNode.lockWrite();
      try {
        if (!currentNode.isEndOfWord()) {
          currentNode.setEndOfWord(true);
          size.incrementAndGet();
        }
        currentNode.setValue(value);
      } finally {
        currentNode.unlockWrite();
      }
    } finally {
      try {
        currentNode.unlockRead();
      } catch (IllegalMonitorStateException e) {
      }
    }
  }

  @Override
  public Optional<V> search(String key) {
    validateKey(key);
    TrieNode<V> currentNode = root;
    currentNode.lockRead();
    try {
      for (int i = 0; i < key.length(); i++) {
        char charCode = key.charAt(i);
        TrieNode<V> nextNode = currentNode.getChild(charCode);
        if (nextNode == null) {
          return Optional.empty();
        }
        nextNode.lockRead();
        currentNode.unlockRead();
        currentNode = nextNode;
      }
      if (currentNode.isEndOfWord()) {
        return Optional.ofNullable(currentNode.getValue());
      } else {
        return Optional.empty();
      }
    } finally {
      currentNode.unlockRead();
    }
  }

  @Override
  public boolean delete(String key) {
    validateKey(key);
    Stack<TrieNode<V>> nodeStack = new Stack<>();
    TrieNode<V> currentNode = root;
    currentNode.lockRead();
    nodeStack.push(currentNode);
    try {
      for (int i = 0; i < key.length(); i++) {
        char charCode = key.charAt(i);
        TrieNode<V> nextNode = currentNode.getChild(charCode);
        if (nextNode == null) {
          releaseLocks(nodeStack);
          return false;
        }
        nextNode.lockRead();
        currentNode = nextNode;
        nodeStack.push(currentNode);
      }
      TrieNode<V> targetNode = nodeStack.peek();
      targetNode.unlockRead();
      targetNode.lockWrite();
      boolean deleted = false;
      try {
        if (targetNode.isEndOfWord()) {
          targetNode.setEndOfWord(false);
          targetNode.setValue(null);
          size.decrementAndGet();
          deleted = true;
        }
      } finally {
        targetNode.readLockFromWriteLock();
        targetNode.unlockWrite();
      }
      if (!deleted) {
        releaseLocks(nodeStack);
        return false;
      }
      cleanUpNodes(key, nodeStack);
      return true;
    } catch (Exception e) {
      releaseLocks(nodeStack);
      throw e;
    }
  }

  private void cleanUpNodes(String key, Stack<TrieNode<V>> nodeStack) {
    if (nodeStack.isEmpty()) return;
    TrieNode<V> child = nodeStack.pop();
    int charIndex = key.length() - 1;
    while (!nodeStack.isEmpty() && charIndex >= 0) {
      TrieNode<V> parent = nodeStack.peek();
      char charCode = key.charAt(charIndex);
      child.unlockRead();
      child.lockWrite();
      boolean canDeleteChild = false;
      try {
        if (!child.isEndOfWord() && !child.hasChildren()) {
          canDeleteChild = true;
        }
      } finally {
        child.unlockWrite();
      }
      if (canDeleteChild) {
        parent.unlockRead();
        parent.lockWrite();
        try {
          TrieNode<V> currentChild = parent.getChild(charCode);
          if (currentChild != null) {
            currentChild.lockRead();
            try {
              if (!currentChild.isEndOfWord() && !currentChild.hasChildren()) {
                parent.removeChild(charCode);
              }
            } finally {
              currentChild.unlockRead();
            }
          }
        } finally {
          parent.readLockFromWriteLock();
          parent.unlockWrite();
        }
      } else {
        break;
      }
      child = nodeStack.pop();
      charIndex--;
    }
    releaseLocks(nodeStack);
    if (child != null) {
      try {
        child.unlockRead();
      } catch (IllegalMonitorStateException e) {
      }
    }
  }

  private void releaseLocks(Stack<TrieNode<V>> stack) {
    while (!stack.isEmpty()) {
      TrieNode<V> node = stack.pop();
      try {
        node.unlockRead();
      } catch (IllegalMonitorStateException e) {
      }
    }
  }

  @Override
  public boolean has(String key) {
    return search(key).isPresent();
  }

  @Override
  public boolean startsWith(String prefix) {
    validatePrefix(prefix);
    TrieNode<V> node = findNode(prefix);
    return node != null;
  }

  @Override
  public List<Map.Entry<String, V>> searchByPrefix(String prefix) {
    validatePrefix(prefix);
    TrieNode<V> prefixRoot = findNode(prefix);
    if (prefixRoot == null) {
      return Collections.emptyList();
    }
    List<Map.Entry<String, V>> results = new ArrayList<>();
    TrieIterator<V> iterator = new TrieIterator<>(prefixRoot, prefix);
    while (iterator.hasNext()) {
      results.add(iterator.next());
    }
    return results;
  }

  @Override
  public List<String> autocomplete(String prefix, int limit) {
    validatePrefix(prefix);
    if (limit <= 0) {
      throw new IllegalArgumentException("Limit must be greater than 0");
    }
    TrieNode<V> prefixRoot = findNode(prefix);
    if (prefixRoot == null) {
      return Collections.emptyList();
    }
    List<String> results = new ArrayList<>();
    TrieIterator<V> iterator = new TrieIterator<>(prefixRoot, prefix);
    while (iterator.hasNext() && results.size() < limit) {
      results.add(iterator.next().getKey());
    }
    return results;
  }

  private TrieNode<V> findNode(String prefix) {
    TrieNode<V> currentNode = root;
    currentNode.lockRead();
    try {
      for (int i = 0; i < prefix.length(); i++) {
        char charCode = prefix.charAt(i);
        TrieNode<V> nextNode = currentNode.getChild(charCode);
        if (nextNode == null) {
          return null;
        }
        nextNode.lockRead();
        currentNode.unlockRead();
        currentNode = nextNode;
      }
      return currentNode;
    } finally {
      try {
        currentNode.unlockRead();
      } catch (IllegalMonitorStateException e) {
      }
    }
  }

  @Override
  public int size() {
    return size.get();
  }

  @Override
  public boolean isEmpty() {
    return size.get() == 0;
  }

  @Override
  public void clear() {
    root.lockWrite();
    try {
      root.clear();
      size.set(0);
    } finally {
      root.unlockWrite();
    }
  }

  @Override
  public List<String> getAllKeys() {
    return searchByPrefix("").stream().map(Map.Entry::getKey).collect(Collectors.toList());
  }

  @Override
  public String longestCommonPrefix() {
    TrieNode<V> currentNode = root;
    StringBuilder prefix = new StringBuilder();
    while (true) {
      currentNode.lockRead();
      try {
        if (currentNode.getChildrenCount() != 1 || currentNode.isEndOfWord()) {
          return prefix.toString();
        }
        Iterator<Character> it = currentNode.getChildrenKeys().iterator();
        if (!it.hasNext()) {
          return prefix.toString();
        }
        char nextChar = it.next();
        prefix.append(nextChar);
        TrieNode<V> nextNode = currentNode.getChild(nextChar);
        if (nextNode == null) {
          return prefix.substring(0, prefix.length() - 1);
        }
        nextNode.lockRead();
        currentNode.unlockRead();
        currentNode = nextNode;
      } finally {
        try {
          currentNode.unlockRead();
        } catch (Exception e) {
        }
      }
    }
  }

  @Override
  public Iterator<Map.Entry<String, V>> iterator() {
    return new TrieIterator<>(root);
  }

  private void validateKey(String key) {
    if (key == null) {
      throw new InvalidKeyException("Key cannot be null");
    }
    if (key.isEmpty()) {
      throw new InvalidKeyException("Key cannot be empty");
    }
  }

  private void validatePrefix(String prefix) {
    if (prefix == null) {
      throw new InvalidKeyException("Prefix cannot be null");
    }
  }

  protected TrieNode<V> getRoot() {
    return root;
  }

  @Override
  public long getGlobalVersion() {
    return globalVersion.get();
  }

  private void incrementGlobalVersion() {
    globalVersion.incrementAndGet();
  }

  @Override
  public long createSnapshot() {
    root.lockRead();
    try {
      return snapshotManager.createSnapshot(root, size.get());
    } finally {
      root.unlockRead();
    }
  }

  @Override
  public boolean rollbackToSnapshot(long snapshotId) {
    TrieSnapshot<V> snapshot = snapshotManager.getSnapshot(snapshotId);
    if (snapshot == null) {
      return false;
    }
    root.lockWrite();
    try {
      root.clear();
      if (snapshot.getRoot() != null) {
        copyFromSnapshot(root, snapshot.getRoot());
      }
      size.set(snapshot.getSize());
      incrementGlobalVersion();
      return true;
    } finally {
      root.unlockWrite();
    }
  }

  private void copyFromSnapshot(TrieNode<V> target, TrieNode<V> source) {
    if (source == null || target == null) {
      return;
    }
    target.setValue(source.getValue());
    target.setEndOfWord(source.isEndOfWord());

    Map<Character, TrieNode<V>> children = source.getChildrenMapDirect();
    if (children != null) {
      for (Map.Entry<Character, TrieNode<V>> entry : children.entrySet()) {
        TrieNode<V> childClone = new TrieNode<>();
        copyFromSnapshot(childClone, entry.getValue());
        target.setChild(entry.getKey(), childClone);
      }
    }
  }

  @Override
  public boolean rollbackNode(String key) {
    validateKey(key);
    TrieNode<V> node = findNodeForWrite(key);
    if (node == null) {
      return false;
    }
    node.lockWrite();
    try {
      boolean result = node.rollback();
      if (result) {
        incrementGlobalVersion();
      }
      return result;
    } finally {
      node.unlockWrite();
    }
  }

  @Override
  public boolean rollbackNodeToVersion(String key, long version) {
    validateKey(key);
    if (version < 0) {
      throw new IllegalArgumentException("Version cannot be negative");
    }
    TrieNode<V> node = findNodeForWrite(key);
    if (node == null) {
      return false;
    }
    node.lockWrite();
    try {
      boolean result = node.rollbackToVersion(version);
      if (result) {
        incrementGlobalVersion();
      }
      return result;
    } finally {
      node.unlockWrite();
    }
  }

  private TrieNode<V> findNodeForWrite(String prefix) {
    TrieNode<V> currentNode = root;
    for (int i = 0; i < prefix.length(); i++) {
      char charCode = prefix.charAt(i);
      TrieNode<V> nextNode = currentNode.getChild(charCode);
      if (nextNode == null) {
        return null;
      }
      currentNode = nextNode;
    }
    return currentNode;
  }

  @Override
  public Map<Long, TrieSnapshot<V>> getSnapshots() {
    return snapshotManager.getAllSnapshots();
  }

  @Override
  public boolean deleteSnapshot(long snapshotId) {
    return snapshotManager.deleteSnapshot(snapshotId);
  }

  @Override
  public int getSnapshotCount() {
    return snapshotManager.getSnapshotCount();
  }

  @Override
  public void clearAllSnapshots() {
    snapshotManager.clearAllSnapshots();
  }

  @Override
  public void clearNodeSnapshotHistory() {
    clearNodeSnapshotHistoryRecursive(root);
  }

  private void clearNodeSnapshotHistoryRecursive(TrieNode<V> node) {
    if (node == null) return;
    node.lockWrite();
    try {
      node.clearSnapshotHistory();
      Map<Character, TrieNode<V>> children = node.getChildrenMapDirect();
      if (children != null) {
        for (TrieNode<V> child : children.values()) {
          clearNodeSnapshotHistoryRecursive(child);
        }
      }
    } finally {
      node.unlockWrite();
    }
  }
}
