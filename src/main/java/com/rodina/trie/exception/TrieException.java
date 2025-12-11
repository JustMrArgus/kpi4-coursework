package com.rodina.trie.exception;

public class TrieException extends RuntimeException {
  public TrieException() {
    super();
  }

  public TrieException(String message) {
    super(message);
  }

  public TrieException(String message, Throwable cause) {
    super(message, cause);
  }

  public TrieException(Throwable cause) {
    super(cause);
  }

  protected TrieException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
