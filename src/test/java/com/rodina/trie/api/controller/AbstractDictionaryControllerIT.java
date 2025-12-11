package com.rodina.trie.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodina.trie.api.dto.InsertRequest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
abstract class AbstractDictionaryControllerIT {
  @Autowired protected MockMvc mockMvc;
  @Autowired protected ObjectMapper objectMapper;

  @BeforeEach
  void resetState() throws Exception {
    clearDictionary();
  }

  protected void clearDictionary() throws Exception {
    mockMvc.perform(delete("/api/v1/dictionary/clear")).andExpect(status().isNoContent());
  }

  protected void insertEntry(String key, Object value) throws Exception {
    InsertRequest request = new InsertRequest(key, value);
    mockMvc
        .perform(
            post("/api/v1/dictionary")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }
}
