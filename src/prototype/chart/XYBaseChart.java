package prototype.chart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
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
import prototype.settings.Configuration;
import org.dockfx.DockNode;

public class XYBaseChart implements Chart {  
  public enum ChartType { Scatter, Line };
  
  private Scene scene;
  private List<Turtle> data;
  private ChartType type;
  
  public int[] selectedTurtles;
  public String parameter;
  public int parameterIndex;
  public String parameterValue;
  public boolean liveUpdate;
  
  private DockNode dockNode;
  
  private ParameterMap parameterMap;
  
  private BorderPane rootPane; 
  private Point2D selectionPoint;
  private Rectangle selectionRectangle;
  private XYChart<Number,Number> XYChart;
  
  private int selectedStartIndex;
  private int selectedEndIndex;
  
  public XYBaseChart(Scene scene, ChartType type, int[] selectedTurtles, String yParameter, int yParameterIndex, String yParameterValue, List<Turtle> data,  boolean liveUpdate) {
    this.scene = scene;
    this.type = type;
    this.selectedTurtles = selectedTurtles;
    this.parameter = yParameter;
    this.parameterIndex = yParameterIndex;
    this.parameterValue = yParameterValue;
    this.data = data;
    this.liveUpdate = liveUpdate;
    this.rootPane = new BorderPane();
    this.rootPane.getStylesheets().add("prototype/plot.css");
    this.parameterMap = new ParameterMap();
    
    initialize();
  }
  
