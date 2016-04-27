package prototype.object;

import java.util.ArrayList;
import java.util.List;
import prototype.settings.DataGeneration;
import prototype.settings.DataMapping;

public class ParameterMap {
  private final List<Parameter> parameters;
  private final List<GeneratedParameter> generated;
  
  public ParameterMap() {
    this.parameters = DataMapping.loadParameters();
    this.generated = DataGeneration.loadGenerated();
  }
  
  public boolean ContainsParameter(String parameterName) {
    for(Parameter parameter : this.parameters) {
      if(parameter.getName().equals(parameterName))
        return true;
    }
    for(GeneratedParameter parameter : this.generated) {
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
    for(GeneratedParameter parameter : this.generated) {
      if(parameter.getName().equals(parameterName))
        return parameter;
    }
    return null;
  }
  
  public List<GeneratedParameter> GetGeneratedParameters() {
    return this.generated;
  }
  
  public List<Parameter> GetAllParameters() {
    List<Parameter> combined = new ArrayList<Parameter>(this.parameters);
    for(GeneratedParameter parameter : this.generated) {
     combined.add(parameter);
    }
    return combined;
  }
  
  public List<Parameter> GetParametersOfType(Type type) {
    List<Parameter> typeParameters = new ArrayList();
    for(Parameter parameter : this.parameters) {
      if(parameter.getType() == type) {
        typeParameters.add(parameter);
      }
    }
    
    for(GeneratedParameter parameter : this.generated) {
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
    for(GeneratedParameter parameter : this.generated) {
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
    boolean breaked = false;
    
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
        
        breaked= true;
        break;
      } else {
        index += parameter.getSize();
      }
    }
    
    if(!breaked) {
      for(GeneratedParameter parameter : this.generated) {
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