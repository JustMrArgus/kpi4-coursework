package com.rodina.trie.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.rodina.trie.contract.Trie;
import com.rodina.trie.core.impl.ConcurrentTrie;

@Configuration
public class TrieConfig {
  @Bean
  @Scope("singleton")
  public Trie<Object> concurrentTrie() {
    return new ConcurrentTrie<>();
  }
}
