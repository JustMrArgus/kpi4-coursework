package com.rodina.trie.api.dto;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("DTO Unit Tests")
class DtoUnitTest {

  @Nested
  @DisplayName("BulkOperationResponse Tests")
  class BulkOperationResponseTests {
    @Test
    @DisplayName("Should create success response")
    void successFactory() {
      BulkOperationResponse response = BulkOperationResponse.success(5);
      assertThat(response.getStatus()).isEqualTo(BulkOperationResponse.Status.SUCCESS);
      assertThat(response.getSuccessCount()).isEqualTo(5);
      assertThat(response.getFailedCount()).isZero();
    }

    @Test
    @DisplayName("Should create partial success response")
    void partialSuccessFactory() {
      List<BulkOperationResponse.BulkOperationError> errors =
          Arrays.asList(new BulkOperationResponse.BulkOperationError("key1", "error", 0));
      BulkOperationResponse response = BulkOperationResponse.partialSuccess(3, 1, errors);
      assertThat(response.getStatus()).isEqualTo(BulkOperationResponse.Status.PARTIAL_SUCCESS);
      assertThat(response.getTotalCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("Should create failed response")
    void failedFactory() {
      List<BulkOperationResponse.BulkOperationError> errors =
          Arrays.asList(new BulkOperationResponse.BulkOperationError("key1", "error1", 0));
      BulkOperationResponse response = BulkOperationResponse.failed(1, errors);
      assertThat(response.getStatus()).isEqualTo(BulkOperationResponse.Status.FAILED);
    }

    @Test
    @DisplayName("Should handle null errors list in constructor")
    void nullErrorsHandled() {
      BulkOperationResponse response =
          new BulkOperationResponse(BulkOperationResponse.Status.SUCCESS, 1, 0, 1, null);
      assertThat(response.getErrors()).isNotNull().isEmpty();
    }
  }

  @Nested
  @DisplayName("PagedPrefixResponse Tests")
  class PagedPrefixResponseTests {
    @Test
    @DisplayName("Should calculate pagination correctly")
    void paginationCalculation() {
      List<DictionaryEntryDto> content =
          Arrays.asList(new DictionaryEntryDto("k1", "v1"), new DictionaryEntryDto("k2", "v2"));
      PagedPrefixResponse response = new PagedPrefixResponse(content, 0, 10, 25);
      assertThat(response.getTotalPages()).isEqualTo(3);
      assertThat(response.isHasNext()).isTrue();
      assertThat(response.isHasPrevious()).isFalse();
    }

    @Test
    @DisplayName("Should handle last page")
    void lastPage() {
      PagedPrefixResponse response = new PagedPrefixResponse(Arrays.asList(), 2, 10, 25);
      assertThat(response.isHasNext()).isFalse();
      assertThat(response.isHasPrevious()).isTrue();
    }

    @Test
    @DisplayName("Should handle zero size")
    void zeroSize() {
      PagedPrefixResponse response = new PagedPrefixResponse(Arrays.asList(), 0, 0, 0);
      assertThat(response.getTotalPages()).isZero();
    }
  }

  @Nested
  @DisplayName("Basic DTO Construction Tests")
  class BasicConstructionTests {
    @Test
    @DisplayName("Should create DTOs with constructors")
    void dtoConstruction() {
      BulkDeleteRequest deleteReq = new BulkDeleteRequest(Arrays.asList("k1", "k2"), false, true);
      assertThat(deleteReq.getKeys()).containsExactly("k1", "k2");
      assertThat(deleteReq.isAtomic()).isFalse();
      BulkInsertRequest insertReq =
          new BulkInsertRequest(Arrays.asList(new InsertRequest("k", "v")), false);
      assertThat(insertReq.getEntries()).hasSize(1);
      DictionaryEntryDto entry = new DictionaryEntryDto("key", "value");
      assertThat(entry.getKey()).isEqualTo("key");
    }
  }
}
