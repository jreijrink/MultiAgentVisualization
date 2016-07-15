package prototype.chart;

import com.sun.javafx.charts.Legend;
import java.util.ArrayList;
import java.util.Collection;
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
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import org.apache.pivot.util.Console;
import org.dockfx.DockNode;
import prototype.Main;
import static prototype.chart.DockElement.getCheckbox;
import static prototype.chart.DockElement.getTurtleListView;
import prototype.listener.SelectionEventListener;
import prototype.object.Parameter;
import prototype.object.ParameterMap;
import prototype.object.Range;
import prototype.object.StringValuePair;
import prototype.object.Turtle;
import prototype.object.Type;
import prototype.object.Value;

public abstract class BaseChart implements DockElement {
  protected Scene scene;
  protected List<Turtle> data;
  private int visibleDataPoints;
  protected BorderPane rootPane; 
  protected DockNode dockNode;  
  protected XYChart XYChart; 
  protected ParameterMap parameterMap;
  protected List<SelectionEventListener> listenerList = new ArrayList();
  
  public int[] selectedTurtles;
  public String parameter;
  public int parameterIndex;
  public String parameterValue;
  public boolean liveUpdate;
  
  protected int selectedStartIndex;
  protected int selectedEndIndex;
  protected boolean forward;
    
  protected Point2D selectionPoint;
  protected Rectangle selectionRectangle;
  protected Rectangle selectionFrame;
  
  protected Point2D basePoint;
  protected List<Rectangle> filterRectangles;
  protected int selectedFilterIndex;
  protected boolean newFilter;
  
  protected Rectangle zoomRectangle;
  protected Rectangle zoomCloseRectangle;
  protected Rectangle moveLeftRectangle;
  protected Rectangle moveRightRectangle;
  protected Range<Integer> zoomRange;
  
  public BaseChart(Scene scene, int[] selectedTurtles, String yParameter, int yParameterIndex, String yParameterValue, List<Turtle> data, boolean liveUpdate) {
    this.scene = scene;
    this.selectedTurtles = selectedTurtles;
    this.parameter = yParameter;
    this.parameterIndex = yParameterIndex;
    this.parameterValue = yParameterValue;
    this.data = data;
    this.liveUpdate = liveUpdate;
    
    this.rootPane = new BorderPane();
    this.rootPane.getStylesheets().add("prototype/plot.css");
    this.filterRectangles = new ArrayList();
    this.parameterMap = new ParameterMap();
  }
  
  public BaseChart(Scene scene, List<Turtle> data, int selectionStart, int selectionEnd, boolean forward) {    
    this.scene = scene;
    this.data = data;
    this.parameterMap = new ParameterMap();
    this.selectedTurtles = new int[]{ 0 };    
    this.parameterIndex = 0;
    this.liveUpdate = true;
    
    this.selectedStartIndex = selectionStart;    
    this.selectedEndIndex = selectionEnd;
    this.forward = forward;
    
    this.rootPane = new BorderPane();
    this.rootPane.getStylesheets().add("prototype/plot.css");
    this.filterRectangles = new ArrayList();
  }
    
  abstract boolean isCategorical();
  
  abstract List<XYChart.Series> getData();
  
  abstract void createAxisFilter();
  
  abstract void createChart(Collection datapoints);
  
  @Override
  public void addSelectionEventListener(SelectionEventListener listener) {
    listenerList.add(listener);
  }
  
  @Override
  public void update() {
    initialize(false);
  }
  
  @Override
  public Node getNode() {
    return rootPane;
  }
  
  @Override
  public void updateData(List<Turtle> data) {
    clearFilters();
    this.data = data;
    initialize(false);
  }
  
