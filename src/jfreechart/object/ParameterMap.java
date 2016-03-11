package jfreechart.object;

import java.util.ArrayList;
import java.util.List;
import jfreechart.settings.DataMapping;

public class ParameterMap {
  private List<Parameter> parameters;
  
  public ParameterMap() {
    this.parameters = DataMapping.loadParameters();
  }
  
  public boolean ContainsParameter(String parameterName) {
    for(Parameter parameter : this.parameters) {
      if(parameter.getName().equals(parameterName))
        return true;
    }
    return false;
  }
  
  public boolean ParameterContainsValues(String parameterName, String valueName) {
    for(Parameter parameter : this.parameters) {
      if(parameter.getName().equals(parameterName)) {
        for(Value value : parameter.getValuesCopy()) {
          if(value.getName().equals(valueName))
            return true;
        }
      }
    }
    return false;
  }
  
  public Parameter GetParameter(String parameterName) {
    for(Parameter parameter : this.parameters) {
      if(parameter.getName().equals(parameterName))
        return parameter;
    }
    return null;
  }
  
  public List<Parameter> GetParameters() {
    return this.parameters;
  }
  
  public List<Parameter> GetParametersOfType(Type type) {
    List<Parameter> typeParameters = new ArrayList();
    for(Parameter parameter : this.parameters) {
      if(parameter.getType() == type) {
        typeParameters.add(parameter);
      }
    }
    return typeParameters;
  }
  
  public List<Value> GetParametersValues(String parameterName) {
    for(Parameter parameter : this.parameters) {
      if(parameter.getName().equals(parameterName)) {
        return parameter.getValuesCopy();
      }
    }
    return null;
  }
    
  public int GetValueIndex(String parameterName, int parameterIndex, String valueName) throws Exception {
    if(!ContainsParameter(parameterName))
      throw new Exception("Parameter does not exist");
    if(!GetParameter(parameterName).containsValue(valueName))
      throw new Exception("Value does not exist");
    if(GetParameter(parameterName).getCount() <= parameterIndex)
      throw new Exception("Parameter index does not exist");
    
    int index = 0;
    
    for(Parameter parameter : this.parameters) {
      if(parameter.getName().equals(parameterName)) {
        int size = parameter.getValueSize();
        index +=  (parameterIndex * size);
        
        for(Value value : parameter.getValues()) {
          if(value.getName().equals(valueName)) {
            index += value.getIndex();
            break;
          }
        }        
        break;
      } else {
        index += parameter.getSize();
      }
    }
    
    return index;
  }
  
  public int GetMappingSize() {
    int size = 0;    
    for(Parameter parameter : this.parameters) {
      size += parameter.getSize();
    }
    return size;    
  }
}