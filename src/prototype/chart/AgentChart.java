package prototype.chart;

import com.sun.javafx.charts.Legend;
import com.sun.javafx.charts.Legend.LegendItem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import prototype.listener.SelectionEventListener;
import prototype.object.Category;
import prototype.object.Parameter;
import prototype.object.Turtle;
import prototype.object.Type;
import prototype.object.Value;
import prototype.object.Filter;

public final class AgentChart extends BaseChart {
  private static final int MIN_WIDTH = 1;
    
  public AgentChart(Scene scene, int[] selectedTurtles, String yParameter, int yParameterIndex, String yParameterValue, List<Turtle> data, boolean liveUpdate) {
    super(scene, selectedTurtles, yParameter, yParameterIndex, yParameterValue, data,  liveUpdate);    
    initialize(false);
  }
  
  public AgentChart(Scene scene, List<Turtle> data, int selectionStart, int selectionEnd, boolean forward) {
    super(scene, data, selectionStart, selectionEnd, forward);
    
    if(this.parameterMap.getParametersOfType(Type.Categorical).size() > 0) {
    Parameter firstParameter = this.parameterMap.getParametersOfType(Type.Categorical).get(0);
      this.parameter = firstParameter.getName();
      if(firstParameter.getValues().size() > 0) {
        this.parameterValue = firstParameter.getValues().get(0).getName();
      }
    }
    
    this.rootPane = new BorderPane();
    this.rootPane.getStylesheets().add("prototype/plot.css");
    this.filterRectangles = new ArrayList();
    
    initialize(false);
  }
  
  @Override
  public String getName() {
    return "Turtle-chart";
  }
      
  @Override
  boolean isCategorical() {
    return true;
  }
  
  @Override
  List<XYChart.Series> getData() {
    return new ArrayList();
  }

