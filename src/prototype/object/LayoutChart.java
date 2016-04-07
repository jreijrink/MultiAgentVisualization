package prototype.object;

import java.time.LocalDateTime;
import javafx.scene.Scene;
import org.dockfx.DockPos;
import prototype.chart.AgentChart;
import prototype.chart.CategoricalChart;
import prototype.chart.Chart;
import prototype.chart.FieldCanvas;
import prototype.chart.XYBaseChart;

public class LayoutChart {
  private final String type;
  private final String position;
  private final String time;
  
  public LayoutChart(String type, String position, String time) {
    this.type = type;
    this.position = position;
    this.time = time;
  }
  
  public Chart GetChartType(Scene scene) {
    switch(this.type)
    {
      case "Field":
        return new FieldCanvas();
      case "Agent-chart":
        return new AgentChart(scene);
      case "Categorical-chart":
        return new CategoricalChart(scene);
      case "Scatter-chart":
        return new XYBaseChart(scene, XYBaseChart.ChartType.Scatter);
      case "Line-chart":
        return new XYBaseChart(scene, XYBaseChart.ChartType.Line);
    }
    return null;
  }
  
  public DockPos GetPosition() {
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
  
  public LocalDateTime GetTime() {
    try {
      if(this.time != null && this.time != "")
        return LocalDateTime.parse(this.time);
    } catch(Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }
}