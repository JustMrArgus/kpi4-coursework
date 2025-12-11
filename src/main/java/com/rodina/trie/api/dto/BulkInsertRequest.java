package com.rodina.trie.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

public class BulkInsertRequest {
  @NotEmpty(message = "Entries list cannot be empty")
  @Size(max = 1000, message = "Maximum 1000 entries allowed per bulk request")
  @Valid
  private List<InsertRequest> entries;

  private boolean atomic = true;

  public BulkInsertRequest() {}

  public BulkInsertRequest(List<InsertRequest> entries) {
    this.entries = entries;
  }

  public BulkInsertRequest(List<InsertRequest> entries, boolean atomic) {
    this.entries = entries;
    this.atomic = atomic;
  }

  public List<InsertRequest> getEntries() {
    return entries;
  }

  public void setEntries(List<InsertRequest> entries) {
    this.entries = entries;
  }

  public boolean isAtomic() {
    return atomic;
  }

  public void setAtomic(boolean atomic) {
    this.atomic = atomic;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BulkInsertRequest that = (BulkInsertRequest) o;
    return atomic == that.atomic && Objects.equals(entries, that.entries);
  }

  @Override
  public int hashCode() {
    return Objects.hash(entries, atomic);
  }

  @Override
  public String toString() {
    return "BulkInsertRequest{" + "entries=" + entries + ", atomic=" + atomic + '}';
  }
}
