package com.rodina.trie.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.rodina.trie.api.dto.InsertRequest;

@DisplayName("Dictionary Controller Load Integration Tests")
class DictionaryControllerLoadIT extends AbstractDictionaryControllerIT {

  @Test
  @DisplayName("Should handle concurrent REST requests without errors")
  void handlesConcurrentRestRequests() throws InterruptedException {
    int threads = 20;
    int requestsPerThread = 100;
    ExecutorService service = Executors.newFixedThreadPool(threads);
    CountDownLatch latch = new CountDownLatch(threads);
    AtomicInteger errorCount = new AtomicInteger(0);
    AtomicInteger successCount = new AtomicInteger(0);

    for (int i = 0; i < threads; i++) {
      final int threadId = i;
      service.submit(
          () -> {
            try {
              for (int j = 0; j < requestsPerThread; j++) {
                String key = "user-" + threadId + "-" + j;
                InsertRequest request = new InsertRequest(key, "data-" + j);
                try {
                  mockMvc
                      .perform(
                          post("/api/v1/dictionary")
                              .contentType(MediaType.APPLICATION_JSON)
                              .content(objectMapper.writeValueAsString(request)))
                      .andExpect(status().isCreated());
                  mockMvc.perform(get("/api/v1/dictionary/" + key)).andExpect(status().isOk());
                  successCount.incrementAndGet();
                } catch (Exception e) {
                  errorCount.incrementAndGet();
                }
              }
            } finally {
              latch.countDown();
            }
          });
    }
    latch.await();
    service.shutdown();

    assertThat(errorCount).hasValue(0);
    assertThat(successCount).hasValue(threads * requestsPerThread);

    try {
      mockMvc.perform(get("/api/v1/dictionary/keys")).andExpect(status().isOk());
    } catch (Exception e) {
      throw new AssertionError("Failed to verify service liveness after load", e);
    }
  }
}
