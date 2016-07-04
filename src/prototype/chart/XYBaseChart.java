package prototype.chart;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import org.apache.pivot.util.Console;
import static prototype.chart.DockElement.getAllTurtles;
import prototype.listener.SelectionEventListener;
import prototype.object.Parameter;
import prototype.object.Turtle;
import prototype.object.Filter;
import prototype.object.Range;

public final class XYBaseChart extends BaseChart {  
  public enum ChartType { Scatter, Line };  
  private final ChartType type;
  private Timer resizeTimer;
  private TimerTask resizeTask;
  
  public XYBaseChart(Scene scene, ChartType type, int[] selectedTurtles, String yParameter, int yParameterIndex, String yParameterValue, List<Turtle> data,  boolean liveUpdate) {
    super(scene, selectedTurtles, yParameter, yParameterIndex, yParameterValue, data,  liveUpdate);
    this.type = type;    
    initRootPaneListeners();
  }
  
  public XYBaseChart(Scene scene, ChartType type, List<Turtle> data, int selectionStart, int selectionEnd, boolean forward) {
    super(scene, data, selectionStart, selectionEnd, forward);
    this.type = type;
    
    if(this.parameterMap.getAllParameters().size() > 0) {
    Parameter firstParameter = this.parameterMap.getAllParameters().get(0);
      this.parameter = firstParameter.getName();
      if(firstParameter.getValues().size() > 0) {
        this.parameterValue = firstParameter.getValues().get(0).getName();
      }
    }
    
    initRootPaneListeners();    
    initialize(false);
  }
  
  @Override
  public String getName() {
    switch(type) {
      case Scatter:
        return "Scatter-chart";
      case Line:
      return "Line-chart";
    }
    return "XY-chart";
  }
    
