package prototype.object;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.util.Pair;
import prototype.chart.Chart;
import prototype.chart.DataPoint;

public class Turtle {
  public final int turtleID;
  public double[][] data;
  private final ParameterMap parameterMap;
  private boolean[] filterMap;
  private final Map<Chart, List<Filter>> filters;
  
  public Turtle(int turtleID, double[][] data) {
    this.turtleID = turtleID;
    this.data = data;
    this.parameterMap = new ParameterMap();
    this.filters = new HashMap();
    
    createGeneratedParameters();
  }
  
  private void createGeneratedParameters() {
    List<GeneratedParameter> parameters = this.parameterMap.GetGeneratedParameters();
    
    for(GeneratedParameter parameter : parameters) {

      Map<Condition, List<DataPoint>> preConditions = getConditionMap(parameter.getPreConditions());
      Map<Condition, List<DataPoint>> postConditionsSuccess = getConditionMap(parameter.getPostConditionsSuccess());
      Map<Condition, List<DataPoint>> postConditionsFailed = getConditionMap(parameter.getPostConditionsFailed());
      
      for(Value value : parameter.getValues()) {
        try {
          int valueIndex = this.parameterMap.GetValueIndex(parameter.getName(), 0, value.getName());
          double[] newDataRow = new double[this.data[0].length];
          
          boolean active = false;
          int activationIndex = 0;
          int endIndex = 0;
          int result = 0;
          boolean finished = false;
          
          for(int i = 0; i < newDataRow.length - 1; i++) {
            
            if(!active) {
              boolean satisfied = (preConditions.size() > 0);
              
              //AND type
              for(Condition condition : preConditions.keySet()) {
                satisfied = condition.IsSatisfied(parameterMap, preConditions.get(condition).get(i)) && satisfied;
              }
              
              if(satisfied) {
                active = true;
                finished = false;
                activationIndex = i;
                newDataRow[i] = 1;
              }              
              
            } else {
              boolean preConditionsFinished = false;
              
              //Wait until one of the preconditions is unsatisfied.
              for(Condition condition : preConditions.keySet()) {
                preConditionsFinished = !condition.IsSatisfied(parameterMap, preConditions.get(condition).get(i)) || preConditionsFinished;
              }
              
              if(preConditionsFinished) {
                boolean success = true;
                boolean failed = false;

                //AND type
                for(Condition condition : postConditionsSuccess.keySet()) {
                  success = condition.IsSatisfied(parameterMap, postConditionsSuccess.get(condition).get(i)) && success;
                }

                //OR type
                for(Condition condition : postConditionsFailed.keySet()) {
                  failed = condition.IsSatisfied(parameterMap, postConditionsFailed.get(condition).get(i)) || failed;
                }

                //Don't end if the post conditions success and failed are both satisfied
                if((success || failed) && !(success && failed)) {
                  endIndex = i;
                  if(success) {
                    result = 1; //Succes
                  } else {
                    result = 2; //Failed
                  }
                  finished = true;
                }
              }
            }
            
            if(finished) {
              for(int index = activationIndex; index <= endIndex; index++) {
                newDataRow[index] = result;
              }              
              active = false;
            }
          }

          this.data = addElement(this.data, valueIndex, newDataRow);
        } catch(Exception ex) {
          ex.printStackTrace();
        }
      }
    }
  }
  
  private Map<Condition, List<DataPoint>> getConditionMap(List<Condition> conditions) {
    Map<Condition, List<DataPoint>> conditionsMap = new HashMap();
    
    for(Condition condition : conditions) {
      List<DataPoint> datapoints = GetAllValues(condition.GetParameterName(), condition.GetParameterIndex(), condition.GetValueName());
      conditionsMap.put(condition, datapoints);
    }
    
    return conditionsMap;
  }
  
  private double[][] addElement(double[][] a, int index, double[] e) {
    a  = Arrays.copyOf(a, Math.max(a.length, a.length + (index - a.length) + 1));
    a[index] = e;
    return a;
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
              if(filter.SatisfiesFilter(turtleID, dataset[i])) {
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
      int startIndex = index;
      int endIndex = index + 1;
      
      if(endIndex > this.data[0].length - 1) {
        startIndex = this.data[0].length - 2;
        endIndex = this.data[0].length - 1;
      }
      
      List<DataPoint> values = GetValues(parameterName, parameterIndex, valueName, startIndex, endIndex);
      if(values.size() > 0) {
        return values.get(0);
      }
    } catch(Exception ex) {
      ex.printStackTrace();
    }
      return null;
  }
  
  public List<DataPoint> GetAllValues(String parameterName, int parameterIndex, String valueName) {
    try {
      return GetValues(parameterName, parameterIndex, valueName, 0, this.data[0].length - 1);
    } catch(Exception ex) {
      ex.printStackTrace();
      return new ArrayList();
    }
  }
  
  public List<DataPoint> GetValues(String parameterName, int parameterIndex, String valueName, int startIndex, int endIndex) {    
    try {
      Value value = this.parameterMap.GetParameter(parameterName).getValue(valueName);
      int valueIndex = this.parameterMap.GetValueIndex(parameterName, parameterIndex, valueName);

      startIndex = Math.max(startIndex, 0);
      startIndex = Math.min(startIndex, this.data[valueIndex].length - 1);

      endIndex = Math.max(endIndex, 0);
      endIndex = Math.min(endIndex, this.data[valueIndex].length - 1);

      double[] dataset = Arrays.copyOfRange(this.data[valueIndex], startIndex, endIndex);
      return processData(startIndex, dataset, value);
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
      DataPoint point = null;
      
      if(value != null)
        point = new DataPoint(i + offest, dataValue, i + offest, value.aboveMin(dataValue), value.belowMax(dataValue), satisfiesFilter(i + offest));
      else
        point = new DataPoint(i + offest, dataValue, i + offest, true, true, satisfiesFilter(i + offest));
      
      values.add(point);
    }
    
    return values;
  }
  
  private boolean satisfiesFilter(int index) {
    return this.filterMap == null || this.filterMap.length <= index || this.filterMap[index];
  }
  
  private double[] applyDecimalMask(double[] data, Value value) {
    if(value != null) {
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
    }
    return data;
  }
}
