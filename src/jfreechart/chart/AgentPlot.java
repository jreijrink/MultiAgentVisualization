package jfreechart.chart;

import com.sun.javafx.charts.Legend;
import com.sun.javafx.charts.Legend.LegendItem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import static jfreechart.chart.Chart.getCheckbox;
import static jfreechart.chart.Chart.getTurtleListView;
import jfreechart.listener.SelectionEventListener;
import jfreechart.object.StringValuePair;
import jfreechart.object.TimeFrame;
import jfreechart.object.Turtle;

public class AgentPlot implements Chart {
  
  private Scene scene;
  private int[] selectedTurtles;
  private List<TimeFrame> data;
  private boolean liveUpdate;
  
  private BorderPane rootPane; 
  private ScatterChart<Number,String> scattterChart;
  
  private Rectangle selectionRectangle;
  private Point2D selectionPoint;
  
  private double initSelectionX = 0;
  private double initSelectionWidth = 0;
  private Object[] initSelectionData = new Object[]{ 0, 0 };
  
  private int selectedStartIndex;
  private int selectedEndIndex;
  
  public AgentPlot(Scene scene, int[] selectedTurtles, List<TimeFrame> data, boolean liveUpdate) {
    this.scene = scene;
    this.selectedTurtles = selectedTurtles;
    this.data = data;
    this.liveUpdate = liveUpdate;
    this.rootPane = new BorderPane();
    this.rootPane.getStylesheets().add("jfreechart/plot.css");
    initialize();
  }
  
  public AgentPlot(Scene scene) {
    this(scene, new int[]{ 0, 1, 2, 3, 4, 5, 6 }, new ArrayList(), false);
  }
  
  @Override
  public Node getNode() {
    return rootPane;
  }
  
  @Override
  public String getName() {
    return "AgentPlot";
  }
  
  @Override
  public void addSelectionEventListener(SelectionEventListener listener) {
    listenerList.add(SelectionEventListener.class, listener);
  }
  
  @Override
  public void updateData(List<TimeFrame> data) {
    this.data = data;
    initialize();
  }
  
  @Override
  public void selectFrames(int startIndex, int endIndex, boolean drag) {
    if((!drag || liveUpdate) && data.size() > 0) {
      
      selectedStartIndex = startIndex;
      selectedEndIndex = endIndex;
      
      NumberAxis xAxis = (NumberAxis) scattterChart.getXAxis();
      double xAxisShift = getSceneXShift(xAxis);
      double start = xAxis.getDisplayPosition(startIndex);
      double end = xAxis.getDisplayPosition(endIndex);
      
      if(selectionRectangle != null) {
        selectionRectangle.setX(xAxisShift + start);
        selectionRectangle.setWidth(end - start);
        selectionRectangle.setUserData(new Object[]{ startIndex, endIndex });
      } else {
        initSelectionX = xAxisShift + start;
        initSelectionWidth = end - start;
        initSelectionData = new Object[]{ startIndex, endIndex };
      }
    }
  }
  
