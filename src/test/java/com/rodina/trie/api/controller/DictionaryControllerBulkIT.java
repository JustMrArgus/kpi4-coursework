package com.rodina.trie.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.rodina.trie.api.dto.BulkDeleteRequest;
import com.rodina.trie.api.dto.BulkInsertRequest;
import com.rodina.trie.api.dto.InsertRequest;

@DisplayName("Dictionary Controller Bulk Operations Integration Tests")
class DictionaryControllerBulkIT extends AbstractDictionaryControllerIT {
  @Test
  @DisplayName("Should create and delete entries in bulk operations")
  void bulkOperations() throws Exception {
    BulkInsertRequest insertRequest =
        new BulkInsertRequest(
            Arrays.asList(
                new InsertRequest("key1", "value1"),
                new InsertRequest("key2", "value2"),
                new InsertRequest("key3", "value3")));
    mockMvc
        .perform(
            post("/api/v1/dictionary/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(insertRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.successCount").value(3));
    BulkDeleteRequest deleteRequest = new BulkDeleteRequest(Arrays.asList("key1", "key2", "key3"));
    mockMvc
        .perform(
            delete("/api/v1/dictionary/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deleteRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.successCount").value(3));
  }

  @Test
  @DisplayName("Should handle atomic delete failure and ignoreMissing flag")
  void bulkDeleteAtomicAndIgnoreMissing() throws Exception {
    insertEntry("existing", "value");
    BulkDeleteRequest atomicRequest =
        new BulkDeleteRequest(Arrays.asList("existing", "missing"), true, false);
    mockMvc
        .perform(
            delete("/api/v1/dictionary/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(atomicRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("FAILED"));
    BulkDeleteRequest ignoreMissingRequest =
        new BulkDeleteRequest(Arrays.asList("existing", "missing"), false, true);
    mockMvc
        .perform(
            delete("/api/v1/dictionary/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ignoreMissingRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUCCESS"));
  }

  @Test
  @DisplayName("Should reject empty bulk operations")
  void rejectsEmptyBulkOperations() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/dictionary/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new BulkInsertRequest(List.of()))))
        .andExpect(status().isBadRequest());
    mockMvc
        .perform(
            delete("/api/v1/dictionary/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new BulkDeleteRequest(List.of()))))
        .andExpect(status().isBadRequest());
  }
}
