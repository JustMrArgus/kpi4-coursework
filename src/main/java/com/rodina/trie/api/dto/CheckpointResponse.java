package com.rodina.trie.api.dto;

public class CheckpointResponse {
  private long checkpointId;

  public CheckpointResponse() {}

  public CheckpointResponse(long checkpointId) {
    this.checkpointId = checkpointId;
  }

  public long getCheckpointId() {
    return checkpointId;
  }

  public void setCheckpointId(long checkpointId) {
    this.checkpointId = checkpointId;
  }
}
