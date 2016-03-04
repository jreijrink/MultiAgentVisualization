package jfreechart.chart;

import com.sun.javafx.charts.Legend;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.util.Pair;
import jfreechart.object.ParameterMap;
import jfreechart.listener.SelectionEventListener;
import jfreechart.object.StringValuePair;
import jfreechart.object.TimeFrame;
import static jfreechart.Parser.MAX_TURTLES;
import static jfreechart.chart.Chart.getCheckbox;
import static jfreechart.chart.Chart.getTurtleListView;

public class ScatterPlot implements Chart {
  private Scene scene;
  private int[] selectedTurtles;
  private String yParameter;
  private int yParameterIndex;
  private List<TimeFrame> data;
  private List<Point2D>[] dataMap;
  private boolean liveUpdate;
  
  private double minValueX;
  private double maxValueX;
  
  private double minValueY;
  private double maxValueY;
  
  private BorderPane rootPane; 
  private Point2D selectionPoint;
  private Rectangle selectionRectangle;
  private ScatterChart<Number,Number> scatterChart;
  
  private int selectedStartIndex;
  private int selectedEndIndex;
  
  public ScatterPlot(Scene scene, int[] selectedTurtles, String yParameter, int yParameterIndex, List<TimeFrame> data,  boolean liveUpdate) {
    this.scene = scene;
    this.selectedTurtles = selectedTurtles;
    this.yParameter = yParameter;
    this.yParameterIndex = yParameterIndex;
    this.data = data;
    this.rootPane = new BorderPane();
    this.rootPane.getStylesheets().add("jfreechart/plot.css");
    initialize();
  }
  
  public ScatterPlot(Scene scene) {
    this(scene, new int[]{0}, "batteryVoltage", 0, new ArrayList(), false);
  }
  
  @Override
  public Node getNode() {
    return rootPane;
  }
  
