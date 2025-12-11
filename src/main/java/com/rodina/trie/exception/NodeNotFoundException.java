package com.rodina.trie.exception;

public class NodeNotFoundException extends TrieException {
  public NodeNotFoundException() {
    super();
  }

  public NodeNotFoundException(String message) {
    super(message);
  }

  public NodeNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public NodeNotFoundException(Throwable cause) {
    super(cause);
  }
}
