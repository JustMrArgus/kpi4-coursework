package com.rodina.trie.api.service;

import com.rodina.trie.api.dto.BulkDeleteRequest;
import com.rodina.trie.api.dto.BulkInsertRequest;
import com.rodina.trie.api.dto.BulkOperationResponse;
import com.rodina.trie.api.dto.BulkOperationResponse.BulkOperationError;
import com.rodina.trie.api.dto.DictionaryEntryDto;
import com.rodina.trie.api.dto.InsertRequest;
import com.rodina.trie.contract.Trie;
import com.rodina.trie.core.impl.ConcurrentTrie;
import com.rodina.trie.core.transaction.DeleteCommand;
import com.rodina.trie.core.transaction.InsertCommand;
import com.rodina.trie.core.transaction.TransactionManager;
import com.rodina.trie.exception.NodeNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DictionaryService {
  private static final Logger logger = LoggerFactory.getLogger(DictionaryService.class);
  private final Trie<Object> trie;
  private final TransactionManager transactionManager;

  public DictionaryService(Trie<Object> trie, TransactionManager transactionManager) {
    this.trie = trie;
    this.transactionManager = transactionManager;
  }

  public void insert(InsertRequest request) {
    trie.insert(request.getKey(), request.getValue());
  }

  public Object search(String key) {
    return trie.search(key).orElseThrow(() -> new NodeNotFoundException("Key not found: " + key));
  }

  public void delete(String key) {
    boolean deleted = trie.delete(key);
    if (!deleted) {
      throw new NodeNotFoundException("Key not found for deletion: " + key);
    }
  }

  public boolean exists(String key) {
    return trie.has(key);
  }

  public List<String> autocomplete(String prefix, int limit) {
    return trie.autocomplete(prefix, limit);
  }

  public List<DictionaryEntryDto> searchByPrefix(String prefix) {
    List<Map.Entry<String, Object>> entries = trie.searchByPrefix(prefix);
    return entries.stream()
        .map(entry -> new DictionaryEntryDto(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }

  public BulkOperationResponse bulkInsert(BulkInsertRequest request) {
    List<InsertRequest> entries = request.getEntries();
    BulkOperationResponse response;
    if (request.isAtomic()) {
      response = executeBulkInsertAtomic(entries);
    } else {
      response = executeBulkInsertNonAtomic(entries);
    }
    return response;
  }

  private BulkOperationResponse executeBulkInsertAtomic(List<InsertRequest> entries) {
    int totalCount = entries.size();
    for (int i = 0; i < entries.size(); i++) {
      InsertRequest entry = entries.get(i);
      if (entry.getKey() == null || entry.getKey().isBlank()) {
        List<BulkOperationError> errors = new ArrayList<>();
        errors.add(new BulkOperationError(entry.getKey(), "Key cannot be blank", i));
        return BulkOperationResponse.failed(totalCount, errors);
      }
    }
    List<InsertCommand> commands = new ArrayList<>();
    try {
      for (InsertRequest entry : entries) {
        InsertCommand command = new InsertCommand(trie, entry.getKey(), entry.getValue());
        transactionManager.execute(command);
        commands.add(command);
      }
      logger.info("Bulk insert completed: {} entries inserted atomically", totalCount);
      return BulkOperationResponse.success(totalCount);
    } catch (Exception e) {
      for (int i = commands.size() - 1; i >= 0; i--) {
        try {
          transactionManager.undo();
        } catch (Exception rollbackEx) {
          logger.error("Error during rollback of bulk insert", rollbackEx);
        }
      }
      List<BulkOperationError> errors = new ArrayList<>();
      errors.add(new BulkOperationError(null, "Atomic operation failed: " + e.getMessage(), -1));
      return BulkOperationResponse.failed(totalCount, errors);
    }
  }

  private BulkOperationResponse executeBulkInsertNonAtomic(List<InsertRequest> entries) {
    List<BulkOperationError> errors = new ArrayList<>();
    int successCount = 0;
    for (int i = 0; i < entries.size(); i++) {
      InsertRequest entry = entries.get(i);
      try {
        if (entry.getKey() == null || entry.getKey().isBlank()) {
          errors.add(new BulkOperationError(entry.getKey(), "Key cannot be blank", i));
          continue;
        }
        trie.insert(entry.getKey(), entry.getValue());
        successCount++;
      } catch (Exception e) {
        errors.add(new BulkOperationError(entry.getKey(), e.getMessage(), i));
      }
    }
    int totalCount = entries.size();
    if (errors.isEmpty()) {
      logger.info("Bulk insert completed: {} entries inserted", successCount);
      return BulkOperationResponse.success(successCount);
    } else if (successCount > 0) {
      logger.info(
          "Bulk insert partially completed: {} of {} entries inserted", successCount, totalCount);
      return BulkOperationResponse.partialSuccess(successCount, errors.size(), errors);
    } else {
      logger.warn("Bulk insert failed: all {} entries failed", totalCount);
      return BulkOperationResponse.failed(totalCount, errors);
    }
  }

  public BulkOperationResponse bulkDelete(BulkDeleteRequest request) {
    List<String> keys = request.getKeys();
    BulkOperationResponse response;
    if (request.isAtomic()) {
      response = executeBulkDeleteAtomic(keys, request.isIgnoreMissing());
    } else {
      response = executeBulkDeleteNonAtomic(keys, request.isIgnoreMissing());
    }
    return response;
  }

  private BulkOperationResponse executeBulkDeleteAtomic(List<String> keys, boolean ignoreMissing) {
    int totalCount = keys.size();
    List<BulkOperationError> errors = new ArrayList<>();
    if (!ignoreMissing) {
      for (int i = 0; i < keys.size(); i++) {
        String key = keys.get(i);
        if (!trie.has(key)) {
          errors.add(new BulkOperationError(key, "Key not found", i));
        }
      }
      if (!errors.isEmpty()) {
        return BulkOperationResponse.failed(totalCount, errors);
      }
    }
    List<DeleteCommand> commands = new ArrayList<>();
    try {
      for (String key : keys) {
        if (trie.has(key)) {
          DeleteCommand command = new DeleteCommand(trie, key);
          transactionManager.execute(command);
          commands.add(command);
        }
      }
      logger.info("Bulk delete completed: {} entries deleted atomically", commands.size());
      return BulkOperationResponse.success(commands.size());
    } catch (Exception e) {
      for (int i = commands.size() - 1; i >= 0; i--) {
        try {
          transactionManager.undo();
        } catch (Exception rollbackEx) {
          logger.error("Error during rollback of bulk delete", rollbackEx);
        }
      }
      errors.add(new BulkOperationError(null, "Atomic operation failed: " + e.getMessage(), -1));
      return BulkOperationResponse.failed(totalCount, errors);
    }
  }

  private BulkOperationResponse executeBulkDeleteNonAtomic(
      List<String> keys, boolean ignoreMissing) {
    List<BulkOperationError> errors = new ArrayList<>();
    int successCount = 0;
    for (int i = 0; i < keys.size(); i++) {
      String key = keys.get(i);
      try {
        if (trie.has(key)) {
          trie.delete(key);
          successCount++;
        } else if (!ignoreMissing) {
          errors.add(new BulkOperationError(key, "Key not found", i));
        }
      } catch (Exception e) {
        errors.add(new BulkOperationError(key, e.getMessage(), i));
      }
    }
    int totalCount = keys.size();
    if (errors.isEmpty()) {
      logger.info("Bulk delete completed: {} entries deleted", successCount);
      return BulkOperationResponse.success(successCount);
    } else if (successCount > 0) {
      logger.info(
          "Bulk delete partially completed: {} of {} entries deleted", successCount, totalCount);
      return BulkOperationResponse.partialSuccess(successCount, errors.size(), errors);
    } else {
      logger.warn("Bulk delete failed: all {} entries failed", totalCount);
      return BulkOperationResponse.failed(totalCount, errors);
    }
  }

  public void clear() {
    trie.clear();
  }

  public boolean startsWith(String prefix) {
    return trie.startsWith(prefix);
  }

  public List<String> getAllKeys() {
    return trie.getAllKeys();
  }

  public long createCheckpoint() {
    if (!(trie instanceof ConcurrentTrie)) {
      throw new UnsupportedOperationException("Checkpoints are only supported with ConcurrentTrie");
    }
    ConcurrentTrie<Object> concurrentTrie = (ConcurrentTrie<Object>) trie;
    long checkpointId = concurrentTrie.createSnapshot();
    logger.info("Memory checkpoint created: {}", checkpointId);
    return checkpointId;
  }

  public boolean rollbackToCheckpoint(long checkpointId) {
    if (!(trie instanceof ConcurrentTrie)) {
      throw new UnsupportedOperationException("Checkpoints are only supported with ConcurrentTrie");
    }
    ConcurrentTrie<Object> concurrentTrie = (ConcurrentTrie<Object>) trie;
    boolean success = concurrentTrie.rollbackToSnapshot(checkpointId);
    if (success) {
      logger.info("Rolled back to checkpoint: {}", checkpointId);
    } else {
      logger.warn("Failed to rollback to checkpoint: {}", checkpointId);
    }
    return success;
  }

  public Map<Long, Integer> listCheckpoints() {
    if (!(trie instanceof ConcurrentTrie)) {
      throw new UnsupportedOperationException("Checkpoints are only supported with ConcurrentTrie");
    }
    ConcurrentTrie<Object> concurrentTrie = (ConcurrentTrie<Object>) trie;
    return concurrentTrie.getSnapshots().entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getSize()));
  }

  public boolean deleteCheckpoint(long checkpointId) {
    if (!(trie instanceof ConcurrentTrie)) {
      throw new UnsupportedOperationException("Checkpoints are only supported with ConcurrentTrie");
    }
    ConcurrentTrie<Object> concurrentTrie = (ConcurrentTrie<Object>) trie;
    boolean deleted = concurrentTrie.deleteSnapshot(checkpointId);
    if (deleted) {
      logger.info("Checkpoint deleted: {}", checkpointId);
    }
    return deleted;
  }
}
