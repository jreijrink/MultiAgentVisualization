package prototype.object;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;
import javafx.scene.shape.Rectangle;
import prototype.settings.Configuration;

public class CombinedOpponent {
  private final List<Point2D> positions;
  private final List<Integer> turtles;
  private final double id;
  private final List<Rectangle> shapes;
  private final Configuration configuration;
  
  public CombinedOpponent(double id, int turtle, Point2D position) {
    this.positions = new ArrayList();
    this.turtles = new ArrayList();
    this.shapes = new ArrayList();
    this.configuration = new Configuration();
    
    this.id = id;
    this.turtles.add(turtle);
    this.positions.add(position);
  }

  public Point2D getAveragePosition() {
    double count = 0;
    double totalX = 0;
    double totalY = 0;
    
    for(Point2D position : positions) {
      totalX += position.getX();
      totalY += position.getY();
      count++;
    }
    
    if(count != 0)
      return new Point2D(totalX / count, totalY / count);
    else
      return null;
  }
  
  public double getId() {
    return id;
  }
  
  public float distance(Point2D destination) {
    Point2D source = getAveragePosition();
    
    if(source != null)
      return (float) Math.sqrt(Math.pow(getAveragePosition().getX() - destination.getX(), 2) + Math.pow(getAveragePosition().getY() - destination.getY(), 2));
    else
      return 0;
  }
  
  public boolean containsTurtle(int turtle) {
    return turtles.contains(turtle);    
  }
  
  public void addTurtle(int turtle, Point2D position) throws Exception {
    if(containsTurtle(turtle))
      throw new Exception("Turtle already in set");
    
    this.turtles.add(turtle);
    this.positions.add(position);
  }
  
  public int[] getTurtles() {
    int[] turtlesArray = new int[this.turtles.size()];
    for(int i = 0; i < this.turtles.size(); i++) {
      turtlesArray[i] = this.turtles.get(i);
    }
    return turtlesArray;
  }

  public List<Rectangle> createShape(double opacity) {
    this.shapes.clear();
    Point2D position = getAveragePosition();
    
    double rectSize = 26.0;
    double offsetX = 2;
    double offsetY = 2;
    
    if(position != null) {
      Rectangle opponent = new Rectangle(position.getX() - (rectSize / 2), position.getY() - (rectSize / 2), rectSize, rectSize);
      opponent.getStyleClass().add("default-background-combined-shape");
      opponent.setOpacity(opacity);
      this.shapes.add(opponent);
      
      double height = (rectSize - offsetY) / this.configuration.MaxTurtles;
      
      for(int index = 0; index < this.configuration.MaxTurtles; index++) {
        if(containsTurtle(index)) { // Show opponents that see the opponent
        //if(!containsTurtle(index)) { // Show opponents that NOT see the opponent
          Rectangle turtleRect = new Rectangle(position.getX() - ((rectSize - offsetX) / 2), (position.getY() - ((rectSize - offsetY) / 2)) + (height * index) , rectSize - offsetX, height);
          turtleRect.getStyleClass().add(String.format("default-color%d-fill", index));
          turtleRect.setOpacity(opacity - 0.1);
          this.shapes.add(turtleRect);
        }
      }
      
    }
  
    return this.shapes; 
  }
  
  public List<Rectangle> getShape() {
    return this.shapes; 
  }
}