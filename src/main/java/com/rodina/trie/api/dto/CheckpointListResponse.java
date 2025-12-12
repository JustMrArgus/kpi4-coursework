package com.rodina.trie.api.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CheckpointListResponse {
  private List<CheckpointDto> checkpoints;

  public CheckpointListResponse() {
    this.checkpoints = new ArrayList<>();
  }

  public CheckpointListResponse(Map<Long, Integer> checkpointsMap) {
    this.checkpoints = new ArrayList<>();
    if (checkpointsMap != null) {
      checkpointsMap.forEach((id, size) -> this.checkpoints.add(new CheckpointDto(id, size)));
    }
  }

  public List<CheckpointDto> getCheckpoints() {
    return checkpoints;
  }

  public void setCheckpoints(List<CheckpointDto> checkpoints) {
    this.checkpoints = checkpoints;
  }

  public static class CheckpointDto {
    private long id;
    private int size;

    public CheckpointDto() {}

    public CheckpointDto(long id, int size) {
      this.id = id;
      this.size = size;
    }

    public long getId() {
      return id;
    }

    public void setId(long id) {
      this.id = id;
    }

    public int getSize() {
      return size;
    }

    public void setSize(int size) {
      this.size = size;
    }
  }
}
