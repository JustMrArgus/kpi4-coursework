package com.rodina.trie.benchmark;

import com.rodina.trie.core.impl.ConcurrentTrie;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class TrieBenchmark {

    private ConcurrentTrie<Integer> trie;
    private String[] existingKeys;
    private static final int KEY_COUNT = 100_000;

    @Setup(Level.Trial)
    public void setUp() {
        trie = new ConcurrentTrie<>();
        existingKeys = new String[KEY_COUNT];
        
        for (int i = 0; i < KEY_COUNT; i++) {
            String key = "key-" + i;
            existingKeys[i] = key;
            trie.insert(key, i);
        }
    }

    @Benchmark
    @Threads(4)
    public void searchExistingKeys(Blackhole bh) {
        int index = ThreadLocalRandom.current().nextInt(KEY_COUNT);
        String key = existingKeys[index];
        bh.consume(trie.search(key));
    }

    @Benchmark
    @Threads(4)
    public void insertNewKeys() {
        String newKey = "new-" + ThreadLocalRandom.current().nextInt(1_000_000_000);
        trie.insert(newKey, 1);
    }

    @Benchmark
    @Threads(4)
    public void mixedOperations(Blackhole bh) {
        int random = ThreadLocalRandom.current().nextInt(100);
        
        if (random < 80) {
            int index = ThreadLocalRandom.current().nextInt(KEY_COUNT);
            bh.consume(trie.search(existingKeys[index]));
        } else {
            String newKey = "mixed-" + ThreadLocalRandom.current().nextInt(1_000_000_000);
            trie.insert(newKey, 999);
        }
    }
}