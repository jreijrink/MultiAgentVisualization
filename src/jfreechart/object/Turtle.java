package jfreechart.object;

import java.util.Arrays;

public class Turtle {
  public final int turtleID;
  public final double[][] data;
  private ParameterMap parameterMap;
  
  public Turtle(int turtleID, double[][] data) {
    this.turtleID = turtleID;
    this.data = data;
    this.parameterMap = new ParameterMap();
  }
  
  public double[] GetValues(String parameterName, int parameterIndex, String valueName, int startIndex, int endIndex) {
    try {
    int valueIndex = this.parameterMap.GetValueIndex(parameterName, parameterIndex, valueName);
    double[] parameterData = this.data[valueIndex];
    return applyDecimalMask(Arrays.copyOfRange(parameterData, startIndex, endIndex), parameterName, valueName);
    } catch(Exception ex) {
      ex.printStackTrace();
      return new double[0];
    }
  }
    
  public double[] GetAllValues(String parameterName, int parameterIndex, String valueName) {
    try {
    int valueIndex = this.parameterMap.GetValueIndex(parameterName, parameterIndex, valueName);
    return applyDecimalMask(this.data[valueIndex], parameterName, valueName);
    } catch(Exception ex) {
      ex.printStackTrace();
      return new double[0];
    }
  }
  
  private double[] applyDecimalMask(double[] data, String parameterName, String valueName) {
    Parameter parameter = parameterMap.GetParameter(parameterName);
    Value value = parameter.getValue(valueName);
    
    String mask = value.getDecimalmask();
    if(mask!= null && mask.length() > 0) {      
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
   
  public int getID() {
    return turtleID;
  }
  
  @Override
  public String toString() {
    return String.format("Turtle: %d", turtleID);
  }
}
