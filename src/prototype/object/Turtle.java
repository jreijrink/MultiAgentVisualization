package prototype.object;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import prototype.chart.Chart;
import prototype.chart.DataPoint;

public class Turtle {
  public final int turtleID;
  public final double[][] data;
  private final ParameterMap parameterMap;
  private boolean[] filterMap;
  private final Map<Chart, List<Filter>> filters;
  
  public Turtle(int turtleID, double[][] data) {
    this.turtleID = turtleID;
    this.data = data;
    this.parameterMap = new ParameterMap();
    this.filters = new HashMap();
  }
  
  private void applyFilters() {
    try {
      this.filterMap = new boolean[this.data[0].length];
      for(int i = 0; i < this.data[0].length; i++) {
        this.filterMap[i] = true;
      }
      
      for(Chart chart : filters.keySet()) {
        if(filters.get(chart).size() > 0) {
          boolean[] localFilterMap = new boolean[this.data[0].length];
          for(int i = 0; i < this.data[0].length; i++) {
            localFilterMap[i] = false;
          }

          for(Filter filter : filters.get(chart)) {          
            int valueIndex = this.parameterMap.GetValueIndex(filter.ParameterName(), filter.ParameterIndex(), filter.ValueName());
            double[] dataset = this.data[valueIndex];            
            dataset = applyDecimalMask(dataset, this.parameterMap.GetParameter(filter.ParameterName()).getValue(filter.ValueName()));
          
            for(int i = 0; i < this.data[0].length; i++) {
              if(filter.SatisfiesFilter(dataset[i])) {
                localFilterMap[i] = true;
              }
            }
          }

          for(int i = 0; i < this.data[0].length; i++) {
            this.filterMap[i] = this.filterMap[i] && localFilterMap[i];
          }
        }
      }
    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  
  public void setFilter(Filter newFilter) {
    Chart chart = newFilter.GetChart();
    if(!filters.containsKey(chart))
      filters.put(chart, new ArrayList());
    filters.get(chart).add(newFilter);
    applyFilters();
  }
  
  public boolean removeFilters(Chart chart) {
    if(filters.containsKey(chart) && filters.get(chart).size() > 0) {
      filters.remove(chart);
      applyFilters();
      return true;
    }
    return false;
  }
    
  public boolean removeFilter(Filter filter) {
    Chart chart = filter.GetChart();
    boolean success = false;
    
    if(filters.containsKey(chart) && filters.get(chart).size() > 0) {
      success = filters.get(chart).remove(filter);
      if(success) 
        applyFilters();
    }
    
    return success;
  }
  
  public DataPoint GetValue(String parameterName, int parameterIndex, String valueName, int index) {
    try {
      Value value = this.parameterMap.GetParameter(parameterName).getValue(valueName);
            
      int valueIndex = this.parameterMap.GetValueIndex(parameterName, parameterIndex, valueName);
      double[] dataset = this.data[valueIndex];
      
      index = Math.max(index, 0);
      index = Math.min(index, dataset.length - 1);
      
      return processData(valueIndex, new double[] { dataset[index] }, value).get(0);
    } catch(Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }
  
  public List<DataPoint> GetValues(String parameterName, int parameterIndex, String valueName, int startIndex, int endIndex) {    
    try {
      Value value = this.parameterMap.GetParameter(parameterName).getValue(valueName);
      
      int valueIndex = this.parameterMap.GetValueIndex(parameterName, parameterIndex, valueName);
      
      startIndex = Math.max(startIndex, 0);
      endIndex = Math.min(endIndex, this.data[valueIndex].length - 1);
      
      double[] dataset = Arrays.copyOfRange(this.data[valueIndex], startIndex, endIndex);
      return processData(startIndex, dataset, value);
    } catch(Exception ex) {
      ex.printStackTrace();
      return new ArrayList();
    }
  }
  
  public List<DataPoint> GetAllValues(String parameterName, int parameterIndex, String valueName) {
    try {
      Value value = this.parameterMap.GetParameter(parameterName).getValue(valueName);
      
      int valueIndex = this.parameterMap.GetValueIndex(parameterName, parameterIndex, valueName);
      double[] dataset = this.data[valueIndex];
      return processData(0, dataset, value);
    } catch(Exception ex) {
      ex.printStackTrace();
      return new ArrayList();
    }
  }
  
  public int getTimeFrameCount() {
    return data[0].length;
  }
  
  public int getID() {
    return turtleID;
  }
  
  @Override
  public String toString() {
    return String.format("Turtle: %d", turtleID);
  }
  
  private List<DataPoint> processData(int offest, double[] data, Value value) {
    List<DataPoint> values = new ArrayList();
    
    double[] result = applyDecimalMask(data, value);

    for(int i = 0; i < result.length; i++) {
      double dataValue = result[i];
                  
      DataPoint point = new DataPoint(i + offest, dataValue, i + offest, value.aboveMin(dataValue), value.belowMax(dataValue), satisfiesFilter(i + offest));
      values.add(point);
    }
    
    return values;
  }
  
  private boolean satisfiesFilter(int index) {
    return this.filterMap == null || this.filterMap.length <= index || this.filterMap[index];
  }
  
  private double[] applyDecimalMask(double[] data, Value value) {
    
    String mask = value.getDecimalmask();
    if(mask != null && mask.length() > 0) {      
      double[] editData = Arrays.copyOf(data, data.length);
      
      for(int i = 0; i < editData.length; i++) {
        int calcValue = (int)editData[i];
        
        String stringRep = String.valueOf(calcValue);
        String newValueRep = "";
        
        for(int s = 0; s < stringRep.length(); s++) {
          if(s < mask.length() && mask.charAt(mask.length() - 1 - s) == '1') {
            newValueRep = stringRep.charAt(stringRep.length() - 1 - s) + newValueRep;
          }
        }
        
        double newValue = 0;
        if(newValueRep.length() > 0)
          newValue = Double.parseDouble(newValueRep);
        
        editData[i] = newValue;
      }
      
      return editData;      
    }
    
    return data;
  }
}
