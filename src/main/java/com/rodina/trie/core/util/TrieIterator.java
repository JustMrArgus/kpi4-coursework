package com.rodina.trie.core.util;

import com.rodina.trie.core.node.TrieNode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class TrieIterator<V> implements Iterator<Map.Entry<String, V>> {
  private final Deque<NodeState<V>> stack;
  private Map.Entry<String, V> nextEntry;

  public TrieIterator(TrieNode<V> root) {
    this(root, "");
  }

  public TrieIterator(TrieNode<V> root, String initialPrefix) {
    this.stack = new ArrayDeque<>();
    this.nextEntry = null;
    if (root != null) {
      this.stack.push(new NodeState<>(root, initialPrefix));
      findNext();
    }
  }

  private void findNext() {
    nextEntry = null;
    while (!stack.isEmpty()) {
      NodeState<V> currentState = stack.pop();
      TrieNode<V> currentNode = currentState.node;
      String currentPrefix = currentState.prefix;

      Map<Character, TrieNode<V>> childrenMap = currentNode.getChildrenMap();
      if (childrenMap != null && !childrenMap.isEmpty()) {
        List<Map.Entry<Character, TrieNode<V>>> sortedChildren =
            new ArrayList<>(childrenMap.entrySet());

        sortedChildren.sort(Map.Entry.comparingByKey(Comparator.reverseOrder()));

        for (Map.Entry<Character, TrieNode<V>> entry : sortedChildren) {
          TrieNode<V> childNode = entry.getValue();
          String childPrefix = currentPrefix + entry.getKey();
          stack.push(new NodeState<>(childNode, childPrefix));
        }
      }

      if (currentNode.isEndOfWord() && currentNode.getValue() != null) {
        nextEntry = new TrieEntry<>(currentPrefix, currentNode.getValue());
        return;
      }
    }
  }

  @Override
  public boolean hasNext() {
    return nextEntry != null;
  }

  @Override
  public Map.Entry<String, V> next() {
    if (nextEntry == null) {
      throw new NoSuchElementException("No more elements in the iteration");
    }
    Map.Entry<String, V> result = nextEntry;
    findNext();
    return result;
  }

  private static class NodeState<V> {
    private final TrieNode<V> node;
    private final String prefix;

    NodeState(TrieNode<V> node, String prefix) {
      this.node = node;
      this.prefix = prefix;
    }
  }
}
