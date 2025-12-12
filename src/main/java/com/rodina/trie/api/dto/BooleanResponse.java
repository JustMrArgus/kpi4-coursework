package com.rodina.trie.api.dto;

public class BooleanResponse {
  private boolean result;

  public BooleanResponse() {}

  public BooleanResponse(boolean result) {
    this.result = result;
  }

  public boolean isResult() {
    return result;
  }

  public void setResult(boolean result) {
    this.result = result;
  }
}
