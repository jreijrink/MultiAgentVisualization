package prototype.chart;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;

public class DataPoint {
  private final int timeframe;
  private final double value;
  private final List<Integer> indices;
  private final boolean aboveMin;
  private final boolean belowMax;
  private final boolean satisfiesFilter;
  
  public DataPoint(int timeframe, double value, int initialIndex, boolean aboveMin, boolean belowMax, boolean satisfiesFilter) {
    this.timeframe = timeframe;
    this.value = value;
    this.indices = new ArrayList();
    this.indices.add(initialIndex);
    this.aboveMin = aboveMin;
    this.belowMax = belowMax;
    this.satisfiesFilter = satisfiesFilter;
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
  
  public boolean aboveMin() {
    return this.aboveMin;
  }
  
  public boolean belowMax() {
    return this.belowMax;
  }
  
  public boolean satisfiesFilter() {
    return this.satisfiesFilter;
  }
  
  public boolean outOfRange() {
    return (!this.aboveMin || !this.belowMax);
  }
  
  public boolean isVisible() {
    return (this.aboveMin && this.belowMax && this.satisfiesFilter);
  }
}
