package prototype.object;

import java.util.ArrayList;
import java.util.List;
import prototype.chart.DataPoint;

public class Condition {
  private String name;
  private String parameterName;
  private int parameterIndex;
  private String valueName;
  private Equation equation;
  private List<String> values;
  private Range range;
  
  public Condition(String name, String parameterName, int parameterIndex, String valueName, Equation equation) {
    this.name = name;
    this.parameterName = parameterName;
    this.parameterIndex = parameterIndex;
    this.valueName = valueName;
    this.equation = equation;
  }
  
  public Condition(String name, String parameterName, int parameterIndex, String valueName, Equation equation, List<String> values) {
    this(name, parameterName, parameterIndex, valueName, equation);
    this.values = values;
  }
  
  public Condition(String name, String parameterName, int parameterIndex, String valueName, Equation equation, Range range) {
    this(name, parameterName, parameterIndex, valueName, equation);
    this.range = range;
  }
  
  public Condition() {
    this("", "", 0, "", Equation.IS);    
  }
  
  public String GetName() {
    return this.name;
  }
  
  public void SetName(String name) {
    this.name = name;
  }
  
  public String GetParameterName() {
    return this.parameterName;
  }
  
  public void SetParameterName(String parameterName) {
    this.parameterName = parameterName;
  }
  
  public int GetParameterIndex() {
    return this.parameterIndex;
  }
  
  public void SetParameterIndex(int parameterIndex) {
    this.parameterIndex = parameterIndex;
  }
  
  public String GetValueName() {
    return this.valueName;
  }
  
  public void SetValueName(String valueName) {
    this.valueName = valueName;
  }
  
  public Equation GetEquationType() {
    return this.equation;
  }
  
  public void SetEquation(Equation equation) {
    this.equation = equation;
  }
  
  public Range GetRange() {
    return this.range;
  }
  
  public void SetRange(Range range) {
    this.range = range;
  }
  
  public List<String> GetValues() {
    return this.values;
  }
  
  public void SetValue(List<String> values) {
    this.values = values;
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
  
  @Override
  public String toString() {
    if(this.range != null)
      return String.format("%s - (%s %s %s)", this.name, this.parameterName, this.equation, this.range);
    else
      return String.format("%s - (%s %s %s)", this.name, this.parameterName,this.equation, valuesToString());
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
  
  private String valuesToString() {
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < values.size(); i++){
      String value = values.get(i);
      sb.append(value);
      
      if(i >= 2) {
        sb.append(" ...");
        break;
      }
      if(i < values.size() - 2)
        sb.append(", ");
      if(i == values.size() - 2)
        sb.append(" or ");
    }
    return sb.toString();    
  }
}