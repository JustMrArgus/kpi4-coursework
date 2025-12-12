package com.rodina.trie.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rodina.trie.api.dto.BulkDeleteRequest;
import com.rodina.trie.api.dto.BulkInsertRequest;
import com.rodina.trie.api.dto.BulkOperationResponse;
import com.rodina.trie.api.dto.DictionaryEntryDto;
import com.rodina.trie.api.dto.InsertRequest;
import com.rodina.trie.core.impl.ConcurrentTrie;
import com.rodina.trie.core.transaction.TransactionManager;
import com.rodina.trie.exception.NodeNotFoundException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Dictionary Service Unit Tests")
class DictionaryServiceTest {
  private DictionaryService service;

  @BeforeEach
  void setUp() {
    service = new DictionaryService(new ConcurrentTrie<>(), new TransactionManager());
  }

  @Test
  @DisplayName("Basic CRUD operations work correctly")
  void basicCrudOperations() {
    service.insert(new InsertRequest("alpha", "value"));
    assertThat(service.search("alpha")).isEqualTo("value");

    service.delete("alpha");
    assertThat(service.exists("alpha")).isFalse();

    assertThatThrownBy(() -> service.search("missing")).isInstanceOf(NodeNotFoundException.class);

    assertThatThrownBy(() -> service.delete("ghost")).isInstanceOf(NodeNotFoundException.class);
  }

  @Test
  @DisplayName("Autocomplete and prefix search work correctly")
  void autocompleteAndPrefixSearch() {
    service.insert(new InsertRequest("apple", 1));
    service.insert(new InsertRequest("application", 2));
    service.insert(new InsertRequest("banana", 3));

    List<String> suggestions = service.autocomplete("app", 2);
    assertThat(suggestions).hasSizeLessThanOrEqualTo(2).allMatch(key -> key.startsWith("app"));

    service.insert(new InsertRequest("color", "red"));
    service.insert(new InsertRequest("cold", "ice"));
    List<DictionaryEntryDto> entries = service.searchByPrefix("co");
    assertThat(entries).hasSize(2);
  }

  @Test
  @DisplayName("Clear operation works correctly")
  void clearOperationWorks() {

    service.insert(new InsertRequest("alpha", 1));
    service.insert(new InsertRequest("alpine", 2));

    assertThat(service.getAllKeys()).hasSize(2);

    service.clear();

    assertThat(service.getAllKeys()).isEmpty();
    assertThat(service.exists("alpha")).isFalse();
    assertThat(service.exists("alpine")).isFalse();
  }

  @Nested
  @DisplayName("Checkpoint Tests")
  class CheckpointTests {
    @BeforeEach
    void setUpData() {
      service.insert(new InsertRequest("alpha", "value1"));
      service.insert(new InsertRequest("beta", "value2"));
    }

    @Test
    @DisplayName("Checkpoint create, list, rollback, and delete operations work")
    void checkpointOperations() {
      long cp1 = service.createCheckpoint();
      assertThat(cp1).isGreaterThan(0);
      assertThat(service.listCheckpoints()).containsKey(cp1);

      service.insert(new InsertRequest("gamma", "value3"));
      long cp2 = service.createCheckpoint();
      assertThat(cp1).isNotEqualTo(cp2);

      Map<Long, Integer> checkpoints = service.listCheckpoints();
      assertThat(checkpoints.get(cp1)).isEqualTo(2);
      assertThat(checkpoints.get(cp2)).isEqualTo(3);

      assertThat(service.exists("gamma")).isTrue();
      assertThat(service.rollbackToCheckpoint(cp1)).isTrue();
      assertThat(service.exists("gamma")).isFalse();
      assertThat(service.exists("alpha")).isTrue();

      long newCp = service.createCheckpoint();
      assertThat(service.deleteCheckpoint(newCp)).isTrue();
      assertThat(service.listCheckpoints()).doesNotContainKey(newCp);

      assertThat(service.rollbackToCheckpoint(999999L)).isFalse();
      assertThat(service.deleteCheckpoint(999999L)).isFalse();
    }
  }

