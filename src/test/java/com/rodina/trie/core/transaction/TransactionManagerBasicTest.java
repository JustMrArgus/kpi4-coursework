package com.rodina.trie.core.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rodina.trie.contract.Trie;
import com.rodina.trie.core.impl.ConcurrentTrie;

@DisplayName("Transaction Manager Basic Tests")
class TransactionManagerBasicTest {
  private Trie<Object> trie;
  private TransactionManager transactionManager;

  @BeforeEach
  void setUp() {
    trie = new ConcurrentTrie<>();
    transactionManager = new TransactionManager();
  }

  @Test
  @DisplayName("Should remove entry when undoing insert command")
  void undoInsertRemovesEntry() {
    Command insert = new InsertCommand(trie, "key1", "val1");
    transactionManager.execute(insert);
    assertThat(trie.has("key1")).isTrue();
    transactionManager.undo();
    assertThat(trie.has("key1")).isFalse();
  }

  @Test
  @DisplayName("Should restore entry when redoing insert command")
  void redoInsertRestoresEntry() {
    Command insert = new InsertCommand(trie, "key1", "val1");
    transactionManager.execute(insert);
    transactionManager.undo();
    assertThat(trie.has("key1")).isFalse();
    transactionManager.redo();
    assertThat(trie.has("key1")).isTrue();
    assertThat(trie.search("key1").get()).isEqualTo("val1");
  }

  @Test
  @DisplayName("Should restore entry when undoing delete command")
  void undoDeleteRestoresEntry() {
    trie.insert("key1", "val1");
    Command delete = new DeleteCommand(trie, "key1");
    transactionManager.execute(delete);
    assertThat(trie.has("key1")).isFalse();
    transactionManager.undo();
    assertThat(trie.has("key1")).isTrue();
    assertThat(trie.search("key1").get()).isEqualTo("val1");
  }

  @Test
  @DisplayName("Should correctly reflect undo/redo stack state")
  void stackStateReflectsOperations() {
    assertThat(transactionManager.canUndo()).isFalse();
    assertThat(transactionManager.canRedo()).isFalse();
    transactionManager.execute(new InsertCommand(trie, "a", 1));
    assertThat(transactionManager.canUndo()).isTrue();
    assertThat(transactionManager.canRedo()).isFalse();
    transactionManager.undo();
    assertThat(transactionManager.canUndo()).isFalse();
    assertThat(transactionManager.canRedo()).isTrue();
  }
}
