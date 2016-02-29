package jfreechart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javax.swing.event.EventListenerList;

public class AgentPlot {
  
  protected EventListenerList listenerList = new EventListenerList();
  
  private Scene scene;
  private List<TimeFrame> data;
    
  private BorderPane rootPane; 
  private ScatterChart<Number,String> scattterChart;
  
  public AgentPlot(Scene scene, List<TimeFrame> data) {
    this.scene = scene;
    this.data = data;
    this.rootPane = new BorderPane();
    initialize();
  }
  
  public Node getChart() {
    return rootPane;
  }
  
  public void updateData(List<TimeFrame> data) {
    this.data = data;
    initialize();
  }
    
  private void initialize() {
    if(data.size() > 0) {
      createChart();
    }
  }
  
  private void createChart() {
    ObservableList<String> categories = FXCollections.observableArrayList();
    for(int i = 0; i < data.get(0).getTurtles().size(); i++) {
      categories.add(String.format("Turtle %d", i + 1));
    }
    final NumberAxis xAxis = new NumberAxis(0, data.size(), data.size() / 5);
    final CategoryAxis yAxis = new CategoryAxis(categories);
    
    this.scattterChart = new ScatterChart<>(xAxis, yAxis);
    
    this.scattterChart.widthProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) -> {
      plotData();
    });
    this.scattterChart.heightProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) -> {
      plotData();
    });
    
    if(data.size() > 0) {

      xAxis.setLabel("Time");

      this.scattterChart.getData().addAll(getData());
    }
    
    rootPane.getChildren().clear();
    rootPane.setCenter(this.scattterChart);

    new java.util.Timer().schedule( 
      new java.util.TimerTask() {
        @Override
        public void run() {
          Platform.runLater(new Runnable() {
              @Override
              public void run() {
                plotData();
              }
            });
          }
        }, 500);
        
  }
  
  private Collection getData() {
    List<XYChart.Series> seriesList = new ArrayList<>();

    if(data.size() > 0) {
      int turtles = data.get(0).getTurtles().size();
      for(int turtle =  0; turtle < turtles; turtle++) {      
        seriesList.add(getSeries(turtle)); 
      }
    }

    return seriesList;
  }
  
  private XYChart.Series getSeries(int turtle) {
      XYChart.Series series = new XYChart.Series();
      //series.setName(String.format("State %d", turtle + 1));
    
      //List<XYChart.Data> elements = new ArrayList();

      //ObservableList<XYChart.Data> data = series.getData();
      //data.addAll(elements);
      
      return series;
  }
  
  private void plotData() {
    
    List<Node> pointChildren = new ArrayList();
    for(Node child : this.rootPane.getChildren()) {
      if(child.getClass() == Rectangle.class && child.getId() == null) {
        pointChildren.add(child);
      }
    }
    this.rootPane.getChildren().removeAll(pointChildren);
    
    if(data.size() > 0) {
      int turtles = data.get(0).getTurtles().size();

      for(int turtleIndex =  0; turtleIndex < turtles; turtleIndex++) {    
        CategoryAxis yAxis = (CategoryAxis) scattterChart.getYAxis();
        NumberAxis xAxis = (NumberAxis) scattterChart.getXAxis();

        double xAxisShift = getSceneXShift(xAxis);
        double yAxisShift = getSceneYShift(yAxis);
        
        double agent1YPosition = yAxis.getDisplayPosition("Turtle 1");
        double agent2YPosition = yAxis.getDisplayPosition("Turtle 2");
        double height = Math.abs(agent2YPosition - agent1YPosition);
        
        double currentRole = Double.MIN_VALUE;
        double currentPosition = xAxis.getDisplayPosition(0);
        
        for(int frameIndex = 0; frameIndex < data.size(); frameIndex++) {
          Turtle turtle = data.get(frameIndex).getTurtles().get(turtleIndex);

          double[][] values = turtle.getParameters().getValue("roleId");
          if(values.length > 0 && values[0].length > 0) {
            double roleid = values[0][0];
            if(currentRole == Double.MIN_VALUE)
              currentRole = roleid;
            
            if(roleid != currentRole || frameIndex >= data.size() - 1) {
              double xPosition = xAxis.getDisplayPosition(turtle.timeFrame);
              double yPosition = yAxis.getDisplayPosition(String.format("Turtle %d", turtleIndex + 1));

              Rectangle selectionPoint = RectangleBuilder.create()
                      .x(currentPosition + xAxisShift)
                      .y(yPosition + yAxisShift - (height / 2))
                      .height(height)
                      .width(xPosition - currentPosition)
                      .styleClass(String.format("default-color%d-status-symbol", Math.round(currentRole)))
                      .build();

               this.rootPane.getChildren().add(selectionPoint);
               
               currentPosition = xPosition;
               currentRole = roleid;
            }
          }
        }
      }
    }
  }
  
  private double getSceneXShift(Node node) { 
    double shift = 0; 
    do {  
        shift += node.getLayoutX();  
        node = node.getParent(); 
    } while (node != null && node.getClass() != ScatterChart.class); 
    return shift; 
  }
  
  private double getSceneYShift(Node node) { 
    double shift = 0; 
    do {  
        shift += node.getLayoutY();
        node = node.getParent(); 
    } while (node != null && node.getClass() != ScatterChart.class); 
    return shift; 
  }
}
