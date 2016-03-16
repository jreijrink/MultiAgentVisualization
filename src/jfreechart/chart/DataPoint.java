package jfreechart.chart;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;

public class DataPoint {
  private int timeframe;
  private double value;
  private List<Integer> indices;
  private boolean inRange;
  
  public DataPoint(int timeframe, double value, int initialIndex, boolean inRange) {
    this.timeframe = timeframe;
    this.value = value;
    this.indices = new ArrayList();
    this.indices.add(initialIndex);
    this.inRange = inRange;
  }
  
  public void addIndices(List<Integer> indices) {
    this.indices.addAll(indices);
  }
  
  public int getTimeframe() {
    return this.timeframe;    
  }
  
  public double getValue() {
    return this.value;    
  }
  
  public Point2D getLocation() {
    return new Point2D(this.timeframe, this.value);    
  }
  
  public List<Integer> getIndices() {
    return this.indices;
  }
  
  public boolean inRange() {
    return this.inRange;
  }
}
