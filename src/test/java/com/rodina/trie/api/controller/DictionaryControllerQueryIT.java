package com.rodina.trie.api.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Dictionary Controller Query Integration Tests")
class DictionaryControllerQueryIT extends AbstractDictionaryControllerIT {

  @Nested
  @DisplayName("Basic Query Tests")
  class BasicQueryTests {
    @Test
    @DisplayName("Should return correct existence status for keys")
    void existsEndpointReflectsState() throws Exception {
      insertEntry("alpha", "value");
      mockMvc
          .perform(get("/api/v1/dictionary/exists/alpha"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", is(true)));
      mockMvc
          .perform(get("/api/v1/dictionary/exists/omega"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", is(false)));
    }

    @Test
    @DisplayName("Should limit autocomplete results to specified limit")
    void autocompleteRespectsLimit() throws Exception {
      insertEntry("app", "application");
      insertEntry("apple", "fruit");
      insertEntry("apricot", "fruit");
      mockMvc
          .perform(get("/api/v1/dictionary/autocomplete").param("prefix", "ap").param("limit", "2"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(2)))
          .andExpect(jsonPath("$[*]", everyItem(startsWith("ap"))));
    }

    @Test
    @DisplayName("Should return all entries matching prefix in prefix search")
    void searchByPrefixReturnsMatches() throws Exception {
      insertEntry("color", "red");
      insertEntry("cold", "ice");
      insertEntry("heat", "fire");
      mockMvc
          .perform(get("/api/v1/dictionary/prefix").param("prefix", "co"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(2)))
          .andExpect(jsonPath("$[*].key", containsInAnyOrder("color", "cold")));
    }
  }

  @Nested
  @DisplayName("Unicode Tests")
  class UnicodeTests {
    @Test
    @DisplayName("Should insert and search entries with Unicode keys")
    void insertAndSearchUnicodeKey() throws Exception {
      String key = "ключ-東京";
      insertEntry(key, "привіт");
      mockMvc
          .perform(get("/api/v1/dictionary/{key}", key))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", is("привіт")));
    }

    @Test
    @DisplayName("Should return autocomplete suggestions for Unicode prefixes")
    void autocompleteSupportsUnicodePrefixes() throws Exception {
      insertEntry("кіт", "cat");
      insertEntry("кімната", "room");
      insertEntry("київ", "city");
      mockMvc
          .perform(get("/api/v1/dictionary/autocomplete").param("prefix", "кі").param("limit", "5"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(2)))
          .andExpect(jsonPath("$[*]", everyItem(startsWith("кі"))));
    }
  }
}
