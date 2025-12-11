package com.rodina.trie.core.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rodina.trie.contract.Trie;
import com.rodina.trie.core.impl.ConcurrentTrie;

@DisplayName("Transaction Manager Workflow Tests")
class TransactionManagerWorkflowTest {
  private Trie<Object> trie;
  private TransactionManager transactionManager;

  @BeforeEach
  void setUp() {
    trie = new ConcurrentTrie<>();
    transactionManager = new TransactionManager();
  }

  @Test
  @DisplayName("Should undo insertions in reverse order")
  void sequenceUndoRollsBackInOrder() {
    transactionManager.execute(new InsertCommand(trie, "a", 1));
    transactionManager.execute(new InsertCommand(trie, "b", 2));
    transactionManager.execute(new InsertCommand(trie, "c", 3));
    assertThat(trie.size()).isEqualTo(3);
    transactionManager.undo();
    assertThat(trie.size()).isEqualTo(2);
    assertThat(trie.has("c")).isFalse();
    transactionManager.undo();
    assertThat(trie.size()).isEqualTo(1);
    assertThat(trie.has("b")).isFalse();
    transactionManager.undo();
    assertThat(trie.size()).isZero();
    assertThat(trie.has("a")).isFalse();
  }

  @Test
  @DisplayName("Should correctly handle interleaved undo and redo operations")
  void interleavedUndoRedoWorks() {
    transactionManager.execute(new InsertCommand(trie, "k1", "v1"));
    transactionManager.execute(new InsertCommand(trie, "k2", "v2"));
    transactionManager.undo();
    assertThat(trie.has("k2")).isFalse();
    transactionManager.redo();
    assertThat(trie.has("k2")).isTrue();
    transactionManager.execute(new InsertCommand(trie, "k3", "v3"));
    assertThat(transactionManager.canRedo()).isFalse();
  }

  @Test
  @DisplayName("Should restore previous value when undoing update")
  void updateCommandUndoRestoresPreviousValue() {
    transactionManager.execute(new InsertCommand(trie, "key", "v1"));
    assertThat(trie.search("key").get()).isEqualTo("v1");
    transactionManager.execute(new InsertCommand(trie, "key", "v2"));
    assertThat(trie.search("key").get()).isEqualTo("v2");
    transactionManager.undo();
    assertThat(trie.search("key").get()).isEqualTo("v1");
    transactionManager.redo();
    assertThat(trie.search("key").get()).isEqualTo("v2");
  }

  @Test
  @DisplayName("Should safely handle delete command for non-existent key")
  void deleteNonExistentCommandIsSafe() {
    Command cmd = new DeleteCommand(trie, "ghost");
    transactionManager.execute(cmd);
    assertThat(trie.has("ghost")).isFalse();
    transactionManager.undo();
    assertThat(trie.has("ghost")).isFalse();
  }

  @Test
  @DisplayName("Should reset undo/redo state when clearing history")
  void clearHistoryResetsState() {
    transactionManager.execute(new InsertCommand(trie, "a", 1));
    transactionManager.execute(new InsertCommand(trie, "b", 2));
    assertThat(transactionManager.getHistorySize()).isEqualTo(2);
    transactionManager.clearHistory();
    assertThat(transactionManager.getHistorySize()).isZero();
    assertThat(transactionManager.canUndo()).isFalse();
  }
}