  public XYBaseChart(Scene scene, ChartType type) {
    this.scene = scene;
    this.type = type;
    this.parameterMap = new ParameterMap();
    this.selectedTurtles = new int[]{ 0 };    
    this.parameterIndex = 0;
    this.data = new ArrayList();
    this.liveUpdate = true;
    
    if(this.parameterMap.GetParameters().size() > 0) {
    Parameter firstParameter = this.parameterMap.GetParameters().get(0);
      this.parameter = firstParameter.getName();
      if(firstParameter.getValues().size() > 0) {
        this.parameterValue = firstParameter.getValues().get(0).getName();
      }
    }
    
    this.rootPane = new BorderPane();
    this.rootPane.getStylesheets().add("prototype/plot.css");
    
    initialize();
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
  public void addSelectionEventListener(SelectionEventListener listener) {
    listenerList.add(SelectionEventListener.class, listener);
  }
  
  @Override
  public void updateData(List<Turtle> data) {
    this.data = data;
    initialize();
  }
  
  @Override
  public void selectFrames(int startIndex, int endIndex, boolean drag) {    
    if((!drag || liveUpdate) && data.size() > 0) {
      
      selectedStartIndex = startIndex;
      selectedEndIndex = endIndex;
      
      clearSelection();
      
      NumberAxis xAxis = (NumberAxis) this.XYChart.getXAxis();
      double xAxisShift = getSceneXShift(xAxis);
      double start = xAxis.getDisplayPosition(startIndex);
      double end = xAxis.getDisplayPosition(endIndex);
      
      if(selectionRectangle != null) {
        selectionRectangle.setX(xAxisShift + start);
        selectionRectangle.setWidth(end - start);
        selectionRectangle.setUserData(new Object[]{ startIndex, endIndex });
      }
      
      setDockTitle();
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

    List<Parameter> choices = parameterMap.GetParameters();
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
      initialize();
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
  
  private void clearSelection() {    
    NumberAxis yAxis = (NumberAxis) XYChart.getYAxis();
    double yAxisShift = getSceneYShift(yAxis);
  
    selectionRectangle.setX(0);
    selectionRectangle.setY(yAxisShift);
    selectionRectangle.setWidth(0);
    selectionRectangle.setHeight(yAxis.getHeight());
  }
  
  private void initialize() {
    this.parameterMap = new ParameterMap();
    createChart();
    setMouseListeners();
    selectFrames(selectedStartIndex, selectedEndIndex, false);
  }
  
  private void createChart() {    
    NumberAxis xAxis = new NumberAxis();
    try {
        int timeframes = this.data.get(0).getTimeFrameCount();
        double scale = getScale(timeframes);
        xAxis = new NumberAxis(0, timeframes, scale);
    } catch(Exception ex) { }
    
    NumberAxis yAxis = new NumberAxis();
            
    switch(type) {
      case Scatter:
        this.XYChart = new ScatterChart<>(xAxis,yAxis);
        break;
      case Line:
        this.XYChart = new LineChart<>(xAxis,yAxis);
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
    
    rootPane.getChildren().clear();
    
    rootPane.setCenter(this.XYChart);
    
    //if(data.size() > 0) {

    xAxis.setLabel("Time");
    yAxis.setLabel(this.parameter + " [" + parameterIndex + "] " + " (" + parameterValue + ")");

    Collection data = getData();

    this.XYChart.getData().setAll(data);
    //}
    
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
    
    double height = yAxis.getHeight();

    double minY = Double.MAX_VALUE;
    double maxY = Double.MIN_VALUE;  

    if(selectionRectangle != null) {
      Object[] userData = (Object[])selectionRectangle.getUserData();
      int startFrame = (int)userData[0];
      int endFrame = (int)userData[1];

      double start = xAxis.getDisplayPosition(startFrame);
      double end = xAxis.getDisplayPosition(endFrame);

      selectionRectangle.setX(xAxisShift + start);
      selectionRectangle.setWidth(end - start);

      selectionRectangle.setY(yAxisShift);
      selectionRectangle.setHeight(yAxis.getHeight());
    }
    
    drawInvalidData();
  }
  
  private void setMouseListeners() {

    XYChart<Number,Number> XYChart = (XYChart<Number,Number>)rootPane.getCenter();
    
    NumberAxis xAxis = (NumberAxis) XYChart.getXAxis();
    NumberAxis yAxis = (NumberAxis) XYChart.getYAxis();

    for(Series series : XYChart.getData()) {
      for (Node n : series.getChart().getChildrenUnmodifiable()) {
        
        if(n.getStyleClass().contains("chart-content")) {          

          final Pane chart = (Pane) n;
          ObservableList<Node> children = chart.getChildren();
          
          for(Node child : children) {
            if(child.getStyleClass().contains("chart-plot-background") || child.getClass().getName().equals("javafx.scene.chart.XYChart$1")) {
              createRectangleSelectionEvents(child, xAxis, yAxis);
            }
          }
        }
      }
    }

    selectionRectangle = RectangleBuilder.create()
            .x(0)
            .y(0)
            .height(yAxis.getHeight())
            .width(0)
            .fill(Color.web("0x222222"))
            .opacity(0.3)
            .id("selection")
            .build();
    selectionRectangle.setUserData(new Object[]{ -1, -1 });
    rootPane.getChildren().add(selectionRectangle);

    for(Node child : this.rootPane.getChildren()) {
      if(child.getClass() == Rectangle.class) {
        createRectangleSelectionEvents(child, xAxis, yAxis);
      }
    }
  }
  
  private void createRectangleSelectionEvents(Node node, NumberAxis xAxis, NumberAxis yAxis) {    
    node.setOnMousePressed((MouseEvent event) -> {
      double xChartShift = getSceneXShift(node);
      double yChartShift = getSceneYShift(node);
      double xAxisShift = getSceneXShift(xAxis);
      double yAxisShift = getSceneYShift(yAxis);

      selectionPoint = new Point2D(event.getX() + xChartShift, event.getY() + yChartShift);
      
      Rectangle selection = getSelectionRectangle(event.getX(), event.getY(), xChartShift, yChartShift, xAxis.getWidth() + xAxisShift - xChartShift, yAxis.getHeight() + yAxisShift- yChartShift);
            
      int start = xAxis.getValueForDisplay(selection.getX() - xAxisShift).intValue();
      int end = xAxis.getValueForDisplay(selection.getX() + selection.getWidth() - xAxisShift).intValue();
      
      notifyListeners(start, end, false);
    });

    node.setOnMouseDragged((MouseEvent event) -> {
      double xChartShift = getSceneXShift(node);
      double yChartShift = getSceneYShift(node);
      double xAxisShift = getSceneXShift(xAxis);
      double yAxisShift = getSceneYShift(yAxis);

      Rectangle selection = getSelectionRectangle(event.getX(), event.getY(), xChartShift, yChartShift, xAxis.getWidth() + xAxisShift - xChartShift, yAxis.getHeight() + yAxisShift- yChartShift);
            
      int start = xAxis.getValueForDisplay(selection.getX() - xAxisShift).intValue();
      int end = xAxis.getValueForDisplay(selection.getX() + selection.getWidth() - xAxisShift).intValue();
      
      notifyListeners(start, end, true);
      
      selectionRectangle.setX(selection.getX());
      selectionRectangle.setWidth(selection.getWidth());
    });
    
    node.setOnMouseReleased((MouseEvent event) -> {
      double xChartShift = getSceneXShift(node);
      double yChartShift = getSceneYShift(node);
      double xAxisShift = getSceneXShift(xAxis);
      double yAxisShift = getSceneYShift(yAxis);

      Rectangle selection = getSelectionRectangle(event.getX(), event.getY(), xChartShift, yChartShift, xAxis.getWidth() + xAxisShift - xChartShift, yAxis.getHeight() + yAxisShift- yChartShift);
      
      int start = xAxis.getValueForDisplay(selection.getX() - xAxisShift).intValue();
      int end = xAxis.getValueForDisplay(selection.getX() + selection.getWidth() - xAxisShift).intValue();
      
      notifyListeners(start, end, false);
      
      //selectionRectangle.setX(0);
      //selectionRectangle.setWidth(0);
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
    
  private Collection getData() { 
    List<XYChart.Series> seriesList = new ArrayList<>();

    if(data.size() > 0) {
      int turtles = data.size();
      for(int turtle =  0; turtle < turtles; turtle++) {
        boolean showData = (new ArrayList<Integer>() {{ for (int i : selectedTurtles) add(i); }}).contains(turtle);      
        seriesList.add(getSeries(turtle, showData));
      }
    }

    return seriesList;
  }
  
  private XYChart.Series getSeries(int turtleIndex, boolean showData) {

      XYChart.Series series = new XYChart.Series();
      series.setName(String.format("Turtle %d", turtleIndex + 1));
    
      List<XYChart.Data> elements = new ArrayList();
            
      if(showData) {
        try {
          Turtle turtle = data.get(turtleIndex);
          List<DataPoint> values = turtle.GetAllValues(parameter, parameterIndex, parameterValue);

          double minValue = Double.MAX_VALUE;
          double maxValue = Double.MIN_VALUE;
          
          for(DataPoint value : values) {
            minValue = Math.min(value.getValue(), minValue);
            maxValue = Math.max(value.getValue(), maxValue);
          }
          
          double xTolerance = values.size() / 500;
          double yTolerance = (maxValue - minValue) / 500;

          List<DataPoint> sortedPoints = simplifyRadialDistance(values, xTolerance, yTolerance);

          for (int i = 0; i < sortedPoints.size(); i++) {
            DataPoint filteredPoint = sortedPoints.get(i);
            Point2D pos = filteredPoint.getLocation();
            XYChart.Data element = new XYChart.Data(pos.getX(), pos.getY(), filteredPoint.getIndices());
            elements.add(element);
          }
        } catch(Exception ex) {
          ex.printStackTrace();
        }
      }
      
      ObservableList<XYChart.Data> data = series.getData();
      data.addAll(elements);
                  
      return series;
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
  
  private List<DataPoint> simplifyRadialDistance(List<DataPoint> data, double xTolerance, double yTolerance) {
    List<DataPoint> sortedPoints = new ArrayList();

    for(DataPoint point : data) {
      if(point.isVisible()) {
        DataPoint validPoint = new DataPoint(point.getTimeframe(), point.getValue(), point.getIndices().get(0), point.aboveMin(), point.belowMax(), point.satisfiesFilter());
        sortedPoints.add(validPoint);
      }
    }

    Collections.sort(sortedPoints, (DataPoint p1, DataPoint p2) -> Double.compare(p1.getLocation().getX(), p2.getLocation().getX()));     
    sortedPoints = filterPoints(sortedPoints, xTolerance, yTolerance);    

    Collections.sort(sortedPoints, (DataPoint p1, DataPoint p2) -> Double.compare(p1.getLocation().getY(), p2.getLocation().getY()));
    sortedPoints = filterPoints(sortedPoints, xTolerance, yTolerance);
        
    return sortedPoints;
  }
  
  private List<DataPoint> filterPoints(List<DataPoint> points, double xTolerance, double yTolerance) {
    int len = points.size();
    
    List<DataPoint> newPoints = new ArrayList();
    
    if(len > 0) {
      DataPoint point;
      DataPoint prevPoint = points.get(0);

      newPoints.add(prevPoint);

      for (int i = 1; i < len; i++) {
        point = points.get(i);

        if (getDistance(point.getLocation().getX(), prevPoint.getLocation().getX()) > xTolerance || getDistance(point.getLocation().getY(), prevPoint.getLocation().getY()) > yTolerance) {
            newPoints.add(point);
            prevPoint = point;
        } else {
          newPoints.get(newPoints.size() - 1).addIndices(point.getIndices());
        }
      }
    }
    return newPoints;
  }
  
  private double getDistance(double p1, double p2) {
    return Math.sqrt((p1 - p2) * (p1 - p2));
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