  private void initRootPaneListeners() {

    resizeTimer = new Timer();
    resizeTask = new TimerTask() {
        @Override
        public void run() {
          initialize(true);
          resizeTimer.cancel();
          resizeTimer.purge();
        }
      };

    this.rootPane.widthProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) -> {
      resizeTask.cancel();
      resizeTask = new TimerTask() {
        @Override
        public void run() {
          initialize(true);
          resizeTimer.cancel();
          resizeTimer.purge();
        }
      };
      resizeTimer.cancel();
      resizeTimer.purge();
      resizeTimer = new Timer();
      resizeTimer.schedule(resizeTask, 1000);
    });
    
    this.rootPane.heightProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) -> {  
      resizeTask.cancel();
      resizeTask = new TimerTask() {
        @Override
        public void run() {
          initialize(true);
          resizeTimer.cancel();
          resizeTimer.purge();
        }
      };
      resizeTimer.cancel();
      resizeTimer.purge();
      resizeTimer = new Timer();
      resizeTimer.schedule(resizeTask, 1000);
    });    
  }
  
  @Override
  boolean isCategorical() {
    return false;
  }
  
  @Override
  void createChart(Collection datapoints) {
    
    NumberAxis xAxis = new NumberAxis();
    try {
        int timeframes = this.data.get(0).getTimeFrameCount();
        double scale = getScale(timeframes);
        if(zoomRange == null)
          xAxis = new NumberAxis(0, timeframes, scale);
        else
          xAxis = new NumberAxis(zoomRange.getMin(), zoomRange.getMax(), scale);
    } catch(Exception ex) { }
    
    NumberAxis yAxis = new NumberAxis();
    try {
      double min = Double.MAX_VALUE;
      double max = Double.MIN_VALUE;
      for(Turtle turlte : data) {
        for(DataPoint value : turlte.getAllValues(parameter, parameterIndex, parameterValue)) {
          if(!value.outOfRange()) {
            min = Math.min(value.getValue(), min);
            max = Math.max(value.getValue(), max);
          }
        }
      }
      if(min != Double.MAX_VALUE && max != Double.MIN_VALUE) {
        double scale = getScale(max - min);
        yAxis = new NumberAxis(min, max, scale);
      }
    } catch(Exception ex) { }
    
    switch(type) {
      case Scatter:
        this.XYChart = new ScatterChart<>(xAxis, yAxis);
        break;
      case Line:
        this.XYChart = new LineChart<>(xAxis, yAxis);
        break;
    }
    
    xAxis.widthProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) -> {   
      resizeSelection();
      Timer timer = new java.util.Timer();
      timer.schedule( 
        new java.util.TimerTask() {
          @Override
          public void run() {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                  resizeSelection();
                  timer.cancel();
                  timer.purge();
                }
              });
            }
          }, 0);
    });
    
    yAxis.heightProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) -> {      
      resizeSelection();
      Timer timer = new java.util.Timer();
      timer.schedule( 
        new java.util.TimerTask() {
          @Override
          public void run() {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                  resizeSelection();
                  timer.cancel();
                  timer.purge();
                }
              });
            }
          }, 0);
    });
        
    Platform.runLater(()->rootPane.getChildren().clear());
    Platform.runLater(()->rootPane.setCenter(this.XYChart));
    
    xAxis.setLabel("Time");
    yAxis.setLabel(this.parameter + " [" + parameterIndex + "] " + " (" + parameterValue + ")");

    Platform.runLater(()-> this.XYChart.getData().setAll(datapoints));
    
    Timer timer = new java.util.Timer();
    timer.schedule( 
      new java.util.TimerTask() {
        @Override
        public void run() {
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              drawInvalidData();
              timer.cancel();
              timer.purge();
            }
          });
        }
      }, 50);
  }
  
  private void resizeSelection() {
    NumberAxis yAxis = (NumberAxis) this.XYChart.getYAxis();
    NumberAxis xAxis = (NumberAxis) this.XYChart.getXAxis();

    double xAxisShift = getSceneXShift(xAxis);
    double yAxisShift = getSceneYShift(yAxis);
    
    if(selectionRectangle != null) {
      Object[] userData = (Object[])selectionRectangle.getUserData();
      int startFrame = (int)userData[0];
      int endFrame = (int)userData[1];

      double start = xAxis.getDisplayPosition(startFrame);
      double end = xAxis.getDisplayPosition(endFrame);

      if(start == end)
        end +=1;
      
      selectionRectangle.setX(xAxisShift + start);
      selectionRectangle.setY(yAxisShift);
      selectionRectangle.setHeight(yAxis.getHeight());

      boolean forward = (boolean)selectionFrame.getUserData();
      if(forward)
        selectionFrame.setX(xAxisShift + end);
      else
        selectionFrame.setX(xAxisShift + start);
      
      selectionFrame.setY(yAxisShift);
      selectionFrame.setHeight(yAxis.getHeight());
    }    

    for(Rectangle filterRectangle : filterRectangles) {
      Object[] userData = (Object[])filterRectangle.getUserData();
      double minValue = (double)userData[0];
      double maxValue = (double)userData[1];

      double start = yAxis.getDisplayPosition(maxValue);
      double end = yAxis.getDisplayPosition(minValue);

      filterRectangle.setY(yAxisShift + start);
      filterRectangle.setHeight(end - start);

      filterRectangle.setX(xAxisShift);
      filterRectangle.setWidth(xAxis.getWidth());
    }
    
    drawInvalidData();
  }
  
  @Override
  void createAxisFilter() {    
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
      for(Rectangle filter : filterRectangles) {
        if(!rootPane.getChildren().contains(filter))
          rootPane.getChildren().add(filter);
        }
      }
    });
    
    NumberAxis xAxis = (NumberAxis) this.XYChart.getXAxis();
    NumberAxis yAxis = (NumberAxis) this.XYChart.getYAxis();
        
    yAxis.setOnMouseMoved((MouseEvent event) -> {
      double yAxisShift = getSceneYShift(yAxis);
      newFilter = true;
      selectedFilterIndex = -1;

      for(Rectangle filter : filterRectangles) {
        if(event.getY() > filter.getY() - yAxisShift && event.getY() < filter.getY() - yAxisShift + filter.getHeight()) {
          selectedFilterIndex = filterRectangles.indexOf(filter);
          newFilter = false;
        }
      }

      if(scene.getCursor() != Cursor.WAIT) {
        if(newFilter) {
          setCursor(Cursor.CROSSHAIR);
        } else {
          setCursor(Cursor.CLOSED_HAND);
        }
      }
    });
    
    yAxis.setOnMouseExited((MouseEvent event) -> {   
      if(scene.getCursor() != Cursor.WAIT)
        setCursor(Cursor.DEFAULT);
    });
    
    yAxis.setOnMousePressed((MouseEvent event) -> {
      if(scene.getCursor() != Cursor.WAIT)
        setCursor(Cursor.V_RESIZE);

      if(newFilter) {
        double yAxisShift = getSceneYShift(yAxis);    
        double xShift = getXShift(yAxis, null, xAxis);
        double yShift = getYShift(yAxis, null, yAxis);

        basePoint = new Point2D(event.getX(), event.getY());

        Rectangle selection = getSelectionRectangle(basePoint, event.getX(), event.getY(), xShift, yShift, xAxis.getWidth(), yAxis.getHeight());

        Rectangle filterRectangle = RectangleBuilder.create()
                .x(getSceneXShift(xAxis))
                .y(selection.getY() + yAxisShift)
                .height(selection.getHeight())
                .width(xAxis.getWidth())
                .fill(Color.web("0x222222"))
                .opacity(0.3)
                .id("filter")
                .build();
        filterRectangle.setUserData(new Object[]{ 0.0, 0.0 });
        rootPane.getChildren().add(filterRectangle);
        filterRectangles.add(filterRectangle);
        selectedFilterIndex = filterRectangles.indexOf(filterRectangle);
        
        createRectangleSelectionEvents(filterRectangle, rootPane, xAxis, yAxis);
      } else {
        if(selectedFilterIndex >= 0 && filterRectangles.size() > selectedFilterIndex) {
          Rectangle removeFilter = filterRectangles.get(selectedFilterIndex);
          for(Turtle turtle : data) {
            Filter filter = (Filter)((Object[])removeFilter.getUserData())[2];
            turtle.removeFilter(filter);
          }
          rootPane.getChildren().remove(removeFilter);
          filterRectangles.remove(removeFilter);

          for(SelectionEventListener listener : listenerList) {
            listener.update();
          }
        }
      }
    });

    yAxis.setOnMouseDragged((MouseEvent event) -> {
      setCursor(Cursor.V_RESIZE);
      
      if(newFilter && selectedFilterIndex >= 0 && filterRectangles.size() > selectedFilterIndex) {
        double yAxisShift = getSceneYShift(yAxis);    
        double xShift = getXShift(yAxis, null, xAxis);
        double yShift = getYShift(yAxis, null, yAxis);

        Rectangle selection = getSelectionRectangle(basePoint, event.getX(), event.getY(), xShift, yShift, xAxis.getWidth(), yAxis.getHeight());

        Rectangle filterRectangle = filterRectangles.get(selectedFilterIndex);
        filterRectangle.setY(selection.getY() + yAxisShift);
        filterRectangle.setHeight(selection.getHeight());
      }
    });
    
    yAxis.setOnMouseReleased((MouseEvent event) -> {
      setCursor(Cursor.DEFAULT);
      
      if(newFilter && selectedFilterIndex >= 0 && filterRectangles.size() > selectedFilterIndex) {
        double yAxisShift = getSceneYShift(yAxis);    
        double xShift = getXShift(yAxis, null, xAxis);
        double yShift = getYShift(yAxis, null, yAxis);

        Rectangle selection = getSelectionRectangle(basePoint, event.getX(), event.getY(), xShift, yShift, xAxis.getWidth(), yAxis.getHeight());

        Rectangle filterRectangle = filterRectangles.get(selectedFilterIndex);
        
        filterRectangle.setY(selection.getY() + yAxisShift);
        filterRectangle.setHeight(selection.getHeight());

        double minValue = yAxis.getValueForDisplay(selection.getY() + selection.getHeight()).doubleValue();
        double maxValue = yAxis.getValueForDisplay(selection.getY()).doubleValue();

        Filter filter = filter(minValue, maxValue);
        
        if(filter != null) {
          filterRectangle.setUserData(new Object[]{ minValue, maxValue, filter });
        } else {
          rootPane.getChildren().remove(filterRectangle);
          filterRectangles.remove(filterRectangle);          
        }
      }
    });
  }
  
  @Override
  List<XYChart.Series> getData() { 
    List<XYChart.Series> seriesList = new ArrayList<>();
        
    if(data.size() > 0) {
      int turtles = data.size();
      double minValue = Double.MAX_VALUE;
      double maxValue = Double.MIN_VALUE;
      int timeframes = 0;
      int before = 0;
      int after = 0;

      Map<Integer, List<DataPoint>> datapoints = new HashMap();
      for(int turtle =  0; turtle < turtles; turtle++) {
        datapoints.put(turtle, new ArrayList());
      }
      
      for (int i : selectedTurtles) {
        Turtle turtle = data.get(i);
        List<DataPoint> values = turtle.getAllValues(parameter, parameterIndex, parameterValue);
        if(zoomRange != null)
          values = values.subList(Math.max(zoomRange.getMin(), 0), Math.min(zoomRange.getMax(), values.size() - 1));
        timeframes = values.size();
        before += values.size();
        datapoints.get(i).addAll(values);

        for(DataPoint value : values) {
          minValue = Math.min(value.getValue(), minValue);
          maxValue = Math.max(value.getValue(), maxValue);
        }
      }

      double xTolerance = (timeframes / rootPane.getWidth()) * 2.0f;
      double yTolerance = ((maxValue - minValue) / rootPane.getHeight()) * 2.0f;
      
      Map<Integer, List<DataPoint>> filteredPoints = simplifyRadialDistance(datapoints, xTolerance, yTolerance);
          
      for(int turtleIndex : filteredPoints.keySet()) {
        
        XYChart.Series series = new XYChart.Series();
        series.setName(String.format("Turtle %d", turtleIndex + 1));

        List<XYChart.Data> elements = new ArrayList();
        
        after += filteredPoints.get(turtleIndex).size();
      
        for (int i = 0; i < filteredPoints.get(turtleIndex).size(); i++) {
          DataPoint filteredPoint = filteredPoints.get(turtleIndex).get(i);
          Point2D pos = filteredPoint.getLocation();
          XYChart.Data element = new XYChart.Data(pos.getX(), pos.getY(), filteredPoint.getIndices());
          elements.add(element);
        }

        ObservableList<XYChart.Data> seriesData = series.getData();
        seriesData.addAll(elements);

        seriesList.add(series);        
      }
      
      System.out.println("FILTER BEFORE: " + before + " AFTER: " + after);
    }

    return seriesList;
  }
  
  private void drawInvalidData() {
      
    List<Node> remove = new ArrayList();
    for(Node child : this.rootPane.getChildren()) {
      if(child.getId() != null && child.getId().equals("InvalidPoint")) {
        remove.add(child);
      }
    }
    this.rootPane.getChildren().removeAll(remove);
    
    if(data.size() > 0) {  
      for(int turtleIndex : selectedTurtles) {
        Turtle turtle = data.get(turtleIndex);
        List<DataPoint> data = turtle.getAllValues(parameter, parameterIndex, parameterValue);

        NumberAxis yAxis = (NumberAxis) XYChart.getYAxis();
        NumberAxis xAxis = (NumberAxis) XYChart.getXAxis();

        double xAxisShift = getSceneXShift(xAxis);
        double yAxisShift = getSceneYShift(yAxis);

        for(DataPoint point : data) {

          if(point.outOfRange() && point.satisfiesFilter()) {

            double xPosition = xAxis.getDisplayPosition(point.getTimeframe());

            if(!point.aboveMin()) {
              //Draw point at bottom     
              Rectangle invalidBlock = RectangleBuilder.create()
                      .x(xPosition + xAxisShift - 2)
                      .y(yAxis.getHeight() + yAxisShift - 2)
                      .height(4)
                      .width(4)
                      .id("InvalidPoint")
                      .styleClass("default-invalid-color")
                      .build();
               this.rootPane.getChildren().add(invalidBlock);     
            } else {
              //Draw point at top 
              Rectangle invalidBlock = RectangleBuilder.create()
                      .x(xPosition + xAxisShift - 2)
                      .y(yAxisShift - 2)
                      .height(4)
                      .width(4)
                      .styleClass("default-invalid-color")
                      .build();
               this.rootPane.getChildren().add(invalidBlock);
           }
          }
        }
      }
    }
  }
  
  private Filter filter(double minValue, double maxValue) {    
    if(minValue != maxValue) {
      Filter filter = new Filter(this, parameter, parameterIndex, parameterValue, getAllTurtles(), new Range(minValue, maxValue));

      for(Turtle turtle : data) {
        turtle.setFilter(filter);
      }

      for(SelectionEventListener listener : listenerList) {
        listener.update();
      }
      
      return filter;
    }
    return null;
  }
    
  private Map<Integer, List<DataPoint>> simplifyRadialDistance(Map<Integer, List<DataPoint>> data, double xTolerance, double yTolerance) {
    List<SimpleEntry<Integer, DataPoint>> sortedPoints = new ArrayList();

    for(Integer turtleIndex : data.keySet()) {
      for(DataPoint point : data.get(turtleIndex)) {
        if(point.isVisible()) {
          DataPoint validPoint = new DataPoint(point.getTimeframe(), point.getValue(), point.getIndices().get(0), point.aboveMin(), point.belowMax(), point.satisfiesFilter());
          sortedPoints.add(new SimpleEntry(turtleIndex, validPoint));
        }
      }
    }

    Collections.sort(sortedPoints, (SimpleEntry<Integer, DataPoint> p1, SimpleEntry<Integer, DataPoint> p2) -> Double.compare(p1.getValue().getLocation().getX(), p2.getValue().getLocation().getX()));     
    sortedPoints = filterPoints(sortedPoints, xTolerance, yTolerance);    

    //Collections.sort(sortedPoints, (SimpleEntry<Integer, DataPoint> p1, SimpleEntry<Integer, DataPoint> p2) -> Double.compare(p1.getValue().getLocation().getY(), p2.getValue().getLocation().getY()));
    //sortedPoints = filterPoints(sortedPoints, xTolerance, yTolerance);
    
    Map<Integer, List<DataPoint>> result = new HashMap();
    for(Integer turtleIndex : data.keySet()) {
      result.put(turtleIndex, new ArrayList());
      //Show at least one point, so the legend shows the turtle color
      //if(data.get(turtleIndex).size() > 0)
        //result.get(turtleIndex).add(data.get(turtleIndex).get(0));
    }
    
    for(SimpleEntry<Integer, DataPoint> point : sortedPoints) {
      result.get(point.getKey()).add(point.getValue());
    }
    
    return result;
  }
  
  private List<SimpleEntry<Integer, DataPoint>> filterPoints(List<SimpleEntry<Integer, DataPoint>> points, double xTolerance, double yTolerance) {
    Console.log("X: " + xTolerance);
    Console.log("Y: " + yTolerance);
    
    int len = points.size();
    
    List<SimpleEntry<Integer, DataPoint>> newPoints = new ArrayList();
    
    if(len > 0) {
      SimpleEntry<Integer, DataPoint> point;
        
      for (int i = 0; i < len; i++) {
        point = points.get(i);
        
        if(point != null) {

          newPoints.add(point);
          double x1 = point.getValue().getLocation().getX();
          double y1 = point.getValue().getLocation().getY();
          
          int offset = 1;
          boolean pointHandled = false;
          
          while(!pointHandled && i + offset < points.size()) {
            SimpleEntry<Integer, DataPoint> neighbourPoint = points.get(i + offset);
            
            if(neighbourPoint != null) {
              
              if (getDistance(x1, neighbourPoint.getValue().getLocation().getX()) <= xTolerance) {
                double y2 = neighbourPoint.getValue().getLocation().getY();

                if (getDistance(y1, y2) <= yTolerance) {
                  //REMOVE THIS POINT!
                  newPoints.get(newPoints.size() - 1).getValue().addIndices(neighbourPoint.getValue().getIndices());
                  points.set(i + offset, null);
                }
              } else {
                pointHandled = true;
              }
            }

            offset++;
          }
        }
      }
    }
    return newPoints;
  }
  
  private double getDistance(double p1, double p2) {
    return Math.sqrt((p1 - p2) * (p1 - p2));
  }
}
