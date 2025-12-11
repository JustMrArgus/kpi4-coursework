package com.rodina.trie.core.transaction;

import java.util.Stack;

import org.springframework.stereotype.Component;

@Component
public class TransactionManager {
  private final Stack<Command> history = new Stack<>();
  private final Stack<Command> redoStack = new Stack<>();

  public void execute(Command command) {
    command.execute();
    history.push(command);
    redoStack.clear();
  }

  public void undo() {
    if (!history.isEmpty()) {
      Command command = history.pop();
      command.undo();
      redoStack.push(command);
    }
  }

  public void redo() {
    if (!redoStack.isEmpty()) {
      Command command = redoStack.pop();
      command.execute();
      history.push(command);
    }
  }

  public boolean canUndo() {
    return !history.isEmpty();
  }

  public boolean canRedo() {
    return !redoStack.isEmpty();
  }

  public void clearHistory() {
    history.clear();
    redoStack.clear();
  }

  public int getHistorySize() {
    return history.size();
  }
}
