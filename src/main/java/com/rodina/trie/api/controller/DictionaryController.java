package com.rodina.trie.api.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rodina.trie.api.dto.BulkDeleteRequest;
import com.rodina.trie.api.dto.BulkInsertRequest;
import com.rodina.trie.api.dto.BulkOperationResponse;
import com.rodina.trie.api.dto.DictionaryEntryDto;
import com.rodina.trie.api.dto.InsertRequest;
import com.rodina.trie.api.dto.PagedPrefixResponse;
import com.rodina.trie.api.service.DictionaryService;

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
  public ResponseEntity<Object> search(@PathVariable String key) {
    Object result = dictionaryService.search(key);
    return ResponseEntity.ok(result);
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
  public ResponseEntity<Boolean> exists(@PathVariable String key) {
    return ResponseEntity.ok(dictionaryService.exists(key));
  }

  @GetMapping("/autocomplete")
  public ResponseEntity<List<String>> autocomplete(
      @RequestParam String prefix, @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
    List<String> result = dictionaryService.autocomplete(prefix, limit);
    return ResponseEntity.ok(result);
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
  public ResponseEntity<Boolean> startsWith(@PathVariable String prefix) {
    return ResponseEntity.ok(dictionaryService.startsWith(prefix));
  }

  @GetMapping("/keys")
  public ResponseEntity<List<String>> getAllKeys() {
    return ResponseEntity.ok(dictionaryService.getAllKeys());
  }
}
