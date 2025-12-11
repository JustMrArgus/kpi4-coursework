package com.rodina.trie.api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Dictionary Controller Pagination Integration Tests")
class DictionaryControllerPaginationIT extends AbstractDictionaryControllerIT {
  @Test
  @DisplayName("Should return paginated prefix search results with correct metadata")
  void searchByPrefixPagedReturnsPaginatedResults() throws Exception {
    for (int i = 1; i <= 25; i++) {
      insertEntry("test" + String.format("%02d", i), "value" + i);
    }
    mockMvc
        .perform(
            get("/api/v1/dictionary/prefix/paged")
                .param("prefix", "test")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(10)))
        .andExpect(jsonPath("$.totalPages").value(3))
        .andExpect(jsonPath("$.hasNext").value(true))
        .andExpect(jsonPath("$.hasPrevious").value(false));
    mockMvc
        .perform(
            get("/api/v1/dictionary/prefix/paged")
                .param("prefix", "test")
                .param("page", "2")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(5)))
        .andExpect(jsonPath("$.hasNext").value(false))
        .andExpect(jsonPath("$.hasPrevious").value(true));
  }

  @Test
  @DisplayName("Should validate pagination parameters")
  void validatesPaginationParameters() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/dictionary/prefix/paged").param("prefix", "test").param("size", "150"))
        .andExpect(status().isBadRequest());
    mockMvc
        .perform(get("/api/v1/dictionary/prefix/paged").param("prefix", "test").param("size", "0"))
        .andExpect(status().isBadRequest());
    mockMvc
        .perform(get("/api/v1/dictionary/prefix/paged").param("prefix", "test").param("page", "-1"))
        .andExpect(status().isBadRequest());
  }
}
