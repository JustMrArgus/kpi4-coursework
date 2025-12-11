package com.rodina.trie.config;

import com.rodina.trie.contract.Trie;
import com.rodina.trie.core.impl.ConcurrentTrie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class TrieConfig {
  @Bean
  @Scope("singleton")
  public Trie<Object> concurrentTrie() {
    return new ConcurrentTrie<>();
  }
}
