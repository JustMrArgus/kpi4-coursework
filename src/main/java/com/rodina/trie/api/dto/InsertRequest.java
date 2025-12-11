package com.rodina.trie.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Objects;

public class InsertRequest {
  @NotBlank(message = "Key cannot be blank")
  @Size(min = 1, max = 255, message = "Key length must be between 1 and 255")
  private String key;

  @NotNull(message = "Value cannot be null")
  private Object value;

  public InsertRequest() {}

  public InsertRequest(String key, Object value) {
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
    InsertRequest that = (InsertRequest) o;
    return Objects.equals(key, that.key) && Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, value);
  }

  @Override
  public String toString() {
    return "InsertRequest{" + "key='" + key + '\'' + ", value=" + value + '}';
  }
}
