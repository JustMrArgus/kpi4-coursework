package com.rodina.trie.core.node;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("NodeSnapshot Unit Tests")
class NodeSnapshotTest {
  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {
    @Test
    @DisplayName("Should create snapshot from TrieNode")
    void createFromTrieNode() {
      TrieNode<String> node = new TrieNode<>();
      node.setValue("value");
      node.setEndOfWord(true);
      node.addChild('a');
      node.addChild('b');
      NodeSnapshot<String> snapshot = new NodeSnapshot<>(node, null);
      assertThat(snapshot.getValue()).isEqualTo("value");
      assertThat(snapshot.isEndOfWord()).isTrue();
      assertThat(snapshot.getChildrenSnapshot()).hasSize(2);
      assertThat(snapshot.getPreviousSnapshot()).isNull();
      assertThat(snapshot.getTimestamp()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should create snapshot with explicit parameters")
    void createWithExplicitParams() {
      Map<Character, TrieNode<String>> children = new HashMap<>();
      children.put('x', new TrieNode<>());
      NodeSnapshot<String> snapshot = new NodeSnapshot<>(100L, "testValue", true, children, null);
      assertThat(snapshot.getVersion()).isEqualTo(100L);
      assertThat(snapshot.getValue()).isEqualTo("testValue");
      assertThat(snapshot.isEndOfWord()).isTrue();
      assertThat(snapshot.getChildrenSnapshot()).hasSize(1);
      assertThat(snapshot.getPreviousSnapshot()).isNull();
    }

    @Test
    @DisplayName("Should link to previous snapshot")
    void linkToPreviousSnapshot() {
      Map<Character, TrieNode<String>> children = new HashMap<>();
      NodeSnapshot<String> first = new NodeSnapshot<>(1L, "first", false, children, null);
      NodeSnapshot<String> second = new NodeSnapshot<>(2L, "second", true, children, first);
      assertThat(second.getPreviousSnapshot()).isEqualTo(first);
      assertThat(first.getPreviousSnapshot()).isNull();
    }
  }

  @Nested
  @DisplayName("Version Comparison Tests")
  class VersionComparisonTests {
    @Test
    @DisplayName("Should check if before version")
    void isBeforeVersion() {
      Map<Character, TrieNode<String>> children = new HashMap<>();
      NodeSnapshot<String> snapshot = new NodeSnapshot<>(10L, "v", false, children, null);
      assertThat(snapshot.isBeforeVersion(20L)).isTrue();
      assertThat(snapshot.isBeforeVersion(10L)).isFalse();
      assertThat(snapshot.isBeforeVersion(5L)).isFalse();
    }

    @Test
    @DisplayName("Should check if before timestamp")
    void isBeforeTimestamp() {
      Map<Character, TrieNode<String>> children = new HashMap<>();
      NodeSnapshot<String> snapshot = new NodeSnapshot<>(1L, "v", false, children, null);
      long futureTimestamp = System.currentTimeMillis() + 10000;
      long pastTimestamp = 1L;
      assertThat(snapshot.isBeforeTimestamp(futureTimestamp)).isTrue();
      assertThat(snapshot.isBeforeTimestamp(pastTimestamp)).isFalse();
    }
  }

  @Nested
  @DisplayName("Children Snapshot Tests")
  class ChildrenSnapshotTests {
    @Test
    @DisplayName("Should return defensive copy of children")
    void childrenSnapshotIsDefensiveCopy() {
      TrieNode<String> node = new TrieNode<>();
      node.addChild('a');
      NodeSnapshot<String> snapshot = new NodeSnapshot<>(node, null);
      Map<Character, TrieNode<String>> children = snapshot.getChildrenSnapshot();
      assertThat(children).hasSize(1);
      children.clear();
      assertThat(snapshot.getChildrenSnapshot()).hasSize(1);
    }
  }

  @Nested
  @DisplayName("ToString Tests")
  class ToStringTests {
    @Test
    @DisplayName("Should produce readable toString")
    void toStringIsReadable() {
      Map<Character, TrieNode<String>> children = new HashMap<>();
      children.put('a', new TrieNode<>());
      NodeSnapshot<String> snapshot = new NodeSnapshot<>(42L, "testValue", true, children, null);
      String str = snapshot.toString();
      assertThat(str).contains("NodeSnapshot");
      assertThat(str).contains("version=42");
      assertThat(str).contains("isEndOfWord=true");
      assertThat(str).contains("testValue");
      assertThat(str).contains("childrenCount=1");
    }
  }
}
