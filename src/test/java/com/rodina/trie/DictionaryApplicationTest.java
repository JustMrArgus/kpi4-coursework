package com.rodina.trie;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
@DisplayName("DictionaryApplication Tests")
class DictionaryApplicationTest {
  @Autowired private ApplicationContext context;

  @Test
  @DisplayName("Should load application context")
  void contextLoads() {
    assertThat(context).isNotNull();
  }

  @Test
  @DisplayName("Should have required beans")
  void hasRequiredBeans() {
    assertThat(context.containsBean("dictionaryService")).isTrue();
    assertThat(context.containsBean("dictionaryController")).isTrue();
  }
}
