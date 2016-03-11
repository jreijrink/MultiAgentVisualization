/*
package jfreechart.object;

import java.util.ArrayList;
import java.util.List;
import static jfreechart.Parser.MAX_TURTLES;

public class TimeFrame {
  private int frameID;
  private List<Turtle> turtles;
  
  public TimeFrame(int frameID, double[] values) {
    this.frameID = frameID;
    turtles = new ArrayList();
    for(int i = 0; i < MAX_TURTLES; i++) {
      Turtle turtle = new Turtle(i, frameID, values);
      turtles.add(turtle);
    }
  }
    
  public List<Turtle> getTurtles() {
    return turtles;
  }
    
  @Override
  public String toString() {
    return String.format("Timeframe: %d", frameID);
  }
}
*/