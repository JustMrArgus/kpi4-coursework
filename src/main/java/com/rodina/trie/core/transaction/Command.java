package com.rodina.trie.core.transaction;

public interface Command {
  void execute();

  void undo();

  String getName();
}