  @Override
  public void clearFilters() {
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
  
  @Override
  public void showParameterDialog() {
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(10, 10, 10, 10));
    
    Dialog<Boolean> dialog = new Dialog();
    dialog.setTitle("Categorical-chart options");
    dialog.setHeaderText("Choose categorical-chart options");
    dialog.setContentText("Choose categorical-chart options:");

    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    CheckBox liveCheckbox = getCheckbox("Live update", liveUpdate);
    
    ListView listView = getTurtleListView(selectedTurtles); 
    
    ChoiceBox<String> parameterChoiceBox = new ChoiceBox();
    ChoiceBox<Integer> indexChoiceBox = new ChoiceBox();
    ChoiceBox<String> valueChoiceBox = new ChoiceBox();

    List<Parameter> choices = parameterMap.getAllParameters();
    if(isCategorical())
      choices = parameterMap.getParametersOfType(Type.Categorical);
    
    ObservableList<String> options = FXCollections.observableArrayList();
    for(Parameter choise : choices) {
      options.add(choise.getName());
    }
    options = options.sorted();

    parameterChoiceBox.setItems(options);
    if(options.contains(parameter)) {
      parameterChoiceBox.getSelectionModel().select(parameter);
      Parameter parameter = parameterMap.getParameter(this.parameter);
      
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
        Parameter parameter = parameterMap.getParameter(newValue.toString());
        
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
          clearFilters();
          
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
  
  @Override
  public void selectFrames(final int startIndex, final int endIndex, final boolean drag, final boolean forward) {    
    if((!drag || liveUpdate) && data.size() > 0 && XYChart != null) {
      
      int actualStartIndex = startIndex;
      int actualEndIndex = endIndex;
      
      if(zoomRange != null) {
        actualStartIndex = Math.max(zoomRange.getMin(), actualStartIndex);
        actualEndIndex = Math.min(zoomRange.getMax(), actualEndIndex);
      }
      
      selectedStartIndex = startIndex;
      selectedEndIndex = endIndex;
      this.forward = forward;
      
      clearSelection();
      
      NumberAxis xAxis = (NumberAxis) this.XYChart.getXAxis();
      double xAxisShift = getSceneXShift(xAxis);
      double start = xAxis.getDisplayPosition(actualStartIndex);
      double end = xAxis.getDisplayPosition(actualEndIndex);
      
      if(start == end)
        end +=1;
      
      if(selectionRectangle != null) {
        selectionRectangle.setX(xAxisShift + start);
        selectionRectangle.setWidth(end - start);        
        selectionRectangle.setUserData(new Object[]{ startIndex, endIndex });
      } 
      
      if (xAxis.getWidth() == 0) {
        Platform.runLater(()-> selectFrames(startIndex, endIndex, drag, forward));    
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
  
  public void initialize(boolean resize) {
    setCursor(Cursor.WAIT);    
    this.parameterMap = new ParameterMap();
    
    (new Thread() {
      public void run() {
        List<XYChart.Series> datapoints = getData();
        if(!resize || dataIncreaseAchieved(datapoints)) {
          visibleDataPoints = getDataSize(datapoints);
          Platform.runLater(() -> {
            createChart(datapoints);
            Platform.runLater(() -> {
              setResizeListeners();
              setMouseListeners();
              createAxisFilter();
              createAxisZoom();
              selectFrames(selectedStartIndex, selectedEndIndex, false, forward);
            });
          });
        }
        Platform.runLater(()->  setCursor(Cursor.DEFAULT));
      }
    }).start();
  }
  
  protected void setDockTitle() {
    if(this.dockNode != null) {
      this.dockNode.setTitle(String.format("%s - %s[%d] (%s) [%d - %d]", getName(), this.parameter, this.parameterIndex, this.parameterValue, this.selectedStartIndex, this.selectedEndIndex));
    }
  }
  
  protected void setCursor(Cursor cursor) {
    scene.setCursor(cursor);
    rootPane.setCursor(cursor);    
  }
  
  protected double getXShift(Node node, Node parent, Node axis) {
    double xChartShift = getSceneXShift(node);
    double xAxisShift = getSceneXShift(axis);
    double xParentShift = parent != null ? getSceneXShift(parent) : 0;
    double xShift = xAxisShift - (xChartShift - xParentShift);
    return xShift;
  }
  
  protected double getYShift(Node node, Node parent, Node axis) {
    double yChartShift = getSceneYShift(node);
    double yAxisShift = getSceneYShift(axis);
    double yParentShift = parent != null ? getSceneYShift(parent) : 0;
    double yShift = yAxisShift - (yChartShift - yParentShift);
    return yShift;
  }  
  
  protected void notifyListeners(int startIndex, int endIndex, boolean drag, boolean forward) {
    for(SelectionEventListener listener : listenerList) {
      listener.timeFrameSelected(startIndex, endIndex, drag, forward);
    }
  }
  
  protected int getDataSize(List<XYChart.Series> data) {
    int totalSize = 0;
    for(XYChart.Series series : data) {
      totalSize += series.getData().size();
    }    
    return totalSize;
  }
    
  protected void setResizeListeners() {
    
    NumberAxis xAxis = (NumberAxis) this.XYChart.getXAxis();
    Axis yAxis = this.XYChart.getYAxis();
    
    xAxis.widthProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) -> {   
      Platform.runLater(()-> setZoomControls(xAxis, yAxis));
    });
    
    yAxis.heightProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) -> {  
      Platform.runLater(()-> setZoomControls(xAxis, yAxis));
    });
  }
  
  protected void setMouseListeners() {
    NumberAxis xAxis = (NumberAxis) this.XYChart.getXAxis();
    Axis yAxis = this.XYChart.getYAxis();

    for(XYChart.Series series : (List<XYChart.Series>)XYChart.getData()) {
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
  }
  
  protected void createAxisZoom() {
    NumberAxis xAxis = (NumberAxis) this.XYChart.getXAxis();
    Axis yAxis = this.XYChart.getYAxis();
    
    Platform.runLater(()-> setZoomControls(xAxis, yAxis));
    
    zoomRectangle = RectangleBuilder.create()
            .x(getSceneXShift(xAxis))
            .y(getSceneYShift(yAxis))
            .height(yAxis.getHeight() + xAxis.getHeight())
            .width(0)
            .fill(Color.web("0x222222"))
            .opacity(0.3)
            .id("zoom")
            .build();

    rootPane.getChildren().add(zoomRectangle);
    
    xAxis.setOnMouseMoved((MouseEvent event) -> {        
      if(scene.getCursor() != Cursor.WAIT) {
        setCursor(Cursor.CROSSHAIR);
      }
    });
    
    xAxis.setOnMouseExited((MouseEvent event) -> {   
      if(scene.getCursor() != Cursor.WAIT)
        setCursor(Cursor.DEFAULT);
    });
    
    xAxis.setOnMousePressed((MouseEvent event) -> {
      if(scene.getCursor() != Cursor.WAIT)
        setCursor(Cursor.H_RESIZE);

      double xAxisShift = getSceneXShift(xAxis);    
      double xShift = getXShift(yAxis, null, xAxis);
      double yShift = getYShift(yAxis, null, yAxis);

      basePoint = new Point2D(event.getX(), event.getY());

      Rectangle selection = getSelectionRectangle(basePoint, event.getX(), event.getY(), 0, yShift, xAxis.getWidth(), yAxis.getHeight());

      rootPane.getChildren().remove(zoomRectangle);
      rootPane.getChildren().add(zoomRectangle);

      zoomRectangle.setY(getSceneYShift(yAxis));
      zoomRectangle.setHeight(yAxis.getHeight() + xAxis.getHeight());
      zoomRectangle.setX(xAxisShift + selection.getX());
      zoomRectangle.setWidth(selection.getWidth());
    });

    xAxis.setOnMouseDragged((MouseEvent event) -> {
      setCursor(Cursor.H_RESIZE);

      double xAxisShift = getSceneXShift(xAxis);    
      double xShift = getXShift(yAxis, null, xAxis);
      double yShift = getYShift(yAxis, null, yAxis);

      Rectangle selection = getSelectionRectangle(basePoint, event.getX(), event.getY(), 0, yShift, xAxis.getWidth(), yAxis.getHeight());

      zoomRectangle.setX(xAxisShift + selection.getX());
      zoomRectangle.setWidth(selection.getWidth());
    });
    
    xAxis.setOnMouseReleased((MouseEvent event) -> {
      setCursor(Cursor.DEFAULT);

      double yShift = getYShift(yAxis, null, yAxis);

      Rectangle selection = getSelectionRectangle(basePoint, event.getX(), event.getY(), 0, yShift, xAxis.getWidth(), yAxis.getHeight());

      zoomRectangle.setX(0);
      zoomRectangle.setWidth(0);

      int minValue = (int)xAxis.getValueForDisplay(selection.getX()).doubleValue();
      int maxValue = (int)xAxis.getValueForDisplay(selection.getX() + selection.getWidth()).doubleValue();

      if(maxValue - minValue > 5) {
        xAxis.setLowerBound(minValue);
        xAxis.setUpperBound(maxValue);

        zoomRange = new Range(minValue, maxValue);
        initialize(false);
      }
    });
  }
  
  protected void createRectangleSelectionEvents(Node node, Node parent, NumberAxis xAxis, Axis yAxis) {    
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
  
  protected Rectangle getSelectionRectangle(Point2D start, double mouseX, double mouseY, double xShift, double yShift, double chartWidth, double chartHeight) {
    
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
  
  protected double getSceneXShift(Node node) { 
    double shift = 0; 
    do {  
        shift += node.getLayoutX();  
        node = node.getParent(); 
    } while (node != null && (node.getClass() != LineChart.class && node.getClass() != ScatterChart.class));
    return shift; 
  }
  
  protected double getSceneYShift(Node node) { 
    double shift = 0; 
    do {  
        shift += node.getLayoutY();
        node = node.getParent(); 
    } while (node != null && (node.getClass() != LineChart.class && node.getClass() != ScatterChart.class));
    return shift; 
  }

  protected double getScale(double range) {
    double leftDigits = range;
    int dividings = 0;
    while(leftDigits >= 100) {
      leftDigits = Math.round(leftDigits / 10);
      dividings += 1;
    }
    double scale = Math.round(leftDigits / 10);
    double result = scale * Math.pow(10, dividings);    
    return result;
  }
  
  protected double getRowHeigt() {  
      CategoryAxis yAxis = (CategoryAxis) XYChart.getYAxis();
      
      double height = yAxis.getHeight();
      List<String> categories = yAxis.getCategories();
      if(categories.size() >= 2) {
        double agent1YPosition = yAxis.getDisplayPosition(categories.get(0));
        double agent2YPosition = yAxis.getDisplayPosition(categories.get(1));
        height = Math.abs(agent2YPosition - agent1YPosition);
      } else {
        height = height - 10;
      }
      return height;
  }
  
  private void setZoomControls(ValueAxis xAxis, Axis yAxis) {
    Legend legend = (Legend)XYChart.lookup(".chart-legend");
    if(legend.getHeight() == 0) {
      Platform.runLater(()-> setZoomControls(xAxis, yAxis));
      return;
    }
    
    if(zoomCloseRectangle != null) {
      rootPane.getChildren().remove(zoomCloseRectangle);
      rootPane.getChildren().remove(moveLeftRectangle);
      rootPane.getChildren().remove(moveRightRectangle);
    }
    
    if(zoomRange != null) {
      
      double rectangeWidth, rectangleHeight;
      rectangeWidth = rectangleHeight = xAxis.getHeight() / 2;
      double xPosition = getSceneXShift(xAxis);
      double yPosition = getSceneYShift(yAxis) + yAxis.getHeight() +  + rectangleHeight + 5;
      double xOffset = 5;
      
      moveLeftRectangle = RectangleBuilder.create()
          .x(xPosition)
          .y(yPosition)
          .height(rectangleHeight)
          .width(rectangeWidth)
          .fill(Color.web("0x222222"))
          .opacity(1)
          .id("leftZoom")
          .build();
      Image leftImg = new Image(Main.class.getClassLoader().getResource("prototype/moveleft.png").toExternalForm());
      moveLeftRectangle.setFill(new ImagePattern(leftImg));
      
      moveLeftRectangle.setOnMouseMoved((MouseEvent event) -> {        
        if(scene.getCursor() != Cursor.WAIT) {
          setCursor(Cursor.HAND);
        }
      });
      moveLeftRectangle.setOnMouseExited((MouseEvent event) -> {   
        if(scene.getCursor() != Cursor.WAIT)
          setCursor(Cursor.DEFAULT);
      });
      moveLeftRectangle.setOnMousePressed((MouseEvent event) -> {
        zoomLeft();
      });
    
      rootPane.getChildren().add(moveLeftRectangle);
      
      
      xPosition += rectangeWidth + xOffset;
      
      zoomCloseRectangle = RectangleBuilder.create()
          .x(xPosition)
          .y(yPosition)
          .height(rectangleHeight)
          .width(rectangeWidth)
          .fill(Color.web("0x222222"))
          .opacity(1)
          .id("closeZoom")
          .build();
      Image zoomImg = new Image(Main.class.getClassLoader().getResource("prototype/zoomout.png").toExternalForm());
      zoomCloseRectangle.setFill(new ImagePattern(zoomImg));

      zoomCloseRectangle.setOnMouseMoved((MouseEvent event) -> {        
        if(scene.getCursor() != Cursor.WAIT) {
          setCursor(Cursor.HAND);
        }
      });
      zoomCloseRectangle.setOnMouseExited((MouseEvent event) -> {   
        if(scene.getCursor() != Cursor.WAIT)
          setCursor(Cursor.DEFAULT);
      });
      zoomCloseRectangle.setOnMousePressed((MouseEvent event) -> {
        resetZooming();
      });
      
      rootPane.getChildren().add(zoomCloseRectangle);
      
      
      xPosition += rectangeWidth + xOffset;
      
      moveRightRectangle = RectangleBuilder.create()
          .x(xPosition)
          .y(yPosition)
          .height(rectangleHeight)
          .width(rectangeWidth)
          .fill(Color.web("0x222222"))
          .opacity(1)
          .id("rightZoom")
          .build();
      Image rightImg = new Image(Main.class.getClassLoader().getResource("prototype/moveright.png").toExternalForm());
      moveRightRectangle.setFill(new ImagePattern(rightImg));
      
      moveRightRectangle.setOnMouseMoved((MouseEvent event) -> {        
        if(scene.getCursor() != Cursor.WAIT) {
          setCursor(Cursor.HAND);
        }
      });
      moveRightRectangle.setOnMouseExited((MouseEvent event) -> {   
        if(scene.getCursor() != Cursor.WAIT)
          setCursor(Cursor.DEFAULT);
      });
      moveRightRectangle.setOnMousePressed((MouseEvent event) -> {
        zoomRight();
      });
    
      rootPane.getChildren().add(moveRightRectangle);
    }
  }
  
  private void resetZooming() {
    zoomRange = null;
    initialize(false);
  }
    
  private void zoomLeft() {
    int range = zoomRange.getMax() - zoomRange.getMin();
    double increase = range * 0.25;
    int min = Math.max(0 , (int)(zoomRange.getMin() - increase));
    zoomRange = new Range(min, min + range);
    
    initialize(false);
  }
    
  private void zoomRight() {
    int timeframes = this.data.get(0).getTimeFrameCount();
    int range = zoomRange.getMax() - zoomRange.getMin();
    double increase = range * 0.25;
    int max = Math.min(timeframes , (int)(zoomRange.getMax() + increase));
    zoomRange = new Range(max - range, max);
    
    initialize(false);
  }
    
  private boolean dataIncreaseAchieved(List<XYChart.Series> data) {
    int newSize = getDataSize(data);
    double difference = Math.abs(newSize - visibleDataPoints);
    double threshold = ((double)visibleDataPoints) * 0.2;
    
    return difference > threshold; 
  }

  private void clearSelection() {    
    if(XYChart != null) {
      Axis yAxis = XYChart.getYAxis();
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
}
