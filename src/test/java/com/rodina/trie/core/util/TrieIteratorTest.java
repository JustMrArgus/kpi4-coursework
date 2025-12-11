package com.rodina.trie.core.util;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.rodina.trie.core.node.TrieNode;

@DisplayName("TrieIterator Unit Tests")
class TrieIteratorTest {
  private TrieNode<String> root;

  @BeforeEach
  void setUp() {
    root = new TrieNode<>();
  }

  @Nested
  @DisplayName("Empty Trie Tests")
  class EmptyTrieTests {
    @Test
    @DisplayName("Should have no elements for empty root")
    void emptyRoot() {
      TrieIterator<String> iterator = new TrieIterator<>(root);
      assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    @DisplayName("Should handle null root")
    void nullRoot() {
      TrieIterator<String> iterator = new TrieIterator<>(null);
      assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    @DisplayName("Should throw NoSuchElementException on next when empty")
    void throwsOnNextWhenEmpty() {
      TrieIterator<String> iterator = new TrieIterator<>(root);
      assertThatThrownBy(iterator::next)
          .isInstanceOf(NoSuchElementException.class)
          .hasMessageContaining("No more elements");
    }
  }

  @Nested
  @DisplayName("Single Element Tests")
  class SingleElementTests {
    @Test
    @DisplayName("Should iterate single root element")
    void singleRootElement() {
      root.setEndOfWord(true);
      root.setValue("rootValue");
      TrieIterator<String> iterator = new TrieIterator<>(root);
      assertThat(iterator.hasNext()).isTrue();
      Map.Entry<String, String> entry = iterator.next();
      assertThat(entry.getKey()).isEmpty();
      assertThat(entry.getValue()).isEqualTo("rootValue");
      assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    @DisplayName("Should iterate single child element")
    void singleChildElement() {
      TrieNode<String> child = root.addChild('a');
      child.setEndOfWord(true);
      child.setValue("aValue");
      TrieIterator<String> iterator = new TrieIterator<>(root);
      assertThat(iterator.hasNext()).isTrue();
      Map.Entry<String, String> entry = iterator.next();
      assertThat(entry.getKey()).isEqualTo("a");
      assertThat(entry.getValue()).isEqualTo("aValue");
      assertThat(iterator.hasNext()).isFalse();
    }
  }

  @Nested
  @DisplayName("Multiple Elements Tests")
  class MultipleElementsTests {
    @Test
    @DisplayName("Should iterate all elements in order")
    void iteratesAllElements() {
      TrieNode<String> a = root.addChild('a');
      a.setEndOfWord(true);
      a.setValue("1");
      TrieNode<String> ab = a.addChild('b');
      ab.setEndOfWord(true);
      ab.setValue("2");
      TrieNode<String> abc = ab.addChild('c');
      abc.setEndOfWord(true);
      abc.setValue("3");
      TrieIterator<String> iterator = new TrieIterator<>(root);
      List<String> keys = new ArrayList<>();
      List<String> values = new ArrayList<>();
      while (iterator.hasNext()) {
        Map.Entry<String, String> entry = iterator.next();
        keys.add(entry.getKey());
        values.add(entry.getValue());
      }
      assertThat(keys).containsExactly("a", "ab", "abc");
      assertThat(values).containsExactly("1", "2", "3");
    }

    @Test
    @DisplayName("Should iterate multiple branches")
    void iteratesMultipleBranches() {
      TrieNode<String> a = root.addChild('a');
      a.setEndOfWord(true);
      a.setValue("aVal");
      TrieNode<String> b = root.addChild('b');
      b.setEndOfWord(true);
      b.setValue("bVal");
      TrieNode<String> c = root.addChild('c');
      c.setEndOfWord(true);
      c.setValue("cVal");
      TrieIterator<String> iterator = new TrieIterator<>(root);
      List<String> keys = new ArrayList<>();
      while (iterator.hasNext()) {
        keys.add(iterator.next().getKey());
      }
      assertThat(keys).containsExactlyInAnyOrder("a", "b", "c");
    }

    @Test
    @DisplayName("Should skip non-end-of-word nodes")
    void skipsNonEndOfWordNodes() {
      TrieNode<String> a = root.addChild('a');
      TrieNode<String> ab = a.addChild('b');
      TrieNode<String> abc = ab.addChild('c');
      abc.setEndOfWord(true);
      abc.setValue("word");
      TrieIterator<String> iterator = new TrieIterator<>(root);
      List<String> keys = new ArrayList<>();
      while (iterator.hasNext()) {
        keys.add(iterator.next().getKey());
      }
      assertThat(keys).containsExactly("abc");
    }

    @Test
    @DisplayName("Should skip nodes with null value even if end of word")
    void skipsNullValueNodes() {
      TrieNode<String> a = root.addChild('a');
      a.setEndOfWord(true);
      a.setValue(null);
      TrieNode<String> ab = a.addChild('b');
      ab.setEndOfWord(true);
      ab.setValue("value");
      TrieIterator<String> iterator = new TrieIterator<>(root);
      List<String> keys = new ArrayList<>();
      while (iterator.hasNext()) {
        keys.add(iterator.next().getKey());
      }
      assertThat(keys).containsExactly("ab");
    }
  }

  @Nested
  @DisplayName("Initial Prefix Tests")
  class InitialPrefixTests {
    @Test
    @DisplayName("Should start iteration from initial prefix")
    void iteratesWithInitialPrefix() {
      TrieNode<String> a = root.addChild('a');
      a.setEndOfWord(true);
      a.setValue("value");
      TrieIterator<String> iterator = new TrieIterator<>(root, "prefix");
      assertThat(iterator.hasNext()).isTrue();
      Map.Entry<String, String> entry = iterator.next();
      assertThat(entry.getKey()).isEqualTo("prefixa");
    }

    @Test
    @DisplayName("Should handle empty initial prefix")
    void emptyInitialPrefix() {
      TrieNode<String> a = root.addChild('x');
      a.setEndOfWord(true);
      a.setValue("val");
      TrieIterator<String> iterator = new TrieIterator<>(root, "");
      assertThat(iterator.hasNext()).isTrue();
      assertThat(iterator.next().getKey()).isEqualTo("x");
    }
  }

  @Nested
  @DisplayName("Iterator Contract Tests")
  class IteratorContractTests {
    @Test
    @DisplayName("hasNext should be idempotent")
    void hasNextIsIdempotent() {
      TrieNode<String> a = root.addChild('a');
      a.setEndOfWord(true);
      a.setValue("val");
      TrieIterator<String> iterator = new TrieIterator<>(root);
      assertThat(iterator.hasNext()).isTrue();
      assertThat(iterator.hasNext()).isTrue();
      assertThat(iterator.hasNext()).isTrue();
      iterator.next();
      assertThat(iterator.hasNext()).isFalse();
      assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    @DisplayName("Should exhaust iterator completely")
    void exhaustsIterator() {
      root.addChild('a').setEndOfWord(true);
      root.getChild('a').setValue("1");
      root.addChild('b').setEndOfWord(true);
      root.getChild('b').setValue("2");
      TrieIterator<String> iterator = new TrieIterator<>(root);
      int count = 0;
      while (iterator.hasNext()) {
        iterator.next();
        count++;
      }
      assertThat(count).isEqualTo(2);
      assertThat(iterator.hasNext()).isFalse();
    }
  }
}
