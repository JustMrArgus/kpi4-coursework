package com.rodina.trie.exception;

public class InvalidKeyException extends TrieException {
  public InvalidKeyException() {
    super();
  }

  public InvalidKeyException(String message) {
    super(message);
  }

  public InvalidKeyException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidKeyException(Throwable cause) {
    super(cause);
  }
}