  @Override
  public String getName() {
    return "ScatterPlot";
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
      
      ScatterChart<Number,Number> scattterChart = (ScatterChart<Number,Number>)rootPane.getCenter();

      clearSelection();

      System.out.println("SELECTFRAME");

      for(int selected : selectedTurtles) {
        List<Point2D> turtleData = dataMap[selected];

        List<Point2D> points = new ArrayList();
        for(int frame = startIndex; frame < endIndex; frame++) {    
          Point2D point = turtleData.get(frame);
          points.add(point);
        }

        ObservableList<Series<Number, Number>> series = scatterChart.getData();
        Series<Number,Number> serie = series.get(selected);
        ObservableList<Data<Number, Number>> datas = serie.getData();
        for(Data<Number, Number> data : datas) {
          List<Integer> indices = (List<Integer>)data.getExtraValue();
          for(int index : indices){
            Node node = data.getNode();  
            List<String> classes = node.getStyleClass();

            if(index >= startIndex && index <= endIndex) {
              if(classes.contains(String.format("default-color%d", selected))) {
                node.getStyleClass().add(String.format("default-color%d-selected-chart-symbol", selected));
                node.getStyleClass().remove(String.format("default-color%d", selected));
              }
            } else {
              if(classes.contains(String.format("default-color%d-selected-chart-symbol", selected))) {
                node.getStyleClass().add(String.format("default-color%d", selected));
                node.getStyleClass().remove(String.format("default-color%d-selected-chart-symbol", selected));
              }
            }
          }
        }
      }
      System.out.println("SELECTFRAME DONE");
    }
  }
  
  @Override
  public void showParameterDialog() {
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(10, 10, 10, 10));
    
    Dialog<Boolean> dialog = new Dialog();
    dialog.setTitle("Lineplot options");
    dialog.setHeaderText("Choose lineplot options");
    dialog.setContentText("Choose lineplot options:");
    
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    CheckBox liveCheckbox = getCheckbox("Live update", liveUpdate);
    
    ListView listView = getTurtleListView(selectedTurtles); 
    
    ChoiceBox<String> parameterChoiceBox = new ChoiceBox();
    ChoiceBox<String> indexChoiceBox = new ChoiceBox();
    
    if(data.size() > 0) {
      ParameterMap parameters = data.get(0).getTurtles().get(0).getParameters();

      List<String> choices = parameters.getKeys();
      ObservableList<String> options = FXCollections.observableArrayList();
      for(String choise : choices) {
        options.add(choise);
      }

      parameterChoiceBox.setItems(options);
      parameterChoiceBox.getSelectionModel().select(yParameter);

      ObservableList<String> selectedOptions = getParameterIndices(parameters, yParameter);
      indexChoiceBox.setItems(selectedOptions);
      indexChoiceBox.getSelectionModel().select(yParameterIndex);

      parameterChoiceBox.getSelectionModel().selectedIndexProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
        ObservableList<String> indexOptions = getParameterIndices(parameters, choices.get(newValue.intValue()));
        indexChoiceBox.setItems(indexOptions);
        indexChoiceBox.getSelectionModel().select(0);
      });
    }
    
    grid.add(liveCheckbox, 0, 0);
    
    grid.add(new Label("Parameter:"), 0, 1);
    grid.add(parameterChoiceBox, 0, 2);
    grid.add(new Label("Index:"), 0, 3);
    grid.add(indexChoiceBox, 0, 4);

    grid.add(new Label("Turtles"), 0, 5);
    grid.add(listView, 0, 6);
    
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
          yParameter = parameterChoiceBox.getSelectionModel().getSelectedItem();
          yParameterIndex = indexChoiceBox.getSelectionModel().getSelectedIndex();
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
    NumberAxis yAxis = (NumberAxis) scatterChart.getYAxis();
    double yAxisShift = getSceneYShift(yAxis);
  
    selectionRectangle.setX(0);
    selectionRectangle.setY(0);
    selectionRectangle.setWidth(0);
    selectionRectangle.setHeight(yAxis.getHeight() + yAxisShift);
  }
  
  private void initialize() {
    if(data.size() > 0) {
      this.dataMap = createDataMap(yParameter, yParameterIndex, data);

      getMinMaxValue();
      createChart();
      setMouseListeners();
      
      selectFrames(selectedStartIndex, selectedEndIndex, false);
    }
  }
  
  private void createChart() {
    System.out.printf("getChart\n");
    
    final NumberAxis xAxis = getAxis(this.minValueX, this.maxValueX);
    final NumberAxis yAxis = getAxis(this.minValueY, this.maxValueY);
    
    this.scatterChart = new ScatterChart<>(xAxis,yAxis);
    
    if(data.size() > 0) {
      ParameterMap parameters = data.get(0).getTurtles().get(0).getParameters();
      String selectedYIndex = getParameterIndices(parameters, this.yParameter).get(this.yParameterIndex);

      xAxis.setLabel("Time");

      yAxis.setLabel(this.yParameter + " " + selectedYIndex);

      this.scatterChart.getData().addAll(getData());
    }
    
    System.out.printf("Completed chart\n");
    
    rootPane.getChildren().clear();
    
    System.out.printf("CLEARED \n");
    rootPane.setCenter(this.scatterChart);
    System.out.printf("SET CENTER \n");
  }
  
  private ObservableList<String> getParameterIndices(ParameterMap parameters, String selection) {
    double[][] values = parameters.getValue(selection);
    ObservableList<String> indexOptions = FXCollections.observableArrayList();
    for (int row = 0; row < values.length; row++) {
      for (int col = 0; col < values[row].length; col++) {
        indexOptions.add(String.format("[%d][%d]", row, col));
      }
    }
    return indexOptions;
  }
  
  private void setMouseListeners() {

    ScatterChart<Number,Number> scattterChart = (ScatterChart<Number,Number>)rootPane.getCenter();
    
    NumberAxis xAxis = (NumberAxis) scattterChart.getXAxis();
    NumberAxis yAxis = (NumberAxis) scattterChart.getYAxis();

    for(Series series : scatterChart.getData()) {
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
            .stroke(Color.web("0x222222"))
            .fill(Color.TRANSPARENT)
            .strokeWidth(1)
            .strokeDashArray(2d)
            .id("selection")
            .build();    
    rootPane.getChildren().add(selectionRectangle);
    
    System.out.printf("END LISTEN \n");
  }
  
  private void createRectangleSelectionEvents(Node node, NumberAxis xAxis, NumberAxis yAxis) {
    ScatterChart<Number,Number> scattterChart = (ScatterChart<Number,Number>)rootPane.getCenter();
    
    node.setOnMousePressed((MouseEvent event) -> {
      System.out.printf("MOUSE_PRESSED \n");

      double xChartShift = getSceneXShift(node);
      double yChartShift = getSceneYShift(node);

      selectionPoint = new Point2D(event.getX() + xChartShift, event.getY() + yChartShift);
      
      notifyListeners(0, 0, false);
    });

    node.setOnMouseDragged((MouseEvent event) -> {
      System.out.printf("MOUSE_DRAGGED \n");      

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
      System.out.printf("MOUSE_RELEASED \n");      

      double xChartShift = getSceneXShift(node);
      double yChartShift = getSceneYShift(node);
      double xAxisShift = getSceneXShift(xAxis);
      double yAxisShift = getSceneYShift(yAxis);

      Rectangle selection = getSelectionRectangle(event.getX(), event.getY(), xChartShift, yChartShift, xAxis.getWidth() + xAxisShift - xChartShift, yAxis.getHeight() + yAxisShift- yChartShift);
      
      int start = xAxis.getValueForDisplay(selection.getX() - xAxisShift).intValue();
      int end = xAxis.getValueForDisplay(selection.getX() + selection.getWidth() - xAxisShift).intValue();
      
      notifyListeners(start, end, false);
      
      selectionRectangle.setX(0);
      selectionRectangle.setWidth(0);      
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
      int turtles = data.get(0).getTurtles().size();
      for(int turtle =  0; turtle < turtles; turtle++) {
        boolean showData = (new ArrayList<Integer>() {{ for (int i : selectedTurtles) add(i); }}).contains(turtle);      
        seriesList.add(getSeries(turtle, showData));
      }
    }

    return seriesList;
  }
  
  private XYChart.Series getSeries(int turtle, boolean showData) {
      System.out.printf("getSeries for %d\n", turtle);

      XYChart.Series series = new XYChart.Series();
      series.setName(String.format("Turtle %d", turtle + 1));
    
      System.out.printf("Start filling values for %d\n", turtle);
      List<XYChart.Data> elements = new ArrayList();
            
      if(showData) {
        double xTolerance = (maxValueX - minValueX) / 500;
        double yTolerance = (maxValueY - minValueY) / 500;
        
        System.out.printf("xTolerance: %f\n", xTolerance);
        System.out.printf("yTolerance: %f\n", yTolerance);

        List<DataPoint> sortedPoints = simplifyRadialDistance(this.dataMap[turtle], xTolerance, yTolerance);
        
        for (int i = 0; i < sortedPoints.size(); i++) {
          DataPoint filteredPoint = sortedPoints.get(i);
          Point2D pos = filteredPoint.getLocation();
          XYChart.Data element = new XYChart.Data(pos.getX(), pos.getY(), filteredPoint.getIndices());
          elements.add(element);
        }
      }
      
      ObservableList<XYChart.Data> data = series.getData();
      data.addAll(elements);
      
      System.out.printf("Completed filling values for %d\n", turtle);
      
      System.out.printf("Done for %d\n", turtle);
      return series;
  }
  
  private List<DataPoint> simplifyRadialDistance(List<Point2D> points, double xTolerance, double yTolerance) {
    //System.out.printf("START SIMPLIFY, size: %d\n", points.size());
    
    List<DataPoint> sortedPoints = new ArrayList();
    
    if(points.size() > 0) {
      for(int i = 0; i < points.size(); i++) {
       DataPoint point = new DataPoint(points.get(i), i);
       sortedPoints.add(point);
      }

      Collections.sort(sortedPoints, (DataPoint p1, DataPoint p2) -> Double.compare(p1.getLocation().getX(), p2.getLocation().getX()));     
      sortedPoints = filterPoints(sortedPoints, xTolerance, yTolerance);    

      Collections.sort(sortedPoints, (DataPoint p1, DataPoint p2) -> Double.compare(p1.getLocation().getY(), p2.getLocation().getY()));
      sortedPoints = filterPoints(sortedPoints, xTolerance, yTolerance);    
    }
    //System.out.printf("END SIMPLIFY, size: %d\n", sortedPoints.size());
    
    return sortedPoints;
  }
  
  private List<DataPoint> filterPoints(List<DataPoint> points, double xTolerance, double yTolerance) {
    int len = points.size();
    
    DataPoint point;
    DataPoint prevPoint = points.get(0);

    List<DataPoint> newPoints = new ArrayList();
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
    return newPoints;
  }
  
  private double getDistance(double p1, double p2) {
    return Math.sqrt((p1 - p2) * (p1 - p2));
  }

  private void getMinMaxValue() {
    System.out.printf("getMinMaxValue!\n");
    
    double minX = Double.MAX_VALUE;
    double maxX = Double.MIN_VALUE;
    
    double minY = Double.MAX_VALUE;
    double maxY = Double.MIN_VALUE;

    for (int turtle = 0; turtle < this.dataMap.length; turtle++) {
      for (int index = 0; index < this.dataMap[turtle].size(); index++) {
        Point2D value = this.dataMap[turtle].get(index);
        
        double valueX = value.getX();
        double valueY = value.getY();
        
        if(valueX < minX)
          minX = valueX;
        if(valueX > maxX)
          maxX = valueX;
        
        if(valueY < minY)
          minY = valueY;
        if(valueY > maxY)
          maxY = valueY;
      }
    }
    
    minValueX = minX;
    maxValueX = maxX;
    minValueY = minY;
    maxValueY = maxY;
    
    System.out.printf("getMinMaxValue DONE!\n");
  }
  
  private List<Point2D>[] createDataMap(String yParameter, int yParameterIndex, List<TimeFrame> data) {
    
    System.out.printf("createDataMap!\n");
    
    List<Point2D>[] map = new List[MAX_TURTLES];
    
    int[] ySelectedRowCol = getSelectedColRow(data, yParameter, yParameterIndex);

    for(int turtle = 0; turtle < MAX_TURTLES; turtle++) {
      map[turtle] = new ArrayList();
      for(int i = 0; i < data.size(); i++) {
          List<jfreechart.object.Turtle> turtles = data.get(i).getTurtles();
          double yValue = turtles.get(turtle).getParameters().getValue(yParameter)[ySelectedRowCol[0]][ySelectedRowCol[1]];

          map[turtle].add(new Point2D(i, yValue));
      }
    }
    System.out.printf("createDataMap DONE!\n");
    
    return map;
  }
  
  private int[] getSelectedColRow(List<TimeFrame> data, String parameter, int parameterIndex) {
    int selectedRow = 0;
    int selectedCol = 0;
    int index = 0;
    if(data.size() > 0) {
      double[][] values = data.get(0).getTurtles().get(0).getParameters().getValue(parameter);
      for (int row = 0; row < values.length; row++) {
        for (int col = 0; col < values[row].length; col++) {
          if(index == parameterIndex) {
            selectedRow = row;
            selectedCol = col;
          }
          index++;
        }
      }
    }
    return new int[] { selectedRow, selectedCol };
  }

  private NumberAxis getAxis(double min, double max) {
    //min = Math.floor(min / 10) * 10;
    //max = Math.ceil(max / 10) * 10;
    double scale = (max - min) / 5;
    return new NumberAxis(min, max, scale);
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
