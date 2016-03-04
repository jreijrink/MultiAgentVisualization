package jfreechart.object;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParameterMap {
  private final int timeFrame;
  private final List<SimpleEntry<String, SimpleEntry<Integer, Double>>> parameters;
  
  public ParameterMap(int timeFrame) {
    this.timeFrame = timeFrame;
    parameters = new ArrayList();
  }
  
  public void addParameter(String name, int size, int ammount) {
    for(int i = 0; i < size * ammount; i++) {
      parameters.add(new SimpleEntry(name, new SimpleEntry(size, 0.0)));
    }
  }
  
  public void setValue(int index, double value) {
    parameters.get(index).getValue().setValue(value);
  }
  
  public int getSize() {
    return parameters.size();
  }
  
  public List<String> getKeys() {
    List<String> keys = new ArrayList();
    keys.add("time");
    for(SimpleEntry<String, SimpleEntry<Integer, Double>> keyvalue : parameters) {
      if(!keys.contains(keyvalue.getKey())) {
        keys.add(keyvalue.getKey());
      }
    }
    return keys;
  }
  
  public double[][] getValue(String key) {
    if("time".equals(key))
      return new double[][] { new double[] { (double)this.timeFrame } };
    
    List<Double> tempvalues = new ArrayList();
    
    int size = 1;
    for(SimpleEntry<String, SimpleEntry<Integer, Double>> keyvalue : parameters) { 
      if(key.equals(keyvalue.getKey())) {
        size = keyvalue.getValue().getKey();
        tempvalues.add(keyvalue.getValue().getValue());
      }
    }
    
    double[][] values = new double[tempvalues.size() / size][size];
    int rowIndex = 0;
    int columnIndex = 0;
    for(Double value : tempvalues) {
      values[rowIndex][columnIndex] = value;
      columnIndex++;
      if(columnIndex >= size) {
        columnIndex = 0;
        rowIndex++;
      }
    }
    
    return values;
  }
  
  public String getStringValue(String key) {
    double[][] values = getValue(key);
    String[] stringValue = new String[values.length];
    for(int i = 0; i < values.length; i++) {
      stringValue[i] = Arrays.toString(values[i]);
    }
    return Arrays.toString(stringValue);
  }
}