  @Test
  @DisplayName("StartsWith and getAllKeys work correctly")
  void startsWithAndGetAllKeys() {
    service.insert(new InsertRequest("application", "software"));
    service.insert(new InsertRequest("banana", "fruit"));
    service.insert(new InsertRequest("hello", "world"));

    assertThat(service.startsWith("app")).isTrue();
    assertThat(service.startsWith("xyz")).isFalse();
    assertThat(service.startsWith("hello")).isTrue();

    List<String> keys = service.getAllKeys();
    assertThat(keys).containsExactlyInAnyOrder("application", "banana", "hello");

    service.clear();
    assertThat(service.getAllKeys()).isEmpty();
  }

  @Nested
  @DisplayName("Bulk Insert Tests")
  class BulkInsertTests {
    @Test
    @DisplayName("Bulk insert operations work in different modes")
    void bulkInsertOperations() {
      BulkInsertRequest nonAtomicReq =
          new BulkInsertRequest(
              List.of(new InsertRequest("key1", "value1"), new InsertRequest("key2", "value2")));
      nonAtomicReq.setAtomic(false);
      BulkOperationResponse resp = service.bulkInsert(nonAtomicReq);
      assertThat(resp.getStatus()).isEqualTo(BulkOperationResponse.Status.SUCCESS);
      assertThat(resp.getSuccessCount()).isEqualTo(2);

      service.clear();

      BulkInsertRequest atomicReq =
          new BulkInsertRequest(
              List.of(new InsertRequest("alpha", "a"), new InsertRequest("beta", "b")));
      atomicReq.setAtomic(true);
      resp = service.bulkInsert(atomicReq);
      assertThat(resp.getStatus()).isEqualTo(BulkOperationResponse.Status.SUCCESS);

      service.clear();

      BulkInsertRequest atomicFailReq =
          new BulkInsertRequest(
              List.of(new InsertRequest("valid", "value"), new InsertRequest("", "invalid")));
      atomicFailReq.setAtomic(true);
      resp = service.bulkInsert(atomicFailReq);
      assertThat(resp.getStatus()).isEqualTo(BulkOperationResponse.Status.FAILED);
      assertThat(service.exists("valid")).isFalse();

      BulkInsertRequest partialReq =
          new BulkInsertRequest(
              List.of(
                  new InsertRequest("good1", "v1"),
                  new InsertRequest("", "bad"),
                  new InsertRequest("good2", "v2")));
      partialReq.setAtomic(false);
      resp = service.bulkInsert(partialReq);
      assertThat(resp.getStatus()).isEqualTo(BulkOperationResponse.Status.PARTIAL_SUCCESS);
      assertThat(resp.getSuccessCount()).isEqualTo(2);
    }
  }

  @Nested
  @DisplayName("Bulk Delete Tests")
  class BulkDeleteTests {
    @BeforeEach
    void setUpData() {
      service.insert(new InsertRequest("delete1", "value1"));
      service.insert(new InsertRequest("delete2", "value2"));
      service.insert(new InsertRequest("delete3", "value3"));
    }

    @Test
    @DisplayName("Bulk delete operations work in different modes")
    void bulkDeleteOperations() {
      BulkDeleteRequest nonAtomicReq =
          new BulkDeleteRequest(List.of("delete1", "delete2"), false, false);
      BulkOperationResponse resp = service.bulkDelete(nonAtomicReq);
      assertThat(resp.getStatus()).isEqualTo(BulkOperationResponse.Status.SUCCESS);
      assertThat(service.exists("delete1")).isFalse();

      service.insert(new InsertRequest("delete1", "value1"));
      service.insert(new InsertRequest("delete2", "value2"));

      BulkDeleteRequest atomicFailReq =
          new BulkDeleteRequest(List.of("delete1", "nonexistent"), true, false);
      resp = service.bulkDelete(atomicFailReq);
      assertThat(resp.getStatus()).isEqualTo(BulkOperationResponse.Status.FAILED);
      assertThat(service.exists("delete1")).isTrue();

      BulkDeleteRequest ignoreMissingReq =
          new BulkDeleteRequest(List.of("delete1", "nonexistent"), true, true);
      resp = service.bulkDelete(ignoreMissingReq);
      assertThat(resp.getStatus()).isEqualTo(BulkOperationResponse.Status.SUCCESS);

      service.insert(new InsertRequest("delete1", "value1"));
      BulkDeleteRequest partialReq =
          new BulkDeleteRequest(List.of("delete1", "missing", "delete2"), false, false);
      resp = service.bulkDelete(partialReq);
      assertThat(resp.getStatus()).isEqualTo(BulkOperationResponse.Status.PARTIAL_SUCCESS);
    }
  }
}
