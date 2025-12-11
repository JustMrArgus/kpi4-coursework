package com.rodina.trie.api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rodina.trie.api.dto.InsertRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@DisplayName("Dictionary Controller CRUD Integration Tests")
class DictionaryControllerCrudIT extends AbstractDictionaryControllerIT {

  @Test
  @DisplayName("Should make inserted entry searchable")
  void insertMakesEntrySearchable() throws Exception {
    insertEntry("apple", "fruit");
    mockMvc
        .perform(get("/api/v1/dictionary/apple"))
        .andExpect(status().isOk())
        .andExpect(content().string("fruit"));
  }

  @Test
  @DisplayName("Should return 404 when searching for missing key")
  void searchMissingKeyReturns404() throws Exception {
    mockMvc
        .perform(get("/api/v1/dictionary/missing"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error", is("Not Found")))
        .andExpect(jsonPath("$.message", is("Key not found: missing")));
  }

  @Test
  @DisplayName("Should remove entry when deleting existing key")
  void deleteRemovesEntry() throws Exception {
    insertEntry("planet", "earth");
    mockMvc.perform(delete("/api/v1/dictionary/planet")).andExpect(status().isNoContent());
    mockMvc.perform(get("/api/v1/dictionary/planet")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Should return 404 when deleting missing entry")
  void deleteMissingEntryReturns404() throws Exception {
    mockMvc.perform(delete("/api/v1/dictionary/ghost")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Should empty trie when calling clear endpoint")
  void clearEndpointEmptiesTrie() throws Exception {

    insertEntry("alpha", "a");
    insertEntry("beta", "b");

    mockMvc.perform(delete("/api/v1/dictionary/clear")).andExpect(status().isNoContent());

    mockMvc
        .perform(get("/api/v1/dictionary/keys"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @DisplayName("Should update value when inserting duplicate key")
  void duplicateInsertUpdatesValue() throws Exception {
    insertEntry("alpha", "first");
    mockMvc
        .perform(
            post("/api/v1/dictionary")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new InsertRequest("alpha", "second"))))
        .andExpect(status().isCreated());
    mockMvc
        .perform(get("/api/v1/dictionary/alpha"))
        .andExpect(status().isOk())
        .andExpect(content().string("second"));
  }
}
