package jfreechart;

import com.sun.javafx.charts.Legend;
import com.sun.javafx.charts.Legend.LegendItem;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.util.Pair;
import javax.swing.event.EventListenerList;

public class AgentPlot {
  
  protected EventListenerList listenerList = new EventListenerList();
  
  private Scene scene;
  private int[] selectedTurtles;
  private List<TimeFrame> data;
    
  private BorderPane rootPane; 
  private ScatterChart<Number,String> scattterChart;
  
  private Rectangle selectionRectangle;
  private Point2D selectionPoint;
  
  public AgentPlot(Scene scene, int[] selectedTurtles, List<TimeFrame> data) {
    this.scene = scene;
    this.selectedTurtles = selectedTurtles;
    this.data = data;
    this.rootPane = new BorderPane();
    this.rootPane.getStylesheets().add("jfreechart/plot.css");
    initialize();
  }
  
  public Node getChart() {
    return rootPane;
  }
  
  public void addSelectionEventListener(SelectionEventListener listener) {
    listenerList.add(SelectionEventListener.class, listener);
  }
  
  public void updateData(List<TimeFrame> data) {
    this.data = data;
    initialize();
  }
    
  public void selectFrames(int startIndex, int endIndex) {
    NumberAxis xAxis = (NumberAxis) scattterChart.getXAxis();
    double xAxisShift = getSceneXShift(xAxis);
    double start = xAxis.getDisplayPosition(startIndex);
    double end = xAxis.getDisplayPosition(endIndex);
    selectionRectangle.setX(xAxisShift + start);
    selectionRectangle.setWidth(end - start);
    selectionRectangle.setUserData(new Object[]{ startIndex, endIndex });
  }
  
