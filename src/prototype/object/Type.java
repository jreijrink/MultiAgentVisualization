package prototype.object;

import java.util.ArrayList;
import java.util.List;

public enum Type {
  Numerical("Numerical"), Categorical("Categorical"), Positional("Positional");
  
  private String name;
  
  Type(String name) {
    this.name = name;
  }
  
  @Override
  public String toString() {
    return this.name;
  }
  
  public static Type fromString(String name) {
    for(Type type : values()) {
      if(type.name.equals(name))
        return type;
    }
    return Numerical;
  }
  
  public static String[] stringValues() {
    List<String> values = new ArrayList();
    for(Type type : values()) {
      values.add(type.toString());
    }
    return values.toArray(new String[values.size()]);
  }
}