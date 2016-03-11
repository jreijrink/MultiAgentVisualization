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
  
  public double[] GetValues(String parameter, int parameterIndex, String value, int startIndex, int endIndex) {
    try {
    int valueIndex = this.parameterMap.GetValueIndex(parameter, parameterIndex, value);
    double[] parameterData = this.data[valueIndex];
    return Arrays.copyOfRange(parameterData, startIndex, endIndex);
    } catch(Exception ex) {
      ex.printStackTrace();
      return new double[0];
    }
  }
    
  public double[] GetAllValues(String parameter, int parameterIndex, String value) {
    try {
    int valueIndex = this.parameterMap.GetValueIndex(parameter, parameterIndex, value);
    return this.data[valueIndex];
    } catch(Exception ex) {
      ex.printStackTrace();
      return new double[0];
    }
  }
   
  public int getID() {
    return turtleID;
  }
  
  @Override
  public String toString() {
    return String.format("Turtle: %d", turtleID);
  }
}
