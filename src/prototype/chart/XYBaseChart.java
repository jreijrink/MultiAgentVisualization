package prototype.chart;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
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
import prototype.object.ParameterMap;
import prototype.listener.SelectionEventListener;
import prototype.object.StringValuePair;
import static prototype.chart.Chart.getCheckbox;
import static prototype.chart.Chart.getScale;
import static prototype.chart.Chart.getTurtleListView;
import prototype.object.Parameter;
import prototype.object.Turtle;
import prototype.object.Value;
import org.dockfx.DockNode;
import static prototype.chart.Chart.getAllTurtles;
import prototype.object.Filter;
import prototype.object.Range;

public class XYBaseChart implements Chart {  
  public enum ChartType { Scatter, Line };
  
  private Scene scene;
  private List<Turtle> data;
  private ChartType type;
  private List<SelectionEventListener> listenerList = new ArrayList();
  
  public int[] selectedTurtles;
  public String parameter;
  public int parameterIndex;
  public String parameterValue;
  public boolean liveUpdate;
  
  private DockNode dockNode;
  
  private ParameterMap parameterMap;
  
  private BorderPane rootPane; 
  private XYChart<Number,Number> XYChart;
  
  private Point2D selectionPoint;
  private Rectangle selectionRectangle;
  private Rectangle selectionFrame;
  private Point2D basePoint;
  private List<Rectangle> filterRectangles;
  private int selectedFilterIndex;
  private boolean newFilter;
  
  private int selectedStartIndex;
  private int selectedEndIndex;
  private boolean forward;
    
  private Timer resizeTimer;
  private TimerTask resizeTask;
  private int visibleDataPoints;
  
  public XYBaseChart(Scene scene, ChartType type, int[] selectedTurtles, String yParameter, int yParameterIndex, String yParameterValue, List<Turtle> data,  boolean liveUpdate) {
    this.scene = scene;
    this.type = type;
    this.parameterMap = new ParameterMap();
    this.selectedTurtles = selectedTurtles;
    this.parameter = yParameter;
    this.parameterIndex = yParameterIndex;
    this.parameterValue = yParameterValue;
    this.data = data;
    this.liveUpdate = liveUpdate;
    
    this.rootPane = new BorderPane();
    this.rootPane.getStylesheets().add("prototype/plot.css");
    this.visibleDataPoints = 0;
    this.filterRectangles = new ArrayList();
    
    initRootPaneListeners();
        
    initialize(false);
  }
  
  public XYBaseChart(Scene scene, ChartType type, List<Turtle> data, int selectionStart, int selectionEnd, boolean forward) {
    this.scene = scene;
    this.type = type;
    this.data = data;
    this.parameterMap = new ParameterMap();
    this.selectedTurtles = new int[]{ 0 };    
    this.parameterIndex = 0;
    this.liveUpdate = true;
    
    this.selectedStartIndex = selectionStart;    
    this.selectedEndIndex = selectionEnd;
    this.forward = forward;
    
    if(this.parameterMap.GetAllParameters().size() > 0) {
    Parameter firstParameter = this.parameterMap.GetAllParameters().get(0);
      this.parameter = firstParameter.getName();
      if(firstParameter.getValues().size() > 0) {
        this.parameterValue = firstParameter.getValues().get(0).getName();
      }
    }
    
    this.rootPane = new BorderPane();
    this.rootPane.getStylesheets().add("prototype/plot.css");
    this.visibleDataPoints = 0;
    this.filterRectangles = new ArrayList();

    
    initRootPaneListeners();
    
    initialize(false);
  }
  
