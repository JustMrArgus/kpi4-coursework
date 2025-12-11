package com.rodina.trie.core.transaction;

import com.rodina.trie.contract.Trie;

public class InsertCommand implements Command {
  private final Trie<Object> trie;
  private final String key;
  private final Object newValue;
  private Object oldValue;
  private boolean wasNewInsert;

  public InsertCommand(Trie<Object> trie, String key, Object newValue) {
    this.trie = trie;
    this.key = key;
    this.newValue = newValue;
  }

  @Override
  public void execute() {
    if (trie.has(key)) {
      this.oldValue = trie.search(key).orElse(null);
      this.wasNewInsert = false;
    } else {
      this.wasNewInsert = true;
    }
    trie.insert(key, newValue);
  }

  @Override
  public void undo() {
    if (wasNewInsert) {
      trie.delete(key);
    } else {
      if (oldValue != null) {
        trie.insert(key, oldValue);
      }
    }
  }

  @Override
  public String getName() {
    return "InsertCommand(key=" + key + ")";
  }
}
