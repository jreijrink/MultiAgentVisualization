package jfreechart;

import javafx.util.Pair;

public class StringValuePair<K, V> extends Pair<K, V> {
  private String name;

  public StringValuePair(K key, V value) {
    super(key, value);
    this.name = key.toString();
  }

  @Override
  public String toString() {
    return name;
  }                
};