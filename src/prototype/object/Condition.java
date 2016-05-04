package prototype.object;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import prototype.chart.DataPoint;

public class Condition {
  private int id;
  private String name;
  private String parameterName;
  private int parameterIndex;
  private String valueName;
  private Equation equation;
  private List<String> values;
  private Range range;
  
  public Condition(int id, String name, String parameterName, int parameterIndex, String valueName, Equation equation) {
    this.id = id;
    this.name = name;
    this.parameterName = parameterName;
    this.parameterIndex = parameterIndex;
    this.valueName = valueName;
    this.equation = equation;
  }
  
  public Condition(int id, String name, String parameterName, int parameterIndex, String valueName, Equation equation, List<String> values) {
    this(id, name, parameterName, parameterIndex, valueName, equation);
    this.values = values;
  }
  
  public Condition(int id, String name, String parameterName, int parameterIndex, String valueName, Equation equation, Range range) {
    this(id, name, parameterName, parameterIndex, valueName, equation);
    this.range = range;
  }
  
  public Condition() {
    this((int)new Date().getTime(), "", "", 0, "", Equation.IS);    
  }
  
  public int getID() {
    return this.id;
  }
  
  public String getName() {
    return this.name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getParameterName() {
    return this.parameterName;
  }
  
  public void setParameterName(String parameterName) {
    this.parameterName = parameterName;
  }
  
  public int getParameterIndex() {
    return this.parameterIndex;
  }
  
  public void setParameterIndex(int parameterIndex) {
    this.parameterIndex = parameterIndex;
  }
  
  public String getValueName() {
    return this.valueName;
  }
  
  public void setValueName(String valueName) {
    this.valueName = valueName;
  }
  
  public Equation getEquationType() {
    return this.equation;
  }
  
  public void setEquation(Equation equation) {
    this.equation = equation;
  }
  
  public Range getRange() {
    return this.range;
  }
  
  public void setRange(Range range) {
    this.range = range;
  }
  
  public List<String> getValues() {
    return this.values;
  }
  
  public void setValue(List<String> values) {
    this.values = values;
  }
  
  public boolean isSatisfied(ParameterMap parameterMap, DataPoint point) {
    List<Double> conditionValues = Condition.this.getValues(parameterMap);

    switch(getEquationType()) {
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
        Value value = parameterMap.getParameter(parameterName).getValue(valueName);

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