  @Override
  public Node getNode() {
    return rootPane;
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
  
  @Override
  public Chart getCopy() {
    return new XYBaseChart(scene, type, selectedTurtles, parameter, parameterIndex,parameterValue, data,  liveUpdate);
  }
  
  @Override
  public void addSelectionEventListener(SelectionEventListener listener) {
    listenerList.add(listener);
  }
  
  @Override
  public void updateData(List<Turtle> data) {
    clearFilter();
    this.data = data;
    initialize(false);
  }
  
  @Override
  public void selectFrames(int startIndex, int endIndex, boolean drag, boolean forward) {    
    if((!drag || liveUpdate) && data.size() > 0 && XYChart != null) {
      
      selectedStartIndex = startIndex;
      selectedEndIndex = endIndex;
      this.forward = forward;
      
      clearSelection();
      
      NumberAxis xAxis = (NumberAxis) this.XYChart.getXAxis();
      double xAxisShift = getSceneXShift(xAxis);
      double start = xAxis.getDisplayPosition(startIndex);
      double end = xAxis.getDisplayPosition(endIndex);
      
      if(start == end)
        end +=1;
      
      if(selectionRectangle != null) {
        selectionRectangle.setX(xAxisShift + start);
        selectionRectangle.setWidth(end - start);
        selectionRectangle.setUserData(new Object[]{ startIndex, endIndex });
      }
      
      if(selectionFrame != null) {
        if(forward)
          selectionFrame.setX(xAxisShift + end);
        else
          selectionFrame.setX(xAxisShift + start);
        selectionFrame.setWidth(2);
        selectionFrame.setUserData(forward);
      }
      
      setDockTitle();
    }
  }
  
  @Override
  public void update() {
    initialize(false);
  }
  
  @Override
  public void clearFilter() {
    boolean hadFilters = false;
    for(Turtle turtle : data) {
      hadFilters = turtle.removeFilters(this) || hadFilters;
    }
    
    for(Rectangle filter : filterRectangles) {
      this.rootPane.getChildren().remove(filter);
    }
    filterRectangles.clear();

    if(hadFilters) {
      for(SelectionEventListener listener : listenerList) {
        listener.update();
      }
    }
  }
  
  @Override
  public void setDockNode(DockNode dockNode) {
    this.dockNode = dockNode;
    setDockTitle();
  }
  
  private void setDockTitle() {
    if(this.dockNode != null) {
      this.dockNode.setTitle(String.format("%s - %s[%d] (%s) [%d - %d]", getName(), this.parameter, this.parameterIndex, this.parameterValue, this.selectedStartIndex, this.selectedEndIndex));
    }
  }
  
  @Override
  public void showParameterDialog() {
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(10, 10, 10, 10));
    
    Dialog<Boolean> dialog = new Dialog();
    dialog.setTitle("Chart options");
    dialog.setHeaderText("Choose chart options");
    dialog.setContentText("Choose chart options:");
    
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    CheckBox liveCheckbox = getCheckbox("Live update", liveUpdate);
    
    ListView listView = getTurtleListView(selectedTurtles); 
    
    ChoiceBox<String> parameterChoiceBox = new ChoiceBox();
    ChoiceBox<Integer> indexChoiceBox = new ChoiceBox();
    ChoiceBox<String> valueChoiceBox = new ChoiceBox();

    List<Parameter> choices = parameterMap.GetAllParameters();
    ObservableList<String> options = FXCollections.observableArrayList();
    for(Parameter choise : choices) {
      options.add(choise.getName());
    }
    options = options.sorted();

    parameterChoiceBox.setItems(options);
    if(options.contains(parameter)) {
      parameterChoiceBox.getSelectionModel().select(parameter);
      Parameter parameter = parameterMap.GetParameter(this.parameter);
      
      int count = parameter.getCount();
      ObservableList<Integer> indexOptions = FXCollections.observableArrayList();
      for(int index = 0; index < count; index++) {
        indexOptions.add(index);
      }      
      indexChoiceBox.setItems(indexOptions);
      if(indexOptions.contains(parameterIndex)) {
        indexChoiceBox.getSelectionModel().select(parameterIndex);
      }
      
      List<Value> values = parameter.getValues();
      ObservableList<String> valueOptions = FXCollections.observableArrayList();
      for(Value value : values) {
        valueOptions.add(value.getName());
      }
      valueChoiceBox.setItems(valueOptions);
      if(valueOptions.contains(parameterValue)) {
        valueChoiceBox.getSelectionModel().select(parameterValue);
      }
    }

    parameterChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        Parameter parameter = parameterMap.GetParameter(newValue.toString());
        
        int count = parameter.getCount();
        ObservableList<Integer> indexOptions = FXCollections.observableArrayList();
        for(int index = 0; index < count; index++) {
          indexOptions.add(index);
        }      
        indexChoiceBox.setItems(indexOptions);
        indexChoiceBox.getSelectionModel().select(0);

        List<Value> values = parameter.getValues();
        ObservableList<String> valueOptions = FXCollections.observableArrayList();
        for(Value value : values) {
          valueOptions.add(value.getName());
        }
        valueChoiceBox.setItems(valueOptions);
        valueChoiceBox.getSelectionModel().select(0);
      }
    });
    
    grid.add(liveCheckbox, 0, 0);
    
    grid.add(new Label("Parameter:"), 0, 1);
    grid.add(parameterChoiceBox, 0, 2);
    grid.add(new Label("Index:"), 0, 3);
    grid.add(indexChoiceBox, 0, 4);
    grid.add(new Label("Value:"), 0, 5);
    grid.add(valueChoiceBox, 0, 6);

    grid.add(new Label("Turtles"), 0, 7);
    grid.add(listView, 0, 8);
    
    dialog.getDialogPane().setContent(grid);
    listView.setPrefHeight(200);
        
    dialog.setResultConverter(dialogButton -> {
      if(dialogButton == ButtonType.OK) {
        ObservableList<StringValuePair<String, Integer>> selectedItems = listView.getSelectionModel().getSelectedItems();
        selectedTurtles = new int[selectedItems.size()];
        for(int i = 0; i < selectedItems.size(); i++) {
          selectedTurtles[i] = selectedItems.get(i).getValue();
        }
        
        if(parameterChoiceBox.getSelectionModel().getSelectedItem() != null) {
          clearFilter();
          
          parameter = parameterChoiceBox.getSelectionModel().getSelectedItem();
          parameterIndex = indexChoiceBox.getSelectionModel().getSelectedItem();
          parameterValue = valueChoiceBox.getSelectionModel().getSelectedItem();
        }
        
        liveUpdate = liveCheckbox.isSelected();

        setDockTitle();

        return true;
      }
      return null;
    });

    Optional<Boolean> result = dialog.showAndWait();

    if(result.isPresent() && result.get() != null) {
      initialize(false);
    }
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
  
  private void notifyListeners(int startIndex, int endIndex, boolean drag, boolean forward) {
    for(SelectionEventListener listener : listenerList) {
      listener.timeFrameSelected(startIndex, endIndex, drag, forward);
    }
  }
  
  private void clearSelection() {    
    if(XYChart != null) {
      NumberAxis yAxis = (NumberAxis) XYChart.getYAxis();
      double yAxisShift = getSceneYShift(yAxis);

      if(selectionRectangle != null) {
        selectionRectangle.setX(0);
        selectionRectangle.setY(yAxisShift);
        selectionRectangle.setWidth(0);
        selectionRectangle.setHeight(yAxis.getHeight());
      }
      
      if(selectionFrame != null) {
        selectionFrame.setX(0);
        selectionFrame.setWidth(0);
        selectionFrame.setY(yAxisShift);
        selectionFrame.setHeight(yAxis.getHeight());      
      }
    }
  }
  
  private void initialize(boolean resize) {
    setCursor(Cursor.WAIT);
    
    this.parameterMap = new ParameterMap();
    (new Thread() {
      public void run() {
        List<XYChart.Series> datapoints = getData();
        if(!resize || dataIncreaseAchieved(datapoints)) {
          visibleDataPoints = getDataSize(datapoints);
          createChart(datapoints);
          Platform.runLater(()-> setMouseListeners());    
          Platform.runLater(()-> selectFrames(selectedStartIndex, selectedEndIndex, false, forward));
          //selectFrames(selectedStartIndex, selectedEndIndex, false, forward);
        }
        Platform.runLater(()->  setCursor(Cursor.DEFAULT));
      }
    }).start();
  }
  
  private void setCursor(Cursor cursor) {
    scene.setCursor(cursor);
    rootPane.setCursor(cursor);    
  }
  
  private boolean dataIncreaseAchieved(List<XYChart.Series> data) {
    int newSize = getDataSize(data);
    double difference = Math.abs(newSize - visibleDataPoints);
    double threshold = ((double)visibleDataPoints) * 0.2;
    
    return difference > threshold; 
  }
  
  private int getDataSize(List<XYChart.Series> data) {
    int totalSize = 0;
    for(XYChart.Series series : data) {
      totalSize += series.getData().size();
    }    
    return totalSize;
  }
  
  private void createChart(Collection datapoints) {
    
    NumberAxis xAxis = new NumberAxis();
    try {
        int timeframes = this.data.get(0).getTimeFrameCount();
        double scale = getScale(timeframes);
        xAxis = new NumberAxis(0, timeframes, scale);
    } catch(Exception ex) { }
    
    NumberAxis yAxis = new NumberAxis();
    try {
      double min = Double.MAX_VALUE;
      double max = Double.MIN_VALUE;
      for(Turtle turlte : data) {
        for(DataPoint value : turlte.GetAllValues(parameter, parameterIndex, parameterValue)) {
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
  
  private void setMouseListeners() {

    //XYChart<Number,Number> XYChart = (XYChart<Number,Number>)rootPane.getCenter();
    
    NumberAxis xAxis = (NumberAxis) this.XYChart.getXAxis();
    NumberAxis yAxis = (NumberAxis) this.XYChart.getYAxis();

    for(Series series : XYChart.getData()) {
      for (Node n : series.getChart().getChildrenUnmodifiable()) {
        
        if(n.getStyleClass().contains("chart-content")) {          

          final Pane chart = (Pane) n;
          ObservableList<Node> children = chart.getChildren();
          
          for(Node child : children) {
            if(child.getStyleClass().contains("chart-plot-background")) {
              createRectangleSelectionEvents(child, null, xAxis, yAxis);
            }
            if(child.getClass().getName().equals("javafx.scene.chart.XYChart$1")) {
              createRectangleSelectionEvents(child, chart, xAxis, yAxis);
            }
          }
        }
      }
    }

    if(selectionRectangle == null) {
      selectionRectangle = RectangleBuilder.create()
              .x(0)
              .y(0)
              .height(yAxis.getHeight())
              .width(0)
              .fill(Color.web("0x222222"))
              .opacity(0.2)
              .id("selection")
              .build();
      selectionRectangle.setUserData(new Object[]{ -1, -1 });
    }
    
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
      if(!rootPane.getChildren().contains(selectionRectangle))
        rootPane.getChildren().add(selectionRectangle);
        createRectangleSelectionEvents(selectionRectangle, rootPane, xAxis, yAxis);
      }
    });

    if(selectionFrame == null) {
      selectionFrame = RectangleBuilder.create()
              .x(0)
              .y(0)
              .height(yAxis.getHeight())
              .width(0)
              .fill(Color.web("0x222222"))
              .opacity(0.4)
              .id("selection")
              .build();
      selectionFrame.setUserData(forward);
    }
    
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
      if(!rootPane.getChildren().contains(selectionFrame))
        rootPane.getChildren().add(selectionFrame);
        createRectangleSelectionEvents(selectionFrame, rootPane, xAxis, yAxis);
      }
    });
    
    createAxisFilter();
  }
  
  private void createAxisFilter() {
    
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
  
  private void createRectangleSelectionEvents(Node node, Node parent, NumberAxis xAxis, NumberAxis yAxis) {    
    node.setOnMousePressed((MouseEvent event) -> {
      double xShift = getXShift(node, parent, xAxis);
      double yShift = getYShift(node, parent, yAxis);
      
      selectionPoint = new Point2D(event.getX(), event.getY());
      
      Rectangle selection = getSelectionRectangle(selectionPoint, event.getX(), event.getY(), xShift, yShift, xAxis.getWidth(), xAxis.getHeight());
            
      int start = xAxis.getValueForDisplay(selection.getX()).intValue();
      int end = xAxis.getValueForDisplay(selection.getX() + selection.getWidth()).intValue();
      
      notifyListeners(start, end, false, true);
    });

    node.setOnMouseDragged((MouseEvent event) -> {
      double xShift = getXShift(node, parent, xAxis);
      double yShift = getYShift(node, parent, yAxis);
      
      Rectangle selection = getSelectionRectangle(selectionPoint, event.getX(), event.getY(), xShift, yShift, xAxis.getWidth(), xAxis.getHeight());
            
      int start = xAxis.getValueForDisplay(selection.getX()).intValue();
      int end = xAxis.getValueForDisplay(selection.getX() + selection.getWidth()).intValue();
      
      boolean forward = true;
      if(selectionPoint.getX() == (selection.getX() + selection.getWidth() + xShift)) {
        //Backward selection
        forward = false;
      }
      
      notifyListeners(start, end, true, forward);
      
      //selectFrames(start, end, false, forward);      
    });
    
    node.setOnMouseReleased((MouseEvent event) -> {
      double xShift = getXShift(node, parent, xAxis);
      double yShift = getYShift(node, parent, yAxis);

      Rectangle selection = getSelectionRectangle(selectionPoint, event.getX(), event.getY(), xShift, yShift, xAxis.getWidth(), xAxis.getHeight());
      
      int start = xAxis.getValueForDisplay(selection.getX()).intValue();
      int end = xAxis.getValueForDisplay(selection.getX() + selection.getWidth()).intValue();
      
      boolean forward = true;
      if(selectionPoint.getX() == (selection.getX() + selection.getWidth() + xShift)) {
        //Backward selection
        forward = false;
      }
      
      notifyListeners(start, end, false, forward);
    });
  }
  
  private Rectangle getSelectionRectangle(Point2D start, double mouseX, double mouseY, double xShift, double yShift, double chartWidth, double chartHeight) {
    
    mouseX = (mouseX - xShift);
    mouseY = (mouseY - yShift);
    
    //Bound the rectangle to be only within the chart
    mouseX = Math.max(mouseX, 0);
    mouseX = Math.min(mouseX, chartWidth);
    
    mouseY = Math.max(mouseY, 0);
    mouseY = Math.min(mouseY, chartHeight);
    
    double startX = start.getX() - xShift;
    double endX = mouseX;
    double width = Math.abs(endX - startX);
    
    double startY = start.getY() - yShift;
    double endY = mouseY;
    double height = Math.abs(endY - startY);
    
    return new Rectangle(Math.min(startX, endX), Math.min(startY, endY), width, height);
  }
    
  private List<XYChart.Series> getData() { 
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
        List<DataPoint> values = turtle.GetAllValues(parameter, parameterIndex, parameterValue);
        timeframes = values.size();
        before += values.size();
        datapoints.get(i).addAll(values);

        for(DataPoint value : values) {
          minValue = Math.min(value.getValue(), minValue);
          maxValue = Math.max(value.getValue(), maxValue);
        }
      }

      double xTolerance = (timeframes / rootPane.getWidth()) * 3.0f;
      double yTolerance = ((maxValue - minValue) / rootPane.getHeight()) * 3.0f;
      
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

        ObservableList<XYChart.Data> data = series.getData();
        data.addAll(elements);

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
        List<DataPoint> data = turtle.GetAllValues(parameter, parameterIndex, parameterValue);

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

    Collections.sort(sortedPoints, (SimpleEntry<Integer, DataPoint> p1, SimpleEntry<Integer, DataPoint> p2) -> Double.compare(p1.getValue().getLocation().getY(), p2.getValue().getLocation().getY()));
    sortedPoints = filterPoints(sortedPoints, xTolerance, yTolerance);
    
    Map<Integer, List<DataPoint>> result = new HashMap();
    for(Integer turtleIndex : data.keySet()) {
      result.put(turtleIndex, new ArrayList());
      //Show at least one point, so the legend shows the turtle color
      if(data.get(turtleIndex).size() > 0)
        result.get(turtleIndex).add(data.get(turtleIndex).get(0));
    }
       
    for(SimpleEntry<Integer, DataPoint> point : sortedPoints) {
      result.get(point.getKey()).add(point.getValue());
    }
    
    return result;
  }
  
  private List<SimpleEntry<Integer, DataPoint>> filterPoints(List<SimpleEntry<Integer, DataPoint>> points, double xTolerance, double yTolerance) {
    int len = points.size();
    
    List<SimpleEntry<Integer, DataPoint>> newPoints = new ArrayList();
    
    if(len > 0) {
      SimpleEntry<Integer, DataPoint> point;
      SimpleEntry<Integer, DataPoint> prevPoint = points.get(0);

      newPoints.add(prevPoint);

      for (int i = 1; i < len; i++) {
        point = points.get(i);

        if (getDistance(point.getValue().getLocation().getX(), prevPoint.getValue().getLocation().getX()) > xTolerance || getDistance(point.getValue().getLocation().getY(), prevPoint.getValue().getLocation().getY()) > yTolerance) {
            newPoints.add(point);
            prevPoint = point;
        } else {
          newPoints.get(newPoints.size() - 1).getValue().addIndices(point.getValue().getIndices());
        }
      }
    }
    return newPoints;
  }
  
  private double getDistance(double p1, double p2) {
    return Math.sqrt((p1 - p2) * (p1 - p2));
  }

  private double getXShift(Node node, Node parent, Node axis) {
    double xChartShift = getSceneXShift(node);
    double xAxisShift = getSceneXShift(axis);
    double xParentShift = parent != null ? getSceneXShift(parent) : 0;
    double xShift = xAxisShift - (xChartShift - xParentShift);
    return xShift;
  }
  
  private double getYShift(Node node, Node parent, Node axis) {
    double yChartShift = getSceneYShift(node);
    double yAxisShift = getSceneYShift(axis);
    double yParentShift = parent != null ? getSceneYShift(parent) : 0;
    double yShift = yAxisShift - (yChartShift - yParentShift);
    return yShift;
  }
  
  private double getSceneXShift(Node node) { 
    double shift = 0; 
    do {  
        shift += node.getLayoutX();  
        node = node.getParent(); 
    } while (node != null && (node.getClass() != LineChart.class && node.getClass() != ScatterChart.class));
    return shift; 
  }
  
  private double getSceneYShift(Node node) { 
    double shift = 0; 
    do {  
        shift += node.getLayoutY();
        node = node.getParent(); 
    } while (node != null && (node.getClass() != LineChart.class && node.getClass() != ScatterChart.class));
    return shift; 
  }
}
