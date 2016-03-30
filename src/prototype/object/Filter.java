package prototype.object;

import java.util.List;

public class Filter {

  private final String parameterName;
  private final int parameterIndex;
  private final String valueName;
  private double[] filterValues;

  public Filter(String parameterName, int parameterIndex, String valueName, double[] filterValues) {
      this.parameterName = parameterName;
      this.parameterIndex = parameterIndex;
      this.valueName = valueName;
      this.filterValues = filterValues;
  }
  
  public String ParameterName() {
    return this.parameterName;
  }
  
  public int ParameterIndex() {
    return this.parameterIndex;
  }
  
  public String ValueName() {
    return this.valueName;
  }
  
  public double[] FilterValues() {
    return this.filterValues;
  }
  
  public boolean ContainsFilterValue(double value) {
    for(int i = 0; i < filterValues.length; i++) {
      if(filterValues[i] == value)
      return true;
    }
    return false;
  }
}