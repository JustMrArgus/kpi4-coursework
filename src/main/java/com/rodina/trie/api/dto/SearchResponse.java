package com.rodina.trie.api.dto;

public class SearchResponse {
  private Object result;

  public SearchResponse() {}

  public SearchResponse(Object result) {
    this.result = result;
  }

  public Object getResult() {
    return result;
  }

  public void setResult(Object result) {
    this.result = result;
  }
}
