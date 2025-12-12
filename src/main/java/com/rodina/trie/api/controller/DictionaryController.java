package com.rodina.trie.api.controller;

import com.rodina.trie.api.dto.*;
import com.rodina.trie.api.service.DictionaryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/dictionary")
public class DictionaryController {
  private final DictionaryService dictionaryService;

  public DictionaryController(DictionaryService dictionaryService) {
    this.dictionaryService = dictionaryService;
  }

  @PostMapping
  public ResponseEntity<Void> insert(@Valid @RequestBody InsertRequest request) {
    dictionaryService.insert(request);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @PostMapping("/bulk")
  public ResponseEntity<BulkOperationResponse> bulkInsert(
      @Valid @RequestBody BulkInsertRequest request) {
    BulkOperationResponse response = dictionaryService.bulkInsert(request);
    HttpStatus status =
        switch (response.getStatus()) {
          case SUCCESS -> HttpStatus.CREATED;
          case PARTIAL_SUCCESS -> HttpStatus.MULTI_STATUS;
          case FAILED -> HttpStatus.BAD_REQUEST;
        };
    return new ResponseEntity<>(response, status);
  }

  @GetMapping("/{key}")
  public ResponseEntity<SearchResponse> search(@PathVariable String key) {
    Object result = dictionaryService.search(key);
    return ResponseEntity.ok(new SearchResponse(result));
  }

  @DeleteMapping("/{key}")
  public ResponseEntity<Void> delete(@PathVariable String key) {
    dictionaryService.delete(key);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/bulk")
  public ResponseEntity<BulkOperationResponse> bulkDelete(
      @Valid @RequestBody BulkDeleteRequest request) {
    BulkOperationResponse response = dictionaryService.bulkDelete(request);
    HttpStatus status =
        switch (response.getStatus()) {
          case SUCCESS -> HttpStatus.OK;
          case PARTIAL_SUCCESS -> HttpStatus.MULTI_STATUS;
          case FAILED -> HttpStatus.BAD_REQUEST;
        };
    return new ResponseEntity<>(response, status);
  }

  @GetMapping("/exists/{key}")
  public ResponseEntity<BooleanResponse> exists(@PathVariable String key) {
    boolean exists = dictionaryService.exists(key);
    return ResponseEntity.ok(new BooleanResponse(exists));
  }

  @GetMapping("/autocomplete")
  public ResponseEntity<StringListResponse> autocomplete(
      @RequestParam String prefix, @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
    List<String> result = dictionaryService.autocomplete(prefix, limit);
    return ResponseEntity.ok(new StringListResponse(result));
  }

  @GetMapping("/prefix")
  public ResponseEntity<List<DictionaryEntryDto>> searchByPrefix(@RequestParam String prefix) {
    List<DictionaryEntryDto> result = dictionaryService.searchByPrefix(prefix);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/prefix/paged")
  public ResponseEntity<PagedPrefixResponse> searchByPrefixPaged(
      @RequestParam String prefix,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    PagedPrefixResponse result = dictionaryService.searchByPrefixPaged(prefix, page, size);
    return ResponseEntity.ok(result);
  }

  @DeleteMapping("/clear")
  public ResponseEntity<Void> clear() {
    dictionaryService.clear();
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/starts-with/{prefix}")
  public ResponseEntity<BooleanResponse> startsWith(@PathVariable String prefix) {
    boolean startsWith = dictionaryService.startsWith(prefix);
    return ResponseEntity.ok(new BooleanResponse(startsWith));
  }

  @GetMapping("/keys")
  public ResponseEntity<StringListResponse> getAllKeys() {
    List<String> keys = dictionaryService.getAllKeys();
    return ResponseEntity.ok(new StringListResponse(keys));
  }

  @PostMapping("/checkpoints")
  public ResponseEntity<CheckpointResponse> createCheckpoint() {
    long checkpointId = dictionaryService.createCheckpoint();
    return new ResponseEntity<>(new CheckpointResponse(checkpointId), HttpStatus.CREATED);
  }

  @GetMapping("/checkpoints")
  public ResponseEntity<CheckpointListResponse> listCheckpoints() {
    Map<Long, Integer> checkpoints = dictionaryService.listCheckpoints();
    return ResponseEntity.ok(new CheckpointListResponse(checkpoints));
  }

  @PostMapping("/checkpoints/{id}/rollback")
  public ResponseEntity<Void> rollbackToCheckpoint(@PathVariable long id) {
    boolean success = dictionaryService.rollbackToCheckpoint(id);
    if (success) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @DeleteMapping("/checkpoints/{id}")
  public ResponseEntity<Void> deleteCheckpoint(@PathVariable long id) {
    boolean deleted = dictionaryService.deleteCheckpoint(id);
    if (deleted) {
      return ResponseEntity.noContent().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}
