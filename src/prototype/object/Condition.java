package prototype.object;

import java.util.ArrayList;
import java.util.List;

public class Condition {
  private final String parameterName;
  private final int parameterIndex;
  private final String valueName;
  private final Equation equation;
  private final List<String> values;
  
  public Condition(String parameterName, int parameterIndex, String valueName, Equation equation, List<String> values) {
    this.parameterName = parameterName;
    this.parameterIndex = parameterIndex;
    this.valueName = valueName;
    this.equation = equation;
    this.values = values;
  }
  
  public List<Double> getValues(ParameterMap parameterMap) {
    List<Double> results = new ArrayList();
    
    try {
      Value value = parameterMap.GetParameter(parameterName).getValue(valueName);
      
      for(String categoryValue : values) {
        results.add((double)value.getCategoryValue(categoryValue));
      }
    } catch(Exception ex) {
      ex.printStackTrace();
    }
    
    return results;
  }
  
  public String getParameterName() {
    return this.parameterName;
  }
  
  public int getParameterIndex() {
    return this.parameterIndex;
  }
  
  public String GetValueName() {
    return this.valueName;
  }
  
  public Equation GetEquationType() {
    return this.equation;
  }
}