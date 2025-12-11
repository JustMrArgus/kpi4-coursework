package com.rodina.trie.exception;

public class TrieStateIllegalException extends TrieException {
  public TrieStateIllegalException() {
    super();
  }

  public TrieStateIllegalException(String message) {
    super(message);
  }

  public TrieStateIllegalException(String message, Throwable cause) {
    super(message, cause);
  }

  public TrieStateIllegalException(Throwable cause) {
    super(cause);
  }
}
