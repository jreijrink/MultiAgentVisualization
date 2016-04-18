package prototype.object;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import prototype.settings.Configuration;

public class CombinedOpponent {
  private List<Point2D> positions;
  private List<Integer> turtles;
  private List<Rectangle> shapes;
  private Configuration configuration;
  
  public CombinedOpponent() {
    this.positions = new ArrayList();
    this.turtles = new ArrayList();
    this.shapes = new ArrayList();
    this.configuration = new Configuration();
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
  
  public float distance(Point2D destination) {
    Point2D source = getAveragePosition();
    
    if(source != null)
      return (float) Math.sqrt(Math.pow(getAveragePosition().getX() - destination.getX(), 2) + Math.pow(getAveragePosition().getY() - destination.getY(), 2));
    else
      return 0;
  }
  
  public boolean containtTurtle(int turtle) {
    return turtles.contains(turtle);    
  }
  
  public void addTurtle(int turtle, Point2D position) throws Exception {
    if(containtTurtle(turtle))
      throw new Exception("Turtle already in set");      
    this.turtles.add(turtle);
    this.positions.add(position);
  }

  public List<Rectangle> createShape() {
    this.shapes.clear();
    Point2D position = getAveragePosition();
    
    double rectSize = 26.0;
    double offsetX = 2;
    double offsetY = 2;
    
    if(position != null) {
      Rectangle opponent = new Rectangle(position.getX() - (rectSize / 2), position.getY() - (rectSize / 2), rectSize, rectSize);
      opponent.getStyleClass().add("default-color-opponent-combined");
      this.shapes.add(opponent);
      
      double height = (rectSize - offsetY) / this.configuration.MaxTurtles;
      
      for(int index = 0; index < this.configuration.MaxTurtles; index++) {
        if(containtTurtle(index)) {
          Rectangle turtleRect = new Rectangle(position.getX() - ((rectSize - offsetX) / 2), (position.getY() - ((rectSize - offsetY) / 2)) + (height * index) , rectSize - offsetX, height);
          turtleRect.getStyleClass().add(String.format("default-color%d-opponent-combined", index));
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