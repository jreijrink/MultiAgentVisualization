package prototype.object;

import java.util.List;
import prototype.chart.Chart;

public class Filter {

  private final Chart chart;
  private final String parameterName;
  private final int parameterIndex;
  private final String valueName;
  private double minValue;
  private double maxValue;

  public Filter(Chart chart, String parameterName, int parameterIndex, String valueName, double minValue, double maxValue) {
    this.chart = chart;
    this.parameterName = parameterName;
    this.parameterIndex = parameterIndex;
    this.valueName = valueName;
    this.minValue = minValue;
    this.maxValue = maxValue;
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
    return value >= minValue && value <= maxValue;
  }
  
  public Chart GetChart() {
    return this.chart;
  }
  
  public boolean Equals(Chart chart) {
    return this.chart == chart;
  }
}