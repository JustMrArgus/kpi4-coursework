package com.rodina.trie.api.dto;

import java.util.List;
import java.util.Objects;

public class PagedPrefixResponse {
  private List<DictionaryEntryDto> content;
  private int page;
  private int size;
  private long totalElements;
  private int totalPages;
  private boolean hasNext;
  private boolean hasPrevious;

  public PagedPrefixResponse() {}

  public PagedPrefixResponse(
      List<DictionaryEntryDto> content, int page, int size, long totalElements) {
    this.content = content;
    this.page = page;
    this.size = size;
    this.totalElements = totalElements;
    this.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
    this.hasNext = page < totalPages - 1;
    this.hasPrevious = page > 0;
  }

  public List<DictionaryEntryDto> getContent() {
    return content;
  }

  public void setContent(List<DictionaryEntryDto> content) {
    this.content = content;
  }

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public long getTotalElements() {
    return totalElements;
  }

  public void setTotalElements(long totalElements) {
    this.totalElements = totalElements;
  }

  public int getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(int totalPages) {
    this.totalPages = totalPages;
  }

  public boolean isHasNext() {
    return hasNext;
  }

  public void setHasNext(boolean hasNext) {
    this.hasNext = hasNext;
  }

  public boolean isHasPrevious() {
    return hasPrevious;
  }

  public void setHasPrevious(boolean hasPrevious) {
    this.hasPrevious = hasPrevious;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PagedPrefixResponse that = (PagedPrefixResponse) o;
    return page == that.page
        && size == that.size
        && totalElements == that.totalElements
        && totalPages == that.totalPages
        && hasNext == that.hasNext
        && hasPrevious == that.hasPrevious
        && Objects.equals(content, that.content);
  }

  @Override
  public int hashCode() {
    return Objects.hash(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
  }

  @Override
  public String toString() {
    return "PagedPrefixResponse{"
        + "content="
        + content
        + ", page="
        + page
        + ", size="
        + size
        + ", totalElements="
        + totalElements
        + ", totalPages="
        + totalPages
        + ", hasNext="
        + hasNext
        + ", hasPrevious="
        + hasPrevious
        + '}';
  }
}
