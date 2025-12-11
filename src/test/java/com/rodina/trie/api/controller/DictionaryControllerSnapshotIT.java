package com.rodina.trie.api.controller;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Dictionary Controller Snapshot Integration Tests")
class DictionaryControllerSnapshotIT extends AbstractDictionaryControllerIT {

  @Test
  @DisplayName("Should create snapshot, modify data, and rollback successfully")
  void snapshotRollbackLifecycle() throws Exception {
    insertEntry("original", "value1");

    String responseString =
        mockMvc
            .perform(post("/api/v1/dictionary/checkpoints"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    long checkpointId = Long.parseLong(responseString);

    insertEntry("new", "value2");
    mockMvc.perform(delete("/api/v1/dictionary/original")).andExpect(status().isNoContent());

    mockMvc.perform(get("/api/v1/dictionary/original")).andExpect(status().isNotFound());
    mockMvc.perform(get("/api/v1/dictionary/new")).andExpect(status().isOk());

    mockMvc
        .perform(post("/api/v1/dictionary/checkpoints/" + checkpointId + "/rollback"))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("/api/v1/dictionary/original"))
        .andExpect(status().isOk())
        .andExpect(content().string("value1"));

    mockMvc.perform(get("/api/v1/dictionary/new")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Should handle snapshot of empty trie correctly")
  void snapshotEmptyTrieAndRollback() throws Exception {
    clearDictionary();

    String responseString =
        mockMvc
            .perform(post("/api/v1/dictionary/checkpoints"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    long checkpointId = Long.parseLong(responseString);

    insertEntry("dirty", "data");
    mockMvc.perform(get("/api/v1/dictionary/dirty")).andExpect(status().isOk());

    mockMvc
        .perform(post("/api/v1/dictionary/checkpoints/" + checkpointId + "/rollback"))
        .andExpect(status().isOk());

    mockMvc.perform(get("/api/v1/dictionary/dirty")).andExpect(status().isNotFound());
    mockMvc.perform(get("/api/v1/dictionary/keys")).andExpect(jsonPath("$").isEmpty());
  }

  @Test
  @DisplayName("Should list created checkpoints with correct sizes")
  void listCheckpoints() throws Exception {
    insertEntry("a", "1");
    String cp1Str =
        mockMvc
            .perform(post("/api/v1/dictionary/checkpoints"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    insertEntry("b", "2");
    String cp2Str =
        mockMvc
            .perform(post("/api/v1/dictionary/checkpoints"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    mockMvc
        .perform(get("/api/v1/dictionary/checkpoints"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasKey(cp1Str)))
        .andExpect(jsonPath("$." + cp1Str, is(1)))
        .andExpect(jsonPath("$", hasKey(cp2Str)))
        .andExpect(jsonPath("$." + cp2Str, is(2)));
  }

  @Test
  @DisplayName("Should delete existing checkpoint")
  void deleteCheckpoint() throws Exception {
    String responseString =
        mockMvc
            .perform(post("/api/v1/dictionary/checkpoints"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    long checkpointId = Long.parseLong(responseString);

    mockMvc
        .perform(delete("/api/v1/dictionary/checkpoints/" + checkpointId))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(get("/api/v1/dictionary/checkpoints"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$." + checkpointId).doesNotExist());
  }

  @Test
  @DisplayName("Should fail when rolling back to a deleted checkpoint")
  void rollbackToDeletedCheckpointReturns404() throws Exception {
    String responseString =
        mockMvc
            .perform(post("/api/v1/dictionary/checkpoints"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    long checkpointId = Long.parseLong(responseString);

    mockMvc
        .perform(delete("/api/v1/dictionary/checkpoints/" + checkpointId))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(post("/api/v1/dictionary/checkpoints/" + checkpointId + "/rollback"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Should return 404 when rolling back to non-existent checkpoint ID")
  void rollbackMissingCheckpointReturns404() throws Exception {
    mockMvc
        .perform(post("/api/v1/dictionary/checkpoints/999999/rollback"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Should return 404 when deleting non-existent checkpoint ID")
  void deleteMissingCheckpointReturns404() throws Exception {
    mockMvc
        .perform(delete("/api/v1/dictionary/checkpoints/999999"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Should handle negative checkpoint ID gracefully")
  void negativeCheckpointIdHandling() throws Exception {
    mockMvc
        .perform(post("/api/v1/dictionary/checkpoints/-1/rollback"))
        .andExpect(status().isNotFound());

    mockMvc.perform(delete("/api/v1/dictionary/checkpoints/-1")).andExpect(status().isNotFound());
  }
}
