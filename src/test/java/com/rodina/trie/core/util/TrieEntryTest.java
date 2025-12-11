package com.rodina.trie.core.util;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TrieEntry Unit Tests")
class TrieEntryTest {
  @Nested
  @DisplayName("Basic Operations Tests")
  class BasicOperationsTests {
    @Test
    @DisplayName("Should create entry with key and value")
    void createWithKeyAndValue() {
      TrieEntry<String> entry = new TrieEntry<>("key", "value");
      assertThat(entry.getKey()).isEqualTo("key");
      assertThat(entry.getValue()).isEqualTo("value");
    }

    @Test
    @DisplayName("Should allow null key")
    void allowNullKey() {
      TrieEntry<String> entry = new TrieEntry<>(null, "value");
      assertThat(entry.getKey()).isNull();
    }

    @Test
    @DisplayName("Should allow null value")
    void allowNullValue() {
      TrieEntry<String> entry = new TrieEntry<>("key", null);
      assertThat(entry.getValue()).isNull();
    }

    @Test
    @DisplayName("Should set value and return old value")
    void setValueReturnsOldValue() {
      TrieEntry<String> entry = new TrieEntry<>("key", "oldValue");
      String oldValue = entry.setValue("newValue");
      assertThat(oldValue).isEqualTo("oldValue");
      assertThat(entry.getValue()).isEqualTo("newValue");
    }
  }

  @Nested
  @DisplayName("Equals and HashCode Tests")
  class EqualsHashCodeTests {
    @Test
    @DisplayName("Should be equal to itself")
    void equalToItself() {
      TrieEntry<String> entry = new TrieEntry<>("key", "value");
      assertThat(entry).isEqualTo(entry);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void notEqualToNull() {
      TrieEntry<String> entry = new TrieEntry<>("key", "value");
      assertThat(entry).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Should not be equal to different type")
    void notEqualToDifferentType() {
      TrieEntry<String> entry = new TrieEntry<>("key", "value");
      assertThat(entry).isNotEqualTo("string");
    }

    @Test
    @DisplayName("Should be equal when same key and value")
    void equalWhenSameKeyAndValue() {
      TrieEntry<String> entry1 = new TrieEntry<>("key", "value");
      TrieEntry<String> entry2 = new TrieEntry<>("key", "value");
      assertThat(entry1).isEqualTo(entry2);
      assertThat(entry1.hashCode()).isEqualTo(entry2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when different key")
    void notEqualWhenDifferentKey() {
      TrieEntry<String> entry1 = new TrieEntry<>("key1", "value");
      TrieEntry<String> entry2 = new TrieEntry<>("key2", "value");
      assertThat(entry1).isNotEqualTo(entry2);
    }

    @Test
    @DisplayName("Should not be equal when different value")
    void notEqualWhenDifferentValue() {
      TrieEntry<String> entry1 = new TrieEntry<>("key", "value1");
      TrieEntry<String> entry2 = new TrieEntry<>("key", "value2");
      assertThat(entry1).isNotEqualTo(entry2);
    }

    @Test
    @DisplayName("Should handle null key in equals")
    void nullKeyEquals() {
      TrieEntry<String> entry1 = new TrieEntry<>(null, "value");
      TrieEntry<String> entry2 = new TrieEntry<>(null, "value");
      TrieEntry<String> entry3 = new TrieEntry<>("key", "value");
      assertThat(entry1).isEqualTo(entry2);
      assertThat(entry1).isNotEqualTo(entry3);
    }

    @Test
    @DisplayName("Should handle null value in equals")
    void nullValueEquals() {
      TrieEntry<String> entry1 = new TrieEntry<>("key", null);
      TrieEntry<String> entry2 = new TrieEntry<>("key", null);
      TrieEntry<String> entry3 = new TrieEntry<>("key", "value");
      assertThat(entry1).isEqualTo(entry2);
      assertThat(entry1).isNotEqualTo(entry3);
    }

    @Test
    @DisplayName("Should have consistent hashCode for null key")
    void nullKeyHashCode() {
      TrieEntry<String> entry = new TrieEntry<>(null, "value");
      assertThatCode(entry::hashCode).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should have consistent hashCode for null value")
    void nullValueHashCode() {
      TrieEntry<String> entry = new TrieEntry<>("key", null);
      assertThatCode(entry::hashCode).doesNotThrowAnyException();
    }
  }

  @Nested
  @DisplayName("ToString Tests")
  class ToStringTests {
    @Test
    @DisplayName("Should produce key=value format")
    void toStringFormat() {
      TrieEntry<String> entry = new TrieEntry<>("myKey", "myValue");
      assertThat(entry.toString()).isEqualTo("myKey=myValue");
    }

    @Test
    @DisplayName("Should handle null in toString")
    void toStringWithNull() {
      TrieEntry<String> entry = new TrieEntry<>(null, null);
      assertThat(entry.toString()).isEqualTo("null=null");
    }
  }
}
