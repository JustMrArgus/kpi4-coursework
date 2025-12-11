package com.rodina.trie.core.snapshot;

import static org.assertj.core.api.Assertions.assertThat;

import com.rodina.trie.core.node.TrieNode;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TrieSnapshotManager Tests")
class TrieSnapshotManagerTest {
  private TrieSnapshotManager<String> manager;
  private TrieNode<String> root;

  @BeforeEach
  void setUp() {
    manager = new TrieSnapshotManager<>();
    root = new TrieNode<>();
  }

  private void populateRoot(int wordCount) {
    TrieNode<String> current = root;
    for (int i = 0; i < wordCount; i++) {
      TrieNode<String> child = new TrieNode<>();
      child.setValue("value" + i);
      child.setEndOfWord(true);
      current.setChild((char) ('a' + i), child);
    }
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {
    @Test
    @DisplayName("Should create manager with default max snapshots")
    void shouldCreateManagerWithDefaultMaxSnapshots() {
      TrieSnapshotManager<String> defaultManager = new TrieSnapshotManager<>();
      assertThat(defaultManager.getSnapshotCount()).isZero();
    }

    @Test
    @DisplayName("Should create manager with custom max snapshots")
    void shouldCreateManagerWithCustomMaxSnapshots() {
      TrieSnapshotManager<String> customManager = new TrieSnapshotManager<>(5);
      assertThat(customManager.getSnapshotCount()).isZero();
    }
  }

  @Nested
  @DisplayName("CreateSnapshot Tests")
  class CreateSnapshotTests {
    @Test
    @DisplayName("Should create snapshot and return unique ID")
    void shouldCreateSnapshotAndReturnUniqueId() {
      populateRoot(3);
      long id1 = manager.createSnapshot(root, 3);
      long id2 = manager.createSnapshot(root, 3);
      assertThat(id1).isNotEqualTo(id2);
      assertThat(manager.getSnapshotCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should store snapshot retrievable by ID")
    void shouldStoreSnapshotRetrievableById() {
      populateRoot(2);
      long id = manager.createSnapshot(root, 2);
      TrieSnapshot<String> snapshot = manager.getSnapshot(id);
      assertThat(snapshot).isNotNull();
      assertThat(snapshot.getId()).isEqualTo(id);
      assertThat(snapshot.getSize()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should deep clone root node")
    void shouldDeepCloneRootNode() {
      root.setValue("rootValue");
      TrieNode<String> child = new TrieNode<>();
      child.setValue("childValue");
      root.setChild('a', child);
      long id = manager.createSnapshot(root, 1);
      root.setValue("modified");
      child.setValue("modifiedChild");
      TrieSnapshot<String> snapshot = manager.getSnapshot(id);
      assertThat(snapshot.getRoot().getValue()).isEqualTo("rootValue");
      assertThat(snapshot.getRoot().getChild('a').getValue()).isEqualTo("childValue");
    }

    @Test
    @DisplayName("Should enforce max snapshots limit")
    void shouldEnforceMaxSnapshotsLimit() {
      TrieSnapshotManager<String> limitedManager = new TrieSnapshotManager<>(3);
      long id1 = limitedManager.createSnapshot(root, 0);
      long id2 = limitedManager.createSnapshot(root, 0);
      long id3 = limitedManager.createSnapshot(root, 0);
      long id4 = limitedManager.createSnapshot(root, 0);
      long id5 = limitedManager.createSnapshot(root, 0);
      assertThat(limitedManager.getSnapshotCount()).isEqualTo(3);
      assertThat(limitedManager.hasSnapshot(id1)).isFalse();
      assertThat(limitedManager.hasSnapshot(id2)).isFalse();
      assertThat(limitedManager.hasSnapshot(id5)).isTrue();
    }
  }

  @Nested
  @DisplayName("GetSnapshot Tests")
  class GetSnapshotTests {
    @Test
    @DisplayName("Should return null for non-existent ID")
    void shouldReturnNullForNonExistentId() {
      assertThat(manager.getSnapshot(999L)).isNull();
    }

    @Test
    @DisplayName("Should return correct snapshot by ID")
    void shouldReturnCorrectSnapshotById() {
      populateRoot(5);
      long id = manager.createSnapshot(root, 5);
      TrieSnapshot<String> snapshot = manager.getSnapshot(id);
      assertThat(snapshot).isNotNull();
      assertThat(snapshot.getSize()).isEqualTo(5);
    }
  }

  @Nested
  @DisplayName("HasSnapshot Tests")
  class HasSnapshotTests {
    @Test
    @DisplayName("Should return true for existing snapshot")
    void shouldReturnTrueForExistingSnapshot() {
      long id = manager.createSnapshot(root, 0);
      assertThat(manager.hasSnapshot(id)).isTrue();
    }

    @Test
    @DisplayName("Should return false for non-existing snapshot")
    void shouldReturnFalseForNonExistingSnapshot() {
      assertThat(manager.hasSnapshot(12345L)).isFalse();
    }
  }

  @Nested
  @DisplayName("DeleteSnapshot Tests")
  class DeleteSnapshotTests {
    @Test
    @DisplayName("Should delete existing snapshot and return true")
    void shouldDeleteExistingSnapshotAndReturnTrue() {
      long id = manager.createSnapshot(root, 0);
      boolean deleted = manager.deleteSnapshot(id);
      assertThat(deleted).isTrue();
      assertThat(manager.hasSnapshot(id)).isFalse();
      assertThat(manager.getSnapshotCount()).isZero();
    }

    @Test
    @DisplayName("Should return false when deleting non-existing snapshot")
    void shouldReturnFalseWhenDeletingNonExistingSnapshot() {
      boolean deleted = manager.deleteSnapshot(999L);
      assertThat(deleted).isFalse();
    }
  }

  @Nested
  @DisplayName("GetAllSnapshots Tests")
  class GetAllSnapshotsTests {
    @Test
    @DisplayName("Should return empty map when no snapshots")
    void shouldReturnEmptyMapWhenNoSnapshots() {
      Map<Long, TrieSnapshot<String>> all = manager.getAllSnapshots();
      assertThat(all).isEmpty();
    }

    @Test
    @DisplayName("Should return all snapshots")
    void shouldReturnAllSnapshots() {
      long id1 = manager.createSnapshot(root, 1);
      long id2 = manager.createSnapshot(root, 2);
      long id3 = manager.createSnapshot(root, 3);
      Map<Long, TrieSnapshot<String>> all = manager.getAllSnapshots();
      assertThat(all).hasSize(3);
      assertThat(all).containsKeys(id1, id2, id3);
    }

    @Test
    @DisplayName("Should return defensive copy")
    void shouldReturnDefensiveCopy() {
      manager.createSnapshot(root, 0);
      Map<Long, TrieSnapshot<String>> copy1 = manager.getAllSnapshots();
      Map<Long, TrieSnapshot<String>> copy2 = manager.getAllSnapshots();
      assertThat(copy1).isNotSameAs(copy2);
    }
  }

  @Nested
  @DisplayName("ClearAllSnapshots Tests")
  class ClearAllSnapshotsTests {
    @Test
    @DisplayName("Should remove all snapshots")
    void shouldRemoveAllSnapshots() {
      manager.createSnapshot(root, 0);
      manager.createSnapshot(root, 0);
      manager.createSnapshot(root, 0);
      manager.clearAllSnapshots();
      assertThat(manager.getSnapshotCount()).isZero();
      assertThat(manager.getAllSnapshots()).isEmpty();
    }
  }

  @Nested
  @DisplayName("GetLatestSnapshotId Tests")
  class GetLatestSnapshotIdTests {
    @Test
    @DisplayName("Should return null when no snapshots")
    void shouldReturnNullWhenNoSnapshots() {
      assertThat(manager.getLatestSnapshotId()).isNull();
    }

    @Test
    @DisplayName("Should return latest snapshot ID")
    void shouldReturnLatestSnapshotId() {
      long id1 = manager.createSnapshot(root, 0);
      long id2 = manager.createSnapshot(root, 0);
      long id3 = manager.createSnapshot(root, 0);
      Long latestId = manager.getLatestSnapshotId();
      assertThat(latestId).isEqualTo(id3);
    }

    @Test
    @DisplayName("Should return correct latest after deletion")
    void shouldReturnCorrectLatestAfterDeletion() {
      long id1 = manager.createSnapshot(root, 0);
      long id2 = manager.createSnapshot(root, 0);
      long id3 = manager.createSnapshot(root, 0);
      manager.deleteSnapshot(id3);
      assertThat(manager.getLatestSnapshotId()).isEqualTo(id2);
    }
  }

  @Nested
  @DisplayName("GetSnapshotCount Tests")
  class GetSnapshotCountTests {
    @Test
    @DisplayName("Should return zero for empty manager")
    void shouldReturnZeroForEmptyManager() {
      assertThat(manager.getSnapshotCount()).isZero();
    }

    @Test
    @DisplayName("Should return correct count")
    void shouldReturnCorrectCount() {
      manager.createSnapshot(root, 0);
      assertThat(manager.getSnapshotCount()).isEqualTo(1);
      manager.createSnapshot(root, 0);
      assertThat(manager.getSnapshotCount()).isEqualTo(2);
      manager.createSnapshot(root, 0);
      assertThat(manager.getSnapshotCount()).isEqualTo(3);
    }
  }

  @Nested
  @DisplayName("Deep Clone Tests")
  class DeepCloneTests {
    @Test
    @DisplayName("Should handle null root in deep clone")
    void shouldHandleNullRootInDeepClone() {
      long id = manager.createSnapshot(null, 0);
      TrieSnapshot<String> snapshot = manager.getSnapshot(id);
      assertThat(snapshot.getRoot()).isNull();
    }

    @Test
    @DisplayName("Should clone nested children")
    void shouldCloneNestedChildren() {
      TrieNode<String> level1 = new TrieNode<>();
      level1.setValue("level1");
      TrieNode<String> level2 = new TrieNode<>();
      level2.setValue("level2");
      TrieNode<String> level3 = new TrieNode<>();
      level3.setValue("level3");
      root.setChild('a', level1);
      level1.setChild('b', level2);
      level2.setChild('c', level3);
      long id = manager.createSnapshot(root, 3);
      level3.setValue("modified");
      TrieSnapshot<String> snapshot = manager.getSnapshot(id);
      TrieNode<String> clonedLevel3 = snapshot.getRoot().getChild('a').getChild('b').getChild('c');
      assertThat(clonedLevel3.getValue()).isEqualTo("level3");
    }
  }
}
