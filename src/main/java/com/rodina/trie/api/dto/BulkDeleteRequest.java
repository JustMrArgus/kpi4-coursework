package com.rodina.trie.api.dto;

import java.util.List;
import java.util.Objects;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class BulkDeleteRequest {
  @NotEmpty(message = "Keys list cannot be empty")
  @Size(max = 1000, message = "Maximum 1000 keys allowed per bulk request")
  private List<String> keys;

  private boolean atomic = true;
  private boolean ignoreMissing = false;

  public BulkDeleteRequest() {}

  public BulkDeleteRequest(List<String> keys) {
    this.keys = keys;
  }

  public BulkDeleteRequest(List<String> keys, boolean atomic, boolean ignoreMissing) {
    this.keys = keys;
    this.atomic = atomic;
    this.ignoreMissing = ignoreMissing;
  }

  public List<String> getKeys() {
    return keys;
  }

  public void setKeys(List<String> keys) {
    this.keys = keys;
  }

  public boolean isAtomic() {
    return atomic;
  }

  public void setAtomic(boolean atomic) {
    this.atomic = atomic;
  }

  public boolean isIgnoreMissing() {
    return ignoreMissing;
  }

  public void setIgnoreMissing(boolean ignoreMissing) {
    this.ignoreMissing = ignoreMissing;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BulkDeleteRequest that = (BulkDeleteRequest) o;
    return atomic == that.atomic
        && ignoreMissing == that.ignoreMissing
        && Objects.equals(keys, that.keys);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keys, atomic, ignoreMissing);
  }

  @Override
  public String toString() {
    return "BulkDeleteRequest{"
        + "keys="
        + keys
        + ", atomic="
        + atomic
        + ", ignoreMissing="
        + ignoreMissing
        + '}';
  }
}