  private void notifyListeners(int startIndex, int endIndex) {
    List<Integer> selectedTimeFrames = new ArrayList();
    Object[] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i = i+2) {
      if (listeners[i] == SelectionEventListener.class) {
        ((SelectionEventListener) listeners[i+1]).timeFrameSelected(startIndex, endIndex);
      }
    }
  }
  
  private void initialize() {
    if(data.size() > 0) {
      createChart();
    }
  }
  
  private void createChart() {
    ObservableList<String> categories = FXCollections.observableArrayList();
    for(int turtle : selectedTurtles) {
      categories.add(String.format("Turtle %d", turtle + 1));
    }
    
    final NumberAxis xAxis = new NumberAxis(0, data.size(), data.size() / 5);
    final CategoryAxis yAxis = new CategoryAxis(categories);
    
    this.scattterChart = new ScatterChart<>(xAxis, yAxis);
    
    this.scattterChart.widthProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) -> {
      new java.util.Timer().schedule( 
        new java.util.TimerTask() {
          @Override
          public void run() {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                  resizePlot();
                }
              });
            }
          }, 100);
    });
    
    this.scattterChart.heightProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) -> {      
      new java.util.Timer().schedule( 
        new java.util.TimerTask() {
          @Override
          public void run() {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                  resizePlot();
                }
              });
            }
          }, 100);
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
        }, 100);
  }
  
  private Collection getData() {
    List<XYChart.Series> seriesList = new ArrayList<>();
    
    return seriesList;
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

      double minY = Double.MAX_VALUE;
      double maxY = Double.MIN_VALUE;  

      CategoryAxis yAxis = (CategoryAxis) scattterChart.getYAxis();
      NumberAxis xAxis = (NumberAxis) scattterChart.getXAxis();

      double xAxisShift = getSceneXShift(xAxis);
      double yAxisShift = getSceneYShift(yAxis);

      double height = getRowHeigt();
      
      List<Double> roles = new ArrayList();
      
      //for(int turtleIndex =  0; turtleIndex < turtles; turtleIndex++) {  
      for(int turtleIndex : selectedTurtles) {
        
        double currentRole = Double.MIN_VALUE;
        int currentFrame = 0;
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

              maxY = Math.max(maxY, yPosition);
              minY = Math.min(minY, yPosition);
              
              Rectangle selectionPoint = RectangleBuilder.create()
                      .x(currentPosition + xAxisShift)
                      .y(yPosition + yAxisShift - (height / 2))
                      .height(height)
                      .width(xPosition - currentPosition)
                      .userData(new Object[]{ String.format("Turtle %d", turtleIndex + 1), currentFrame, turtle.timeFrame })
                      .styleClass(String.format("default-color%d-status-symbol", (int)currentRole))
                      .build();

              if(!roles.contains(roleid)) {
                roles.add(roleid);
              }

               this.rootPane.getChildren().add(selectionPoint);
               
               currentPosition = xPosition;
               currentFrame = turtle.timeFrame;
               currentRole = roleid;
            }
          }
        }
      }

      selectionRectangle = RectangleBuilder.create()
              .x(0)
              .y(minY + yAxisShift - (height / 2))
              .height(maxY + (height / 2) - 5)
              .width(0)
              .fill(Color.web("0x222222"))
              .opacity(0.3)
              .id("selection")
              .userData(new Object[]{ 0, 0 })
              .build();    
      rootPane.getChildren().add(selectionRectangle);
      
      for(Node child : this.rootPane.getChildren()) {
        if(child.getClass() == Rectangle.class) {
          createRectangleSelectionEvents(child, rootPane, xAxis, yAxis);
        }
      }
      
      createLegend(roles);
    }
  }
  
  private void createLegend(List<Double> roles) {
      Legend legend = (Legend)scattterChart.lookup(".chart-legend");
      legend.getStylesheets().add("jfreechart/plot.css");
    
      Collections.sort(roles);
      
      List<LegendItem> items = new ArrayList();
      
      for(double role : roles) {
          Rectangle legendPoint = RectangleBuilder.create()
                  .height(10)
                  .width(10)
                  .build();
        
        LegendItem item = new Legend.LegendItem(String.format("State %d", (int)role), legendPoint);
        item.getSymbol().getStyleClass().add(String.format("default-color%d-status-symbol", (int)role));
        
        items.add(item);
      }
      
      legend.getItems().setAll(items);
  }
  
  private void resizePlot() {
    
    System.out.println("RESIZEAGENT");
    
    CategoryAxis yAxis = (CategoryAxis) scattterChart.getYAxis();
    NumberAxis xAxis = (NumberAxis) scattterChart.getXAxis();

    double xAxisShift = getSceneXShift(xAxis);
    double yAxisShift = getSceneYShift(yAxis);
    
    double height = getRowHeigt();

    double minY = Double.MAX_VALUE;
    double maxY = Double.MIN_VALUE;  

    for(Node child : this.rootPane.getChildren()) {
      if(child.getClass() == Rectangle.class && child.getId() == null) {
        Rectangle rectChild = (Rectangle)child;
        
        Object[] userData = (Object[])rectChild.getUserData();
        String turtle = (String)userData[0];
        int startFrame = (int)userData[1];
        int endFrame = (int)userData[2];

        double startPosition = xAxis.getDisplayPosition(startFrame);
        double endPosition = xAxis.getDisplayPosition(endFrame);
        double yPosition = yAxis.getDisplayPosition(turtle);

        maxY = Math.max(maxY, yPosition);
        minY = Math.min(minY, yPosition);

        rectChild.setX(startPosition + xAxisShift);
        rectChild.setWidth(endPosition - startPosition);
        rectChild.setY(yPosition + yAxisShift - (height / 2));
        rectChild.setHeight(height);
      }
    }

    if(selectionRectangle != null) {
      Object[] userData = (Object[])selectionRectangle.getUserData();
      int startFrame = (int)userData[0];
      int endFrame = (int)userData[1];

      double start = xAxis.getDisplayPosition(startFrame);
      double end = xAxis.getDisplayPosition(endFrame);

      selectionRectangle.setX(xAxisShift + start);
      selectionRectangle.setWidth(end - start);

      selectionRectangle.setY(minY + yAxisShift - (height / 2));
      selectionRectangle.setHeight(maxY + (height / 2) - 5);
    }
  }
  
  private double getRowHeigt() {  
      CategoryAxis yAxis = (CategoryAxis) scattterChart.getYAxis();
      
      double height = yAxis.getHeight();
      List<String> categories = yAxis.getCategories();
      if(categories.size() >= 2) {
        double agent1YPosition = yAxis.getDisplayPosition(categories.get(0));
        double agent2YPosition = yAxis.getDisplayPosition(categories.get(1));
        height = Math.abs(agent2YPosition - agent1YPosition);
      }
      return height;
  }
  
  private void createRectangleSelectionEvents(Node node, Pane parent, NumberAxis xAxis, CategoryAxis yAxis) {
    ScatterChart<Number,String> scattterChart = (ScatterChart<Number,String>)rootPane.getCenter();
    
    node.setOnMousePressed((MouseEvent event) -> {
      System.out.printf("MOUSE_PRESSED \n");

      double xChartShift = getSceneXShift(node);
      double yChartShift = getSceneYShift(node);

      selectionPoint = new Point2D(event.getX() + xChartShift, event.getY() + yChartShift);
      
      notifyListeners(0, 0);
    });

    node.setOnMouseDragged((MouseEvent event) -> {
      System.out.printf("MOUSE_DRAGGED \n");      

      double xChartShift = getSceneXShift(node);
      double yChartShift = getSceneYShift(node);
      double xAxisShift = getSceneXShift(xAxis);
      double yAxisShift = getSceneYShift(yAxis);

      Rectangle selection = getSelectionRectangle(event.getX(), event.getY(), xChartShift, yChartShift, xAxis.getWidth() + xAxisShift - xChartShift, yAxis.getHeight() + yAxisShift- yChartShift);
            
      selectionRectangle.setX(selection.getX());
      selectionRectangle.setWidth(selection.getWidth());
    });
    
    node.setOnMouseReleased((MouseEvent event) -> {
      System.out.printf("MOUSE_RELEASED \n");      

      double xChartShift = getSceneXShift(node);
      double yChartShift = getSceneYShift(node);
      double xAxisShift = getSceneXShift(xAxis);
      double yAxisShift = getSceneYShift(yAxis);

      Rectangle selection = getSelectionRectangle(event.getX(), event.getY(), xChartShift, yChartShift, xAxis.getWidth() + xAxisShift - xChartShift, yAxis.getHeight() + yAxisShift- yChartShift);
      
      int start = xAxis.getValueForDisplay(selection.getX() - xAxisShift).intValue();
      int end = xAxis.getValueForDisplay(selection.getX() + selection.getWidth() - xAxisShift).intValue();
      
      notifyListeners(start, end);
    });
  }
  
  private Rectangle getSelectionRectangle(double mouseX, double mouseY, double xShift, double yShift, double chartWidth, double chartHeight) {
    
    //Bound the rectangle to be only within the chart
    mouseX = Math.max(mouseX, 0);
    mouseX = Math.min(mouseX, chartWidth);    
    mouseY = Math.max(mouseY, 0);
    mouseY = Math.min(mouseY, chartHeight);
    
    double width = mouseX - selectionPoint.getX() + xShift;    
    double height = mouseY - selectionPoint.getY() + yShift;
    double x = Math.max(0, selectionPoint.getX() + Math.min(width, 0));
    double y = selectionPoint.getY() + Math.min(height, 0);
    
    return new Rectangle(x, y, Math.abs(width), Math.abs(height));
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
