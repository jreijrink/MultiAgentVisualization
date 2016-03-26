package prototype.object;

import java.util.ArrayList;
import java.util.List;

public class Parameter {
  private String name;
  private Type type;
  private int count;
  private List<Value> values;
    
  public Parameter() {
    this("", Type.Numerical, 0, new ArrayList());
  }
  
  public Parameter(String name, Type type, int count, List<Value> values) {
    this.name = name;
    this.type = type;
    this.count = count;
    this.values = values;
  }

  public String getName() {
      return this.name;
  }
  
  public void setName(String name) {
    this.name = name;
  }

  public Type getType() {
      return this.type;
  }
  
  public void setType(Type type) {
    this.type = type;
  }
  
  public int getSize() {
    return getValueSize() * this.count;
  }
    
  public int getCount() {
      return this.count;
  }
  
  public void setCount(int count) {
    this.count = count;
  }
  
  public List<Value> getValuesCopy() {
    ArrayList<Value> valuesCopy = new ArrayList();
    for(Value value : this.values) {
      valuesCopy.add(value.copy());
    }
    return valuesCopy;
  }
  
  public boolean containsValue(String valueName) {
    return getValue(valueName) != null;
  }
  
  public List<Value> getValues() {
    return this.values;
  }
  
  public int getValueSize() {
    int size = -1;
    for(Value value : this.values) {
      if(value.getIndex() > size)
        size = value.getIndex();
    }
    return size + 1;
  }
  
  public Value getValue(String name) {
    for(Value value : this.values) {
      if(value.getName().equals(name))
        return value;
    }
    return null;    
  }
      
  public void setValues(List<Value> values) {
    this.values = values;
  }
  
  @Override
  public String toString() {
      return this.name;
  }
}