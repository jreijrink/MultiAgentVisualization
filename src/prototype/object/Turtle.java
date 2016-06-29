package prototype.object;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import prototype.chart.BaseChart;
import prototype.chart.DockElement;
import prototype.chart.DataPoint;

public class Turtle {
  public final int turtleID;
  public double[][] data;
  private final ParameterMap parameterMap;
  private boolean[] filterMap;
  private final Map<BaseChart, List<Filter>> filters;
  
  public Turtle(int turtleID, double[][] data) {
    this.turtleID = turtleID;
    this.data = data;
    this.parameterMap = new ParameterMap();
    this.filters = new HashMap();
    
    createGeneratedParameters();
  }
  
  public void setFilter(Filter newFilter) {
    BaseChart chart = newFilter.getChart();
    if(!filters.containsKey(chart))
      filters.put(chart, new ArrayList());
    filters.get(chart).add(newFilter);
    applyFilters();
  }
  
  public boolean removeFilters(BaseChart chart) {
    if(filters.containsKey(chart) && filters.get(chart).size() > 0) {
      filters.remove(chart);
      applyFilters();
      return true;
    }
    return false;
  }
    
  public boolean removeFilter(Filter filter) {
    BaseChart chart = filter.getChart();
    boolean success = false;
    
    if(filters.containsKey(chart) && filters.get(chart).size() > 0) {
      success = filters.get(chart).remove(filter);
      if(success) 
        applyFilters();
    }
    
    return success;
  }
  
  public DataPoint getValue(String parameterName, int parameterIndex, String valueName, int index) {
    try {
      int startIndex = index;
      int endIndex = index + 1;
      
      if(endIndex > this.data[0].length - 1) {
        startIndex = this.data[0].length - 2;
        endIndex = this.data[0].length - 1;
      }
      
      List<DataPoint> values = getValues(parameterName, parameterIndex, valueName, startIndex, endIndex);
      if(values.size() > 0) {
        return values.get(0);
      }
    } catch(Exception ex) {
      ex.printStackTrace();
    }
      return null;
  }
  
  public List<DataPoint> getAllValues(String parameterName, int parameterIndex, String valueName) {
    try {
      return getValues(parameterName, parameterIndex, valueName, 0, this.data[0].length - 1);
    } catch(Exception ex) {
      ex.printStackTrace();
      return new ArrayList();
    }
  }
  
  public List<DataPoint> getValues(String parameterName, int parameterIndex, String valueName, int startIndex, int endIndex) {    
    try {
      Value value = this.parameterMap.getParameter(parameterName).getValue(valueName);
      int valueIndex = this.parameterMap.getValueIndex(parameterName, parameterIndex, valueName);

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

  private void createGeneratedParameters() {
    List<GeneratedParameter> parameters = this.parameterMap.getGeneratedParameters();
    
    for(GeneratedParameter parameter : parameters) {

      List<Map<Condition, List<DataPoint>>> preConditions = getConditionMap(parameter.getPreConditions());
      List<Map<Condition, List<DataPoint>>> postConditionsSuccess = getConditionMap(parameter.getPostConditionsSuccess());
      List<Map<Condition, List<DataPoint>>> postConditionsFailed = getConditionMap(parameter.getPostConditionsFailed());
      
      for(Value value : parameter.getValues()) {
        try {
          int valueIndex = this.parameterMap.getValueIndex(parameter.getName(), 0, value.getName());
          double[] newDataRow = new double[this.data[0].length];
          
          boolean active = false;
          int activationIndex = 0;
          int endIndex = 0;
          int result = 0;
          boolean finished = false;
          
          for(int i = 0; i < newDataRow.length - 1; i++) {
            
            if(!active) {
              boolean anyCombinedSatisfied = false;
              
              for(Map<Condition, List<DataPoint>> andMap : preConditions) {
                boolean combinedSatisfied = (andMap.size() > 0);
                
                //AND type
                for(Condition condition : andMap.keySet()) {
                  combinedSatisfied = condition.isSatisfied(parameterMap, andMap.get(condition).get(i)) && combinedSatisfied;
                }
                
                anyCombinedSatisfied = combinedSatisfied || anyCombinedSatisfied;
              }          
              
              if(anyCombinedSatisfied) {
                active = true;
                finished = false;
                activationIndex = i;
                newDataRow[i] = 1;
              }         
              
            } else {
              boolean preConditionsFinished = false;
              
              //Wait until the preconditions is unsatisfied.
              for(Map<Condition, List<DataPoint>> andMap : preConditions) {
                boolean combinedSatisfied = true;
                
                //AND type
                for(Condition condition : andMap.keySet()) {
                  combinedSatisfied = condition.isSatisfied(parameterMap, andMap.get(condition).get(i)) && combinedSatisfied;
                }
                  
                preConditionsFinished = !combinedSatisfied || preConditionsFinished;
              }
              
              if(preConditionsFinished || i == newDataRow.length - 2) {
                boolean success = (postConditionsSuccess.size() == 0); // If no conditions -> success
                boolean failed = false;

                for(Map<Condition, List<DataPoint>> andMap : postConditionsSuccess) {
                  boolean combinedSatisfied = (andMap.size() > 0);

                  //AND type
                  for(Condition condition : andMap.keySet()) {
                    combinedSatisfied = condition.isSatisfied(parameterMap, andMap.get(condition).get(i)) && combinedSatisfied;
                  }

                  success = combinedSatisfied || success;
                }
                
                for(Map<Condition, List<DataPoint>> andMap : postConditionsFailed) {
                  boolean combinedSatisfied = (andMap.size() > 0);

                  //AND type
                  for(Condition condition : andMap.keySet()) {
                    combinedSatisfied = condition.isSatisfied(parameterMap, andMap.get(condition).get(i)) && combinedSatisfied;
                  }

                  failed = combinedSatisfied || failed;
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
  
  private List<Map<Condition, List<DataPoint>>> getConditionMap(List<CombinedANDConditions> conditions) {
    List<Map<Condition, List<DataPoint>>> conditionsMap = new ArrayList();
    
    for(CombinedANDConditions Andcondition : conditions) {
      Map<Condition, List<DataPoint>> andMap = new HashMap();
      for(Condition condition : Andcondition.getConditions()) {
        List<DataPoint> datapoints = getAllValues(condition.getParameterName(), condition.getParameterIndex(), condition.getValueName());
        andMap.put(condition, datapoints);
      }
      conditionsMap.add(andMap);
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
      
      for(BaseChart chart : filters.keySet()) {
        if(filters.get(chart).size() > 0) {
          boolean[] localFilterMap = new boolean[this.data[0].length];
          for(int i = 0; i < this.data[0].length; i++) {
            localFilterMap[i] = false;
          }

          for(Filter filter : filters.get(chart)) {
            int valueIndex = this.parameterMap.getValueIndex(filter.parameterName(), filter.parameterIndex(), filter.valueName());
            double[] dataset = this.data[valueIndex];            
            dataset = applyDecimalMask(dataset, this.parameterMap.getParameter(filter.parameterName()).getValue(filter.valueName()));
          
            for(int i = 0; i < this.data[0].length; i++) {
              if(filter.satisfiesFilter(turtleID, dataset[i])) {
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
}
