package prototype.object;

import java.util.ArrayList;
import java.util.List;
import prototype.chart.DataPoint;

public class Condition {
  private final String parameterName;
  private final int parameterIndex;
  private final String valueName;
  private final Equation equation;
  private List<String> values;
  private Range range;
  
  public Condition(String parameterName, int parameterIndex, String valueName, Equation equation) {
    this.parameterName = parameterName;
    this.parameterIndex = parameterIndex;
    this.valueName = valueName;
    this.equation = equation;
  }
  
  public Condition(String parameterName, int parameterIndex, String valueName, Equation equation, List<String> values) {
    this(parameterName, parameterIndex, valueName, equation);
    this.values = values;
  }
  
  public Condition(String parameterName, int parameterIndex, String valueName, Equation equation, Range range) {
    this(parameterName, parameterIndex, valueName, equation);
    this.range = range;
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
    List<Double> conditionValues = getValues(parameterMap);

    switch(GetEquationType()) {
      case IS:
        if(this.values != null && conditionValues.contains(point.getValue())) {
          return true;
        }
        if(this.range != null && range.contains(point.getValue())) {
          return true;
        }
        break;
      case IS_NOT:
        if(this.values != null && !conditionValues.contains(point.getValue())) {
          return true;
        }
        if(this.range != null && !range.contains(point.getValue())) {
          return true;
        }
        break;
    }
    return false;
  }
  
  private List<Double> getValues(ParameterMap parameterMap) {
    List<Double> results = new ArrayList();
    
    if(this.values != null) {
      try {
        Value value = parameterMap.GetParameter(parameterName).getValue(valueName);

        for(String categoryValue : this.values) {
          results.add((double)value.getCategoryValue(categoryValue));
        }
      } catch(Exception ex) {
        ex.printStackTrace();
      }
    }
    
    return results;
  }
}