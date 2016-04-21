package prototype.object;

import java.util.List;
import prototype.chart.Chart;

public class Filter {

  private final Chart chart;
  private final String parameterName;
  private final int parameterIndex;
  private final String valueName;
  private Range range;
  private List<Double> values;

  public Filter(Chart chart, String parameterName, int parameterIndex, String valueName, Range range) {
    this.chart = chart;
    this.parameterName = parameterName;
    this.parameterIndex = parameterIndex;
    this.valueName = valueName;
    this.range = range;
  }
  
  public Filter(Chart chart, String parameterName, int parameterIndex, String valueName, List<Double> values) {
    this.chart = chart;
    this.parameterName = parameterName;
    this.parameterIndex = parameterIndex;
    this.valueName = valueName;
    this.values = values;
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
    
  public boolean SatisfiesFilter(double value) {
    if(range != null) {
      return range.contains(value);
    }
    if(values != null) {
      return values.contains(value);
    }
    return false;
  }
  
  public Chart GetChart() {
    return this.chart;
  }
  
  public boolean Equals(Chart chart) {
    return this.chart == chart;
  }
}