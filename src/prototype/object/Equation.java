package prototype.object;

import java.util.ArrayList;
import java.util.List;

public enum Equation {
  IS("is"), IS_NOT("is not");
  
  private String name;
  
  Equation(String name) {
    this.name = name;
  }
  
  @Override
  public String toString() {
    return this.name;
  }
  
  public static Equation fromString(String name) {
    for(Equation equation : values()) {
      if(equation.name.equals(name))
        return equation;
    }
    return IS;
  }
  
  public static String[] stringValues() {
    List<String> values = new ArrayList();
    for(Equation equation : values()) {
      values.add(equation.toString());
    }
    return values.toArray(new String[values.size()]);
  }
}