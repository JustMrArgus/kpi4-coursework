package com.rodina.trie.api.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BulkOperationResponse {
  public enum Status {
    SUCCESS,
    PARTIAL_SUCCESS,
    FAILED
  }

  private Status status;
  private int successCount;
  private int failedCount;
  private int totalCount;
  private List<BulkOperationError> errors;

  public BulkOperationResponse() {
    this.errors = new ArrayList<>();
  }

  public BulkOperationResponse(Status status, int successCount, int failedCount, int totalCount) {
    this.status = status;
    this.successCount = successCount;
    this.failedCount = failedCount;
    this.totalCount = totalCount;
    this.errors = new ArrayList<>();
  }

  public BulkOperationResponse(
      Status status,
      int successCount,
      int failedCount,
      int totalCount,
      List<BulkOperationError> errors) {
    this.status = status;
    this.successCount = successCount;
    this.failedCount = failedCount;
    this.totalCount = totalCount;
    this.errors = errors != null ? errors : new ArrayList<>();
  }

  public static BulkOperationResponse success(int count) {
    return new BulkOperationResponse(Status.SUCCESS, count, 0, count);
  }

  public static BulkOperationResponse partialSuccess(
      int successCount, int failedCount, List<BulkOperationError> errors) {
    return new BulkOperationResponse(
        Status.PARTIAL_SUCCESS, successCount, failedCount, successCount + failedCount, errors);
  }

  public static BulkOperationResponse failed(int count, List<BulkOperationError> errors) {
    return new BulkOperationResponse(Status.FAILED, 0, count, count, errors);
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public int getSuccessCount() {
    return successCount;
  }

  public void setSuccessCount(int successCount) {
    this.successCount = successCount;
  }

  public int getFailedCount() {
    return failedCount;
  }

  public void setFailedCount(int failedCount) {
    this.failedCount = failedCount;
  }

  public int getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(int totalCount) {
    this.totalCount = totalCount;
  }

  public List<BulkOperationError> getErrors() {
    return errors;
  }

  public void setErrors(List<BulkOperationError> errors) {
    this.errors = errors;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BulkOperationResponse that = (BulkOperationResponse) o;
    return successCount == that.successCount
        && failedCount == that.failedCount
        && totalCount == that.totalCount
        && status == that.status
        && Objects.equals(errors, that.errors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, successCount, failedCount, totalCount, errors);
  }

  @Override
  public String toString() {
    return "BulkOperationResponse{"
        + "status="
        + status
        + ", successCount="
        + successCount
        + ", failedCount="
        + failedCount
        + ", totalCount="
        + totalCount
        + ", errors="
        + errors
        + '}';
  }

  public static class BulkOperationError {
    private String key;
    private String message;
    private int index;

    public BulkOperationError() {}

    public BulkOperationError(String key, String message, int index) {
      this.key = key;
      this.message = message;
      this.index = index;
    }

    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }

    public int getIndex() {
      return index;
    }

    public void setIndex(int index) {
      this.index = index;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      BulkOperationError that = (BulkOperationError) o;
      return index == that.index
          && Objects.equals(key, that.key)
          && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
      return Objects.hash(key, message, index);
    }

    @Override
    public String toString() {
      return "BulkOperationError{"
          + "key='"
          + key
          + '\''
          + ", message='"
          + message
          + '\''
          + ", index="
          + index
          + '}';
    }
  }
}
