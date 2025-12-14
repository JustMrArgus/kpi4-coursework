package com.rodina.trie.core.node;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TrieNode Unit Tests")
class TrieNodeTest {
  private TrieNode<String> node;

  @BeforeEach
  void setUp() {
    node = new TrieNode<>();
  }

  @Test
  @DisplayName("Node initializes with correct default values and basic operations work")
  void basicOperations() {
    assertThat(node.getValue()).isNull();
    assertThat(node.isEndOfWord()).isFalse();
    assertThat(node.hasChildren()).isFalse();
    assertThat(node.getChildrenCount()).isZero();
    assertThat(node.getVersion()).isGreaterThan(0);
    assertThat(node.getCreatedAt()).isGreaterThan(0);
    assertThat(node.getAccessCount()).isZero();

    node.setValue("testValue");
    assertThat(node.getValue()).isEqualTo("testValue");

    node.setEndOfWord(true);
    assertThat(node.isEndOfWord()).isTrue();
    node.setEndOfWord(false);
    assertThat(node.isEndOfWord()).isFalse();

    node.incrementAccessCount();
    node.incrementAccessCount();
    assertThat(node.getAccessCount()).isEqualTo(2);
    node.resetAccessCount();
    assertThat(node.getAccessCount()).isZero();
  }

  @Nested
  @DisplayName("Children Operations Tests")
  class ChildrenOperationsTests {
    @Test
    @DisplayName("Child operations work correctly")
    void childOperations() {
      TrieNode<String> child = node.addChild('a');
      assertThat(child).isNotNull();
      assertThat(node.getChild('a')).isEqualTo(child);
      assertThat(node.hasChild('a')).isTrue();

      assertThat(node.addChild('a')).isSameAs(child);

      TrieNode<String> customChild = new TrieNode<>();
      customChild.setValue("custom");
      node.setChild('x', customChild);
      assertThat(node.getChild('x').getValue()).isEqualTo("custom");

      node.addChild('b');
      node.addChild('c');
      assertThat(node.getChildrenCount()).isEqualTo(4);
      assertThat(node.hasChildren()).isTrue();

      Set<Character> keys = node.getChildrenKeys();
      assertThat(keys).contains('a', 'x', 'b', 'c');

      node.removeChild('b');
      assertThat(node.hasChild('b')).isFalse();

      Map<Character, TrieNode<String>> map = node.getChildrenMap();
      int sizeBefore = node.getChildrenCount();
      map.clear();
      assertThat(node.getChildrenCount()).isEqualTo(sizeBefore);
    }
  }

  @Nested
  @DisplayName("Locking Tests")
  class LockingTests {
    @Test
    @DisplayName("Read and write locks work correctly")
    void lockingOperations() {
      assertThatCode(
              () -> {
                node.lockRead();
                try {
                  node.getValue();
                } finally {
                  node.unlockRead();
                }
              })
          .doesNotThrowAnyException();

      assertThatCode(
              () -> {
                node.lockWrite();
                try {
                  node.setValue("test");
                } finally {
                  node.unlockWrite();
                }
              })
          .doesNotThrowAnyException();

      node.lockWrite();
      try {
        node.readLockFromWriteLock();
        try {
          node.getValue();
        } finally {
          node.unlockRead();
        }
      } finally {
        node.unlockWrite();
      }
    }
  }

  @Nested
  @DisplayName("Snapshot and Rollback Tests")
  class SnapshotTests {
    @Test
    @DisplayName("Snapshot and rollback operations work correctly")
    void snapshotOperations() {
      assertThat(node.hasPreviousSnapshot()).isFalse();
      assertThat(node.getSnapshotDepth()).isZero();

      node.setValue("first");
      assertThat(node.hasPreviousSnapshot()).isTrue();
      assertThat(node.getSnapshotDepth()).isEqualTo(1);

      node.setValue("second");
      assertThat(node.getValue()).isEqualTo("second");
      assertThat(node.rollback()).isTrue();
      assertThat(node.getValue()).isEqualTo("first");

      TrieNode<String> freshNode = new TrieNode<>();
      assertThat(freshNode.rollback()).isFalse();

      node.setValue("v1");
      node.setValue("v2");
      assertThat(node.getSnapshotDepth()).isGreaterThan(0);
      node.clearSnapshotHistory();
      assertThat(node.hasPreviousSnapshot()).isFalse();

      int depth = node.getSnapshotDepth();
      node.saveSnapshot();
      assertThat(node.getSnapshotDepth()).isEqualTo(depth + 1);
    }
  }

  @Test
  @DisplayName("Clear resets node state")
  void clearNodeState() {
    node.setValue("value");
    node.setEndOfWord(true);
    node.addChild('a');

    node.clear();

    assertThat(node.getValue()).isNull();
    assertThat(node.isEndOfWord()).isFalse();
    assertThat(node.hasChildren()).isFalse();
  }

  @Test
  @DisplayName("Equals, hashCode, and toString work correctly")
  void equalsHashCodeToString() {
    assertThat(node).isEqualTo(node);
    assertThat(node).isNotEqualTo(null);
    assertThat(node).isNotEqualTo("string");

    TrieNode<String> node1 = new TrieNode<>();
    TrieNode<String> node2 = new TrieNode<>();
    node1.setValue("value");
    node1.setEndOfWord(true);
    node2.setValue("value");
    node2.setEndOfWord(true);
    assertThat(node1).isEqualTo(node2);
    assertThat(node1.hashCode()).isEqualTo(node2.hashCode());

    node2.setValue("other");
    assertThat(node1).isNotEqualTo(node2);

    node.setValue("testValue");
    node.setEndOfWord(true);
    node.addChild('a');
    String str = node.toString();
    assertThat(str).contains("TrieNode", "isEndOfWord=true", "testValue", "childrenCount=1");
  }
}
