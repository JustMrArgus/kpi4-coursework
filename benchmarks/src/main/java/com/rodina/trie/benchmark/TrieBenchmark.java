package com.rodina.trie.benchmark;

import com.rodina.trie.core.impl.ConcurrentTrie;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class TrieBenchmark {

  private ConcurrentTrie<Integer> trie;
  private String[] existingKeys;

  @Param({"10000", "100000"})
  private int size;

  @Setup(Level.Trial)
  public void setUp() {
    trie = new ConcurrentTrie<>();
    existingKeys = new String[size];

    for (int i = 0; i < size; i++) {
      String key = "key-" + i;
      existingKeys[i] = key;
      trie.insert(key, i);
    }
  }

  @Benchmark
  @Threads(1)
  public void readOnly_1_Thread(Blackhole bh) {
    performSearch(bh);
  }

  @Benchmark
  @Threads(4)
  public void readOnly_4_Threads(Blackhole bh) {
    performSearch(bh);
  }

  @Benchmark
  @Threads(8)
  public void readOnly_8_Threads(Blackhole bh) {
    performSearch(bh);
  }

  @Benchmark
  @Threads(4)
  public void writeOnly_4_Threads() {
    performInsert();
  }

  @Benchmark
  @Threads(8)
  public void mixed_90Read_10Write_8_Threads(Blackhole bh) {
    int random = ThreadLocalRandom.current().nextInt(100);
    if (random < 90) {
      performSearch(bh);
    } else {
      performInsert();
    }
  }

  @Benchmark
  @Threads(8)
  public void mixed_50Read_50Write_8_Threads(Blackhole bh) {
    int random = ThreadLocalRandom.current().nextInt(100);
    if (random < 50) {
      performSearch(bh);
    } else {
      performInsert();
    }
  }

  private void performSearch(Blackhole bh) {
    int index = ThreadLocalRandom.current().nextInt(size);
    String key = existingKeys[index];
    bh.consume(trie.search(key));
  }

  private void performInsert() {
    String newKey = "new-" + ThreadLocalRandom.current().nextInt(1_000_000_000);
    trie.insert(newKey, 1);
  }
}
