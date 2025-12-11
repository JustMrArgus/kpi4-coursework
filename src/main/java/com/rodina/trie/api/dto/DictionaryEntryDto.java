package com.rodina.trie.api.dto;

import java.util.Objects;

public class DictionaryEntryDto {
  private String key;
  private Object value;

  public DictionaryEntryDto() {}

  public DictionaryEntryDto(String key, Object value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DictionaryEntryDto that = (DictionaryEntryDto) o;
    return Objects.equals(key, that.key) && Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, value);
  }

  @Override
  public String toString() {
    return "DictionaryEntryDto{" + "key='" + key + '\'' + ", value=" + value + '}';
  }
}