  @Override
  void createChart(Collection datapoints) {
    ObservableList<String> categories = FXCollections.observableArrayList();
    for(int turtle : selectedTurtles) {
      categories.add(String.format("Turtle %d", turtle + 1));
    }
    CategoryAxis yAxis = new CategoryAxis(categories);
    
    NumberAxis xAxis = new NumberAxis();
    try {
        int timeframes = this.data.get(0).getTimeFrameCount();
        double scale = getScale(timeframes);
        if(zoomRange == null)
          xAxis = new NumberAxis(0, timeframes, scale);
        else
          xAxis = new NumberAxis(zoomRange.getMin(), zoomRange.getMax(), scale);
    } catch(Exception ex) { }
    
    this.XYChart = new ScatterChart<>(xAxis, yAxis);
    
    xAxis.widthProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) -> {
      Timer timer = new java.util.Timer();
      timer.schedule( 
        new java.util.TimerTask() {
          @Override
          public void run() {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                  resizeChart();
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
                  resizeChart();
                  timer.cancel();
                  timer.purge();
                }
              });
            }
          }, 0);
    });
    
    if(data.size() > 0) {
      xAxis.setLabel("Time");
      this.XYChart.getData().addAll(datapoints);
    }
    
    rootPane.getChildren().clear();
    rootPane.setCenter(this.XYChart);

      Timer timer = new java.util.Timer();
      timer.schedule( 
        new java.util.TimerTask() {
        @Override
        public void run() {
          Platform.runLater(new Runnable() {
              @Override
              public void run() {
                plotData();
                selectFrames(selectedStartIndex, selectedEndIndex, false, forward);
                timer.cancel();
                timer.purge();
              }
            });
          }
        }, 0);
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
    CategoryAxis yAxis = (CategoryAxis) this.XYChart.getYAxis();
    
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
        double xShift = getSceneXShift(xAxis);
        double yShift = 0;//getSceneYShift(yAxis);

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
        filterRectangle.setUserData(new Object[]{ "", "" });
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
        double xShift = getSceneXShift(xAxis);
        double yShift = 0;//getSceneYShift(yAxis);

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
        double xShift = getSceneXShift(xAxis);
        double yShift = 0;//getSceneYShift(yAxis);

        Rectangle selection = getSelectionRectangle(basePoint, event.getX(), event.getY(), xShift, yShift, xAxis.getWidth(), yAxis.getHeight());

        Rectangle filterRectangle = filterRectangles.get(selectedFilterIndex);
        
        if(selection.getHeight() != 0) {
          filterRectangle.setY(selection.getY() + yAxisShift);
          filterRectangle.setHeight(selection.getHeight());

          String minValue = yAxis.getValueForDisplay(selection.getY() + selection.getHeight());
          if(minValue == null)
            minValue = yAxis.getValueForDisplay(selection.getY() + selection.getHeight() - (getRowHeigt() / 2));
          String maxValue = yAxis.getValueForDisplay(selection.getY());
          if(maxValue == null)
            maxValue = yAxis.getValueForDisplay(selection.getY() + (getRowHeigt() / 2));

          if(minValue != null && maxValue != null) {
            filterRectangle.setUserData(new Object[]{ minValue, maxValue });

            List<String> filterTurtles = new ArrayList();
            boolean started = false;
            for(String category : yAxis.getCategories()) {
              if(category.equals(minValue))
                started = true;
              if(started)
                filterTurtles.add(category);
              if(category.equals(maxValue))
                started = false;
            }

            Filter filter = filter(filterTurtles);
            if(filter != null) {
              filterRectangle.setUserData(new Object[]{ minValue, maxValue, filter });
            } else {
              rootPane.getChildren().remove(filterRectangle);
              filterRectangles.remove(filterRectangle);
            }
          } else {
            rootPane.getChildren().remove(filterRectangle);
            filterRectangles.remove(filterRectangle);
          }
        }
      }      
    });
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
      CategoryAxis yAxis = (CategoryAxis) XYChart.getYAxis();
      NumberAxis xAxis = (NumberAxis) XYChart.getXAxis();

      double xAxisShift = getSceneXShift(xAxis);
      double yAxisShift = getSceneYShift(yAxis);

      double height = getRowHeigt();
      
      Parameter parameter = this.parameterMap.getParameter(this.parameter);
      Value value = parameter.getValue(parameterValue);
      
      for(int turtleIndex : selectedTurtles) {
        String categoryName = String.format("Turtle %d", turtleIndex + 1);
        double yPosition = yAxis.getDisplayPosition(categoryName);
        int timeframes = this.data.get(0).getTimeFrameCount();

        Rectangle categoryBlock = RectangleBuilder.create()
                .x(xAxisShift)
                .y(yPosition + yAxisShift - (height / 2))
                .height(height)
                .width(xAxis.getWidth())
                .userData(new Object[]{ categoryName, 0, 0, timeframes })
                .styleClass("default-category-background")
                .id("background")
                .build();
        this.rootPane.getChildren().add(categoryBlock);
        
        double currentCategory = -1;
        boolean currentVisibility = true;
        int currentFrame = 0;
        double currentPosition = xAxis.getDisplayPosition(0);
        
        Turtle turtle = data.get(turtleIndex);
        List<DataPoint> categoricalValues = turtle.getAllValues(this.parameter, parameterIndex, parameterValue);
        
        if(zoomRange != null)
          categoricalValues = categoricalValues.subList(Math.max(zoomRange.getMin(), 0), Math.min(zoomRange.getMax(), categoricalValues.size() - 1));
        
        for(int i = 0; i < categoricalValues.size(); i++) {
          DataPoint category = categoricalValues.get(i);
          
          boolean newBlock = false;
          double newCategory = currentCategory;
          
          if(category.isVisible()) {
            if(!currentVisibility || category.getLocation().getY() != currentCategory || i >= categoricalValues.size() - 1) {
              newBlock = true;
              newCategory = category.getLocation().getY();    
            }
            currentVisibility = true;
          } else {
            if(currentVisibility || i >= categoricalValues.size() - 1) {
              currentVisibility = false;
              newBlock = true;
              newCategory = -1;
            }
          }
          
          if(newBlock) {
            double xPosition = xAxis.getDisplayPosition(category.getTimeframe());

            int categoryIndex = value.getCategoryIndex((int)currentCategory);
            if(categoryIndex != -1) {
              Rectangle valueBlock = RectangleBuilder.create()
                      .x(currentPosition + xAxisShift)
                      .y(yPosition + yAxisShift - (height / 2))
                      .height(height)
                      .width(Math.max(xPosition - currentPosition, MIN_WIDTH))
                      .userData(new Object[]{ categoryName, currentFrame, category.getTimeframe() })
                        .styleClass(String.format("default-category-color%d", categoryIndex))
                      .build();
              this.rootPane.getChildren().add(valueBlock);
            }

            currentPosition = xPosition;
            currentFrame = category.getTimeframe();
            currentCategory = newCategory;
          }
        }
      }

      for(Node child : this.rootPane.getChildren()) {
        if(child.getClass() == Rectangle.class) {
          createRectangleSelectionEvents(child, rootPane, xAxis, yAxis);
        }
      }
       
      createLegend();
    }
  }
    
  private void createLegend() {
      Parameter parameter = this.parameterMap.getParameter(this.parameter);
      Value value = parameter.getValue(parameterValue);
      List<Category> categories = value.getCategories();
      
      Legend legend = (Legend)XYChart.lookup(".chart-legend");
      legend.getStylesheets().add("prototype/plot.css");
          
      List<LegendItem> items = new ArrayList();
      
      for(int index = 0; index < categories.size(); index++) {
          Rectangle legendPoint = RectangleBuilder.create()
                  .height(10)
                  .width(10)
                  .build();
        
        LegendItem item = new Legend.LegendItem(categories.get(index).getName(), legendPoint);
        item.getSymbol().getStyleClass().add(String.format("default-category-color%d", index));
        
        items.add(item);
      }
      
      legend.getItems().setAll(items);
  }
  
  private Filter filter(List<String> filterTurtles) {
    if(filterTurtles.size() > 0) {
      Parameter selectedParameter = this.parameterMap.getParameter(this.parameter);
      Value value = selectedParameter.getValue(parameterValue);

      List<Integer> turtles = new ArrayList();
      for(String filterTurtle : filterTurtles) {
        turtles.add(Integer.parseInt(filterTurtle.replaceAll("[\\D]", "")) - 1);
      }

      Filter filter = new Filter(this, this.parameter, this.parameterIndex, this.parameterValue, turtles, value.getCategoryValues());

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
  
  private void resizeChart() {    
    CategoryAxis yAxis = (CategoryAxis) XYChart.getYAxis();
    NumberAxis xAxis = (NumberAxis) XYChart.getXAxis();

    double xAxisShift = getSceneXShift(xAxis);
    double yAxisShift = getSceneYShift(yAxis);
    
    double height = getRowHeigt();
    double offset = height * 0.05;
    double turtleHeight = (height - (offset * 2));

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

        rectChild.setX(startPosition + xAxisShift);
        rectChild.setWidth(Math.max(endPosition - startPosition, MIN_WIDTH));
        rectChild.setY(yPosition + yAxisShift - (height / 2) + offset);
        rectChild.setHeight(turtleHeight);
      } else {
        if(child.getClass() == Rectangle.class && "background".equals(child.getId())) {
          Rectangle rectChild = (Rectangle)child;

          Object[] userData = (Object[])rectChild.getUserData();
          String category = (String)userData[0];
          int index = (int)userData[1];
          int startFrame = (int)userData[2];
          int endFrame = (int)userData[3];

          if(zoomRange != null) {
            startFrame = zoomRange.getMin();
            endFrame = zoomRange.getMax();
          }
          
          double startPosition = xAxis.getDisplayPosition(startFrame);
          double endPosition = xAxis.getDisplayPosition(endFrame);
          double yPosition = yAxis.getDisplayPosition(category);

          rectChild.setX(startPosition + xAxisShift);
          rectChild.setWidth(endPosition - startPosition);
          rectChild.setY(yPosition + yAxisShift - (height / 2));
          rectChild.setHeight(height);
        }
      }
    }

    if(selectionRectangle != null) {
      Object[] userData = (Object[])selectionRectangle.getUserData();
      int startFrame = (int)userData[0];
      int endFrame = (int)userData[1];

      double start = xAxis.getDisplayPosition(startFrame);
      double end = xAxis.getDisplayPosition(endFrame);

      if(start == end)
        end +=1;
      
      selectionRectangle.setX(xAxisShift + start);
      selectionRectangle.setWidth(end - start);

      selectionRectangle.setY(yAxisShift + 5);
      selectionRectangle.setHeight(yAxis.getHeight() - 10);
      
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
      String minValue = (String)userData[0];
      String maxValue = (String)userData[1];

      double start = yAxis.getDisplayPosition(maxValue);
      double end = yAxis.getDisplayPosition(minValue);

      filterRectangle.setY(yAxisShift + start - (height / 2));
      filterRectangle.setHeight(end - start + height);

      filterRectangle.setX(xAxisShift);
      filterRectangle.setWidth(xAxis.getWidth());
    }
  }
}
