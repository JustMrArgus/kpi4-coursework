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
