package prototype.object;

import java.time.LocalDateTime;
import java.util.List;
import javafx.scene.Scene;
import org.dockfx.DockPos;
import prototype.chart.AgentChart;
import prototype.chart.CategoricalChart;
import prototype.chart.DockElement;
import prototype.chart.FieldCanvas;
import prototype.chart.XYBaseChart;

public class LayoutChart {
  private final String type;
  private final String position;
  private final String time;
  private final int[] selectedTurtles;
  private final boolean liveUpdate;
  
  private final String parameter;
  private final int parameterIndex;
  private final String parameterValue;
  
  private final boolean turtleHistory;
  
  public LayoutChart(String type, String position, String time, int[] selectedTurtles, boolean liveUpdate,
          String parameter, int parameterIndex, String parameterValue,
          boolean turtleHistory) {
    this.type = type;
    this.position = position;
    this.time = time;
    this.selectedTurtles = selectedTurtles;
    this.liveUpdate = liveUpdate;
    
    this.parameter = parameter;
    this.parameterIndex = parameterIndex;
    this.parameterValue = parameterValue;
    
    this.turtleHistory = turtleHistory;
  }
  
  public DockElement getChart(Scene scene, List<Turtle> data) {
    switch(this.type)
    {
      case "Field":
        return new FieldCanvas(data, liveUpdate, selectedTurtles, turtleHistory);
      case "Turtle-chart":
        return new AgentChart(scene, selectedTurtles, parameter, parameterIndex, parameterValue, data,  liveUpdate);
      case "Categorical-chart":
        return new CategoricalChart(scene, selectedTurtles, parameter, parameterIndex, parameterValue, data, liveUpdate);
      case "Scatter-chart":
        return new XYBaseChart(scene, XYBaseChart.ChartType.Scatter, selectedTurtles, parameter, parameterIndex, parameterValue, data, liveUpdate);
      case "Line-chart":
        return new XYBaseChart(scene, XYBaseChart.ChartType.Line, selectedTurtles, parameter, parameterIndex, parameterValue, data, liveUpdate);
    }
    return null;
  }
  
  public DockPos getPosition() {
    switch(this.position)
    {
      case "BOTTOM":
        return DockPos.BOTTOM;
      case "CENTER":
        return DockPos.CENTER;
      case "LEFT":
        return DockPos.LEFT;
      case "RIGHT":
        return DockPos.RIGHT;
      case "TOP":
        return DockPos.TOP;
    }
    return null;
  }
  
  public LocalDateTime getTime() {
    try {
      if(this.time != null && this.time != "")
        return LocalDateTime.parse(this.time);
    } catch(Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }
}