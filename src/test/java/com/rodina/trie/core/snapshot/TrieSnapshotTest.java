package com.rodina.trie.core.snapshot;

import static org.assertj.core.api.Assertions.assertThat;

import com.rodina.trie.core.impl.ConcurrentTrie;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Trie Snapshot Tests")
class TrieSnapshotTest {
  private ConcurrentTrie<String> trie;

  @BeforeEach
  void setUp() {
    trie = new ConcurrentTrie<>(5);
  }

  @Nested
  @DisplayName("Snapshot Creation Tests")
  class SnapshotCreation {
    @Test
    @DisplayName("Should create snapshot of empty trie")
    void createsSnapshotOfEmptyTrie() {
      long snapshotId = trie.createSnapshot();
      assertThat(snapshotId).isPositive();
      assertThat(trie.getSnapshotCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should create snapshot with existing data")
    void createsSnapshotWithData() {
      trie.insert("apple", "fruit");
      trie.insert("banana", "fruit");
      trie.insert("cherry", "fruit");
      long snapshotId = trie.createSnapshot();
      assertThat(snapshotId).isPositive();
      assertThat(trie.getSnapshotCount()).isEqualTo(1);
      Map<Long, TrieSnapshot<String>> snapshots = trie.getSnapshots();
      assertThat(snapshots).containsKey(snapshotId);
      assertThat(snapshots.get(snapshotId).getSize()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should create multiple snapshots with incrementing IDs")
    void createsMultipleSnapshots() {
      trie.insert("a", "1");
      long snapshot1 = trie.createSnapshot();
      trie.insert("b", "2");
      long snapshot2 = trie.createSnapshot();
      trie.insert("c", "3");
      long snapshot3 = trie.createSnapshot();
      assertThat(trie.getSnapshotCount()).isEqualTo(3);
      assertThat(snapshot1).isLessThan(snapshot2);
      assertThat(snapshot2).isLessThan(snapshot3);
    }

    @Test
    @DisplayName("Should remove old snapshots when limit is exceeded")
    void removesOldSnapshotsWhenLimitExceeded() {
      for (int i = 0; i < 7; i++) {
        trie.insert("key" + i, "value" + i);
        trie.createSnapshot();
      }
      assertThat(trie.getSnapshotCount()).isEqualTo(5);
    }
  }

  @Nested
  @DisplayName("Snapshot Rollback Tests")
  class SnapshotRollback {
    @Test
    @DisplayName("Should restore previous state on rollback")
    void rollbackRestoresPreviousState() {
      trie.insert("apple", "fruit");
      trie.insert("banana", "fruit");
      long snapshotId = trie.createSnapshot();
      trie.insert("cherry", "fruit");
      trie.insert("date", "fruit");
      assertThat(trie.size()).isEqualTo(4);
      boolean result = trie.rollbackToSnapshot(snapshotId);
      assertThat(result).isTrue();
      assertThat(trie.size()).isEqualTo(2);
      assertThat(trie.has("apple")).isTrue();
      assertThat(trie.has("banana")).isTrue();
      assertThat(trie.has("cherry")).isFalse();
      assertThat(trie.has("date")).isFalse();
    }

    @Test
    @DisplayName("Should return false when rolling back to non-existent snapshot")
    void rollbackToNonExistentSnapshotReturnsFalse() {
      trie.insert("apple", "fruit");
      boolean result = trie.rollbackToSnapshot(99999L);
      assertThat(result).isFalse();
      assertThat(trie.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should restore original values on rollback")
    void rollbackRestoresValues() {
      trie.insert("key", "original");
      long snapshotId = trie.createSnapshot();
      trie.insert("key", "modified");
      assertThat(trie.search("key")).contains("modified");
      trie.rollbackToSnapshot(snapshotId);
      assertThat(trie.search("key")).contains("original");
    }

    @Test
    @DisplayName("Should restore deleted keys on rollback")
    void rollbackRestoresDeletedKeys() {
      trie.insert("apple", "fruit");
      trie.insert("banana", "fruit");
      long snapshotId = trie.createSnapshot();
      trie.delete("apple");
      assertThat(trie.has("apple")).isFalse();
      trie.rollbackToSnapshot(snapshotId);
      assertThat(trie.has("apple")).isTrue();
      assertThat(trie.has("banana")).isTrue();
    }
  }

  @Nested
  @DisplayName("Snapshot Management Tests")
  class SnapshotManagement {
    @Test
    @DisplayName("Should delete snapshot by ID")
    void deletesSnapshot() {
      long snapshotId = trie.createSnapshot();
      assertThat(trie.getSnapshotCount()).isEqualTo(1);
      boolean result = trie.deleteSnapshot(snapshotId);
      assertThat(result).isTrue();
      assertThat(trie.getSnapshotCount()).isZero();
    }

    @Test
    @DisplayName("Should clear all snapshots")
    void clearsAllSnapshots() {
      trie.createSnapshot();
      trie.createSnapshot();
      trie.createSnapshot();
      assertThat(trie.getSnapshotCount()).isEqualTo(3);
      trie.clearAllSnapshots();
      assertThat(trie.getSnapshotCount()).isZero();
    }

    @Test
    @DisplayName("Should return all snapshots")
    void getsAllSnapshots() {
      long id1 = trie.createSnapshot();
      trie.insert("key", "value");
      long id2 = trie.createSnapshot();
      Map<Long, TrieSnapshot<String>> snapshots = trie.getSnapshots();
      assertThat(snapshots).hasSize(2).containsKey(id1).containsKey(id2);
    }
  }

  @Nested
  @DisplayName("Global Version Tests")
  class GlobalVersion {
    @Test
    @DisplayName("Should increment global version on rollback")
    void globalVersionIncrementsOnRollback() {
      trie.insert("key", "value");
      long snapshotId = trie.createSnapshot();
      trie.insert("key2", "value2");
      long versionBefore = trie.getGlobalVersion();
      trie.rollbackToSnapshot(snapshotId);
      long versionAfter = trie.getGlobalVersion();
      assertThat(versionAfter).isGreaterThan(versionBefore);
    }
  }

  @Nested
  @DisplayName("Node Rollback Tests")
  class NodeRollback {
    @Test
    @DisplayName("Should restore previous node state on rollback")
    void rollbackNodeRestoresPreviousState() {
      trie.insert("apple", "v1");
      trie.insert("apple", "v2");
      trie.insert("apple", "v3");
      assertThat(trie.search("apple")).contains("v3");
      boolean result = trie.rollbackNode("apple");
      assertThat(result).isTrue();
      assertThat(trie.search("apple")).contains("v2");
    }

    @Test
    @DisplayName("Should return false when rolling back non-existent node")
    void rollbackNonExistentNodeReturnsFalse() {
      boolean result = trie.rollbackNode("nonexistent");
      assertThat(result).isFalse();
    }
  }
}