  private void notifyListeners(int startIndex, int endIndex, boolean drag) {
    List<Integer> selectedTimeFrames = new ArrayList();
    Object[] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i = i+2) {
      if (listeners[i] == SelectionEventListener.class) {
        ((SelectionEventListener) listeners[i+1]).timeFrameSelected(startIndex, endIndex, drag);
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
    
    xAxis.widthProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) -> {
      Timer timer = new java.util.Timer();
      timer.schedule( 
        new java.util.TimerTask() {
          @Override
          public void run() {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                  resizePlot();
                  timer.cancel();
                  timer.purge();
                }
              });
            }
          }, 0);
    });
    
    yAxis.heightProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) -> {      
      Timer timer = new java.util.Timer();
      timer.schedule( 
        new java.util.TimerTask() {
          @Override
          public void run() {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                  resizePlot();
                  timer.cancel();
                  timer.purge();
                }
              });
            }
          }, 0);
    });
    
    if(data.size() > 0) {
      xAxis.setLabel("Time");
      this.scattterChart.getData().addAll(getData());
    }
    
    rootPane.getChildren().clear();
    rootPane.setCenter(this.scattterChart);

      Timer timer = new java.util.Timer();
      timer.schedule( 
        new java.util.TimerTask() {
        @Override
        public void run() {
          Platform.runLater(new Runnable() {
              @Override
              public void run() {
                plotData();
                selectFrames(selectedStartIndex, selectedEndIndex, false);
                timer.cancel();
                timer.purge();
              }
            });
          }
        }, 0);
  }
  
  @Override
  public void showParameterDialog() {
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(10, 10, 10, 10));
    
    Dialog<Boolean> dialog = new Dialog();
    dialog.setTitle("Agentplot options");
    dialog.setHeaderText("Choose agentplot options");
    dialog.setContentText("Choose agentplot options:");

    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    CheckBox liveCheckbox = getCheckbox("Live update", liveUpdate);
    
    ListView listView = getTurtleListView(selectedTurtles); 
    
    grid.add(liveCheckbox, 0, 0);
    
    grid.add(new Label("Turtles"), 0, 1);
    grid.add(listView, 0, 2);

    dialog.getDialogPane().setContent(grid);
    listView.setPrefHeight(200);

    dialog.setResultConverter(dialogButton -> {
      if(dialogButton == ButtonType.OK) {
        ObservableList<StringValuePair<String, Integer>> selectedItems = listView.getSelectionModel().getSelectedItems();
        selectedTurtles = new int[selectedItems.size()];
        for(int i = 0; i < selectedItems.size(); i++) {
          selectedTurtles[i] = selectedItems.get(i).getValue();
        }
        
        liveUpdate = liveCheckbox.isSelected();

        return true;
      }
      return null;
    });

    Optional<Boolean> result = dialog.showAndWait();

    if(result.isPresent() && result.get() != null) {
      initialize();
    }
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
              .x(initSelectionX)
              .y(minY + yAxisShift - (height / 2))
              .height(maxY + (height / 2) - 5)
              .width(initSelectionWidth)
              .fill(Color.web("0x222222"))
              .opacity(0.3)
              .id("selection")
              .userData(initSelectionData)
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

      selectionPoint = new Point2D(event.getX(), event.getY());
      
      notifyListeners(0, 0, false);
    });

    node.setOnMouseDragged((MouseEvent event) -> {
      System.out.printf("MOUSE_DRAGGED \n");      

      double xAxisShift = getSceneXShift(xAxis);
      double yAxisShift = getSceneYShift(yAxis);
      
      Rectangle selection = getSelectionRectangle(event.getX(), event.getY(), xAxisShift, yAxisShift, xAxis.getWidth(), yAxis.getHeight());
            
      int start = xAxis.getValueForDisplay(selection.getX() - xAxisShift).intValue();
      int end = xAxis.getValueForDisplay(selection.getX() + selection.getWidth() - xAxisShift).intValue();
      
      notifyListeners(start, end, true);
      
      selectionRectangle.setX(selection.getX());
      selectionRectangle.setWidth(selection.getWidth());
    });
    
    node.setOnMouseReleased((MouseEvent event) -> {
      System.out.printf("MOUSE_RELEASED \n");

      double xAxisShift = getSceneXShift(xAxis);
      double yAxisShift = getSceneYShift(yAxis);

      Rectangle selection = getSelectionRectangle(event.getX(), event.getY(), xAxisShift, yAxisShift, xAxis.getWidth(), yAxis.getHeight());
      
      int start = xAxis.getValueForDisplay(selection.getX() - xAxisShift).intValue();
      int end = xAxis.getValueForDisplay(selection.getX() + selection.getWidth() - xAxisShift).intValue();
      
      notifyListeners(start, end, false);
    });
  }
  
  private Rectangle getSelectionRectangle(double mouseX, double mouseY, double xShift, double yShift, double chartWidth, double chartHeight) {
    
    //Bound the rectangle to be only within the chart
    mouseX = Math.max(mouseX, xShift);
    mouseX = Math.min(mouseX, chartWidth + xShift);    
    mouseY = Math.max(mouseY, yShift);
    mouseY = Math.min(mouseY, chartHeight + yShift);
    
    double width = mouseX - selectionPoint.getX();
    double height = mouseY - selectionPoint.getY();
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
