package com.rodina.trie.contract;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.rodina.trie.core.snapshot.TrieSnapshot;

public interface Trie<V> extends Iterable<Map.Entry<String, V>> {
  void insert(String key, V value);

  Optional<V> search(String key);

  boolean delete(String key);

  boolean has(String key);

  boolean startsWith(String prefix);

  List<Map.Entry<String, V>> searchByPrefix(String prefix);

  List<String> autocomplete(String prefix, int limit);

  int size();

  boolean isEmpty();

  void clear();

  List<String> getAllKeys();

  String longestCommonPrefix();

  @Override
  Iterator<Map.Entry<String, V>> iterator();

  long getGlobalVersion();

  long createSnapshot();

  boolean rollbackToSnapshot(long snapshotId);

  boolean rollbackNode(String key);

  boolean rollbackNodeToVersion(String key, long version);

  Map<Long, TrieSnapshot<V>> getSnapshots();

  boolean deleteSnapshot(long snapshotId);

  int getSnapshotCount();

  void clearAllSnapshots();

  void clearNodeSnapshotHistory();
}
