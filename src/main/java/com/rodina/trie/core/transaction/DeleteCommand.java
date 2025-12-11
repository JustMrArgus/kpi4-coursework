package com.rodina.trie.core.transaction;

import com.rodina.trie.contract.Trie;

public class DeleteCommand implements Command {
  private final Trie<Object> trie;
  private final String key;
  private Object deletedValue;
  private boolean executionSuccess;

  public DeleteCommand(Trie<Object> trie, String key) {
    this.trie = trie;
    this.key = key;
  }

  @Override
  public void execute() {
    if (trie.has(key)) {
      this.deletedValue = trie.search(key).orElse(null);
      this.executionSuccess = trie.delete(key);
    } else {
      this.executionSuccess = false;
    }
  }

  @Override
  public void undo() {
    if (executionSuccess && deletedValue != null) {
      trie.insert(key, deletedValue);
    }
  }

  @Override
  public String getName() {
    return "DeleteCommand(key=" + key + ")";
  }
}
