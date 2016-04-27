package prototype.object;

import java.util.ArrayList;
import java.util.List;
import prototype.chart.DataPoint;

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
  
  public List<Double> GetValues(ParameterMap parameterMap) {
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
  
  public String GetParameterName() {
    return this.parameterName;
  }
  
  public int GetParameterIndex() {
    return this.parameterIndex;
  }
  
  public String GetValueName() {
    return this.valueName;
  }
  
  public Equation GetEquationType() {
    return this.equation;
  }
  
  public boolean IsSatisfied(ParameterMap parameterMap, DataPoint point) {
    List<Double> conditionValues = GetValues(parameterMap);

    switch(GetEquationType()) {
      case IS:
        if(conditionValues.contains(point.getValue())) {
          return true;
        }
        break;
      case IS_NOT:
        if(!conditionValues.contains(point.getValue())) {
          return true;
        }
        break;
    }
    return false;
  }
}