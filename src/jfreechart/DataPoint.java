package jfreechart;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;

public class DataPoint {
  private Point2D location;
  private List<Integer> indices;
  
  public DataPoint(Point2D location, int initialIndex) {
    this.location = location;
    this.indices = new ArrayList();
    this.indices.add(initialIndex);
  }
  
  public void addIndices(List<Integer> indices) {
    this.indices.addAll(indices);
  }
  
  public Point2D getLocation() {
    return this.location;    
  }
  
  public List<Integer> getIndices() {
    return this.indices;
  }
}
