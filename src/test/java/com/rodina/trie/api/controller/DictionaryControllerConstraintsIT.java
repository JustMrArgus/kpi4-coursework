package com.rodina.trie.api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rodina.trie.api.dto.InsertRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@DisplayName("Dictionary Controller Constraints Integration Tests")
class DictionaryControllerConstraintsIT extends AbstractDictionaryControllerIT {
  @Nested
  @DisplayName("Limit Tests")
  class LimitTests {
    @Test
    @DisplayName("Should validate autocomplete limit parameter")
    void autocompleteLimitValidation() throws Exception {
      mockMvc
          .perform(get("/api/v1/dictionary/autocomplete").param("prefix", "a").param("limit", "0"))
          .andExpect(status().isBadRequest());
      mockMvc
          .perform(
              get("/api/v1/dictionary/autocomplete").param("prefix", "a").param("limit", "150"))
          .andExpect(status().isBadRequest());
      for (int i = 0; i < 12; i++) {
        insertEntry("pref-" + i, i);
      }
      mockMvc
          .perform(get("/api/v1/dictionary/autocomplete").param("prefix", "pref"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.items", hasSize(10)));
    }
  }

  @Nested
  @DisplayName("Validation Tests")
  class ValidationTests {
    @Test
    @DisplayName("Should validate insert request parameters")
    void insertValidation() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/dictionary")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(new InsertRequest("", "val"))))
          .andExpect(status().isBadRequest());
      mockMvc
          .perform(
              post("/api/v1/dictionary")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(new InsertRequest(null, "val"))))
          .andExpect(status().isBadRequest());
      mockMvc
          .perform(
              post("/api/v1/dictionary")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      objectMapper.writeValueAsString(new InsertRequest("a".repeat(300), "val"))))
          .andExpect(status().isBadRequest());
      mockMvc
          .perform(
              post("/api/v1/dictionary")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(new InsertRequest("alpha", null))))
          .andExpect(status().isBadRequest());
      mockMvc
          .perform(
              post("/api/v1/dictionary")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      objectMapper.writeValueAsString(
                          new InsertRequest("complex", new ComplexObject("nested", 123)))))
          .andExpect(status().isCreated());
    }

    private record ComplexObject(String name, int id) {}
  }
}
