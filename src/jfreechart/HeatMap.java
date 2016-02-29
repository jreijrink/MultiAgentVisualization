package jfreechart;

import java.awt.Point;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class HeatMap {
  private static final int RASTER_WIDTH = 50;
  private static final int RASTER_HEIGHT = 100;
  
  private final double width;
  private final double height;
  private final int[][] raster;
  
  private int maxValue;
  
  public HeatMap(double width, double height) {
    this.width = width;
    this.height = height;
    this.raster = new int[RASTER_WIDTH][RASTER_HEIGHT];
    this.maxValue = 0;
  }
  
  public void addPosition(double x, double y) {
    Point position = normalize(x, y);
    if(position != null) {
      raster[position.x][position.y] += 1;
      int value = raster[position.x][position.y];
      if(value > maxValue)
        maxValue = value;
    }
  }
  
  public void draw(GraphicsContext g) {
    for (int column = 0; column < RASTER_WIDTH; column++) {
      for (int row = 0; row < RASTER_HEIGHT; row++) {
        int ammount = raster[column][row];
        if(ammount > 0) {
          double value = ((double)ammount / (double)maxValue);
          Color gradientColor = new Color(1, 1, 1, 0.4 + (value * 0.6));
          g.setStroke(gradientColor);
          g.setFill(gradientColor);
          
          int x = (int)(Math.floor((double)column / RASTER_WIDTH * width));
          int y = (int)(Math.floor((double)row / RASTER_HEIGHT * height));
          int cell_width = ((int)(Math.floor((double)(column + 1) / RASTER_WIDTH * width))) - x;
          int cell_height = ((int)(Math.floor((double)(row + 1) / RASTER_HEIGHT * height))) - y;
          
          g.fillRect(x, y, cell_width, cell_height);
        }
      }
    }
  }
  
  private Point normalize(double x, double y) {
    int x_norm = (int)Math.floor((x / width) * (double)RASTER_WIDTH);
    int y_norm = (int)Math.floor((y / height) * (double)RASTER_HEIGHT);
    x_norm = Math.max(x_norm, 0);
    y_norm = Math.max(y_norm, 0);
    x_norm = Math.min(x_norm, RASTER_WIDTH - 1);
    y_norm = Math.min(y_norm, RASTER_HEIGHT - 1);
    return new Point(x_norm, y_norm);
  }
}
