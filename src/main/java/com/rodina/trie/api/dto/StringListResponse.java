package com.rodina.trie.api.dto;

import java.util.List;

public class StringListResponse {
  private List<String> items;
  private int count;

  public StringListResponse() {}

  public StringListResponse(List<String> items) {
    this.items = items;
    this.count = items != null ? items.size() : 0;
  }

  public List<String> getItems() {
    return items;
  }

  public void setItems(List<String> items) {
    this.items = items;
    this.count = items != null ? items.size() : 0;
  }

  public int getCount() {
    return count;
  }
}
