package prototype.chart;

import com.sun.javafx.charts.Legend;
import com.sun.javafx.charts.Legend.LegendItem;
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
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import static prototype.chart.Chart.getCheckbox;
import static prototype.chart.Chart.getScale;
import static prototype.chart.Chart.getTurtleListView;
import prototype.listener.SelectionEventListener;
import prototype.object.Category;
import prototype.object.Parameter;
import prototype.object.ParameterMap;
import prototype.object.StringValuePair;
import prototype.object.Turtle;
import prototype.object.Type;
import prototype.object.Value;
import prototype.settings.Configuration;
import org.dockfx.DockNode;

public class CategoricalChart implements Chart {
  
  private Scene scene;
  private int[] selectedTurtles;
  private String yParameter;
  private int yParameterIndex;
  private String yParameterValue;
  private List<Turtle> data;
  private boolean liveUpdate;
  
  private DockNode dockNode;
  
  private ParameterMap parameterMap;
  
  private BorderPane rootPane; 
  private ScatterChart<Number,String> scattterChart;
  
  private Rectangle selectionRectangle;
  private Point2D selectionPoint;
  
  private double initSelectionX = 0;
  private double initSelectionWidth = 0;
  private Object[] initSelectionData = new Object[]{ 0, 0 };
  
  private int selectedStartIndex;
  private int selectedEndIndex;
  
  public CategoricalChart(Scene scene, int[] selectedTurtles, String yParameter, int yParameterIndex, String yParameterValue, List<Turtle> data, boolean liveUpdate) {
    this.scene = scene;
    this.selectedTurtles = selectedTurtles;
    this.yParameter = yParameter;
    this.yParameterIndex = yParameterIndex;
    this.yParameterValue = yParameterValue;
    this.data = data;
    this.liveUpdate = liveUpdate;
    this.rootPane = new BorderPane();
    this.rootPane.getStylesheets().add("prototype/plot.css");
    this.parameterMap = new ParameterMap();
    
    initialize();
  }
  
  public CategoricalChart(Scene scene) {
    this.scene = scene;
    
    this.parameterMap = new ParameterMap();
    
    Configuration configuration = new Configuration();
    this.selectedTurtles = new int[configuration.MaxTurtles];
    for(int i = 0; i < configuration.MaxTurtles; i++) {
      this.selectedTurtles[i] = i;
    }
    
    this.yParameterIndex = 0;
    this.data = new ArrayList();
    this.liveUpdate = true;
    
    if(this.parameterMap.GetParametersOfType(Type.Categorical).size() > 0) {
    Parameter firstParameter = this.parameterMap.GetParametersOfType(Type.Categorical).get(0);
      this.yParameter = firstParameter.getName();
      if(firstParameter.getValues().size() > 0) {
        this.yParameterValue = firstParameter.getValues().get(0).getName();
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
    return "Categorical-chart";
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
      
      setDockTitle();
    }
  }
  
  @Override
  public void setDockNode(DockNode dockNode) {
    this.dockNode = dockNode;
  }
  
  private void setDockTitle() {
    if(this.dockNode != null) {
      this.dockNode.setTitle(String.format("%s - %s[%d] (%s) [%d - %d]", getName(), this.yParameter, this.yParameterIndex, this.yParameterValue, this.selectedStartIndex, this.selectedEndIndex));
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
    this.parameterMap = new ParameterMap();
    createChart();
  }
  
  private void createChart() {
    ObservableList<String> categories = FXCollections.observableArrayList();
    for(int turtle : selectedTurtles) {
      categories.add(String.format("Turtle %d", turtle + 1));
    }
    CategoryAxis yAxis = new CategoryAxis(categories);
    
    NumberAxis xAxis = new NumberAxis();
    try {
        int timeframes = this.data.get(0).getTimeFrameCount();
        double scale = getScale(timeframes);
        xAxis = new NumberAxis(0, timeframes, scale);
    } catch(Exception ex) { }
    
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
    dialog.setTitle("Categorical-chart options");
    dialog.setHeaderText("Choose categorical-chart options");
    dialog.setContentText("Choose categorical-chart options:");

    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    CheckBox liveCheckbox = getCheckbox("Live update", liveUpdate);
    
    ListView listView = getTurtleListView(selectedTurtles); 
    
    ChoiceBox<String> parameterChoiceBox = new ChoiceBox();
    ChoiceBox<Integer> indexChoiceBox = new ChoiceBox();
    ChoiceBox<String> valueChoiceBox = new ChoiceBox();

    List<Parameter> choices = parameterMap.GetParametersOfType(Type.Categorical);
    ObservableList<String> options = FXCollections.observableArrayList();
    for(Parameter choise : choices) {
      options.add(choise.getName());
    }
    options = options.sorted();

    parameterChoiceBox.setItems(options);
    if(options.contains(yParameter)) {
      parameterChoiceBox.getSelectionModel().select(yParameter);
      Parameter parameter = parameterMap.GetParameter(yParameter);
      
      int count = parameter.getCount();
      ObservableList<Integer> indexOptions = FXCollections.observableArrayList();
      for(int index = 0; index < count; index++) {
        indexOptions.add(index);
      }      
      indexChoiceBox.setItems(indexOptions);
      if(indexOptions.contains(yParameterIndex)) {
        indexChoiceBox.getSelectionModel().select(yParameterIndex);
      }
      
      List<Value> values = parameter.getValues();
      ObservableList<String> valueOptions = FXCollections.observableArrayList();
      for(Value value : values) {
        valueOptions.add(value.getName());
      }
      valueChoiceBox.setItems(valueOptions);
      if(valueOptions.contains(yParameterValue)) {
        valueChoiceBox.getSelectionModel().select(yParameterValue);
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
          yParameter = parameterChoiceBox.getSelectionModel().getSelectedItem();
          yParameterIndex = indexChoiceBox.getSelectionModel().getSelectedItem();
          yParameterValue = valueChoiceBox.getSelectionModel().getSelectedItem();
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
      double minY = Double.MAX_VALUE;
      double maxY = Double.MIN_VALUE;  

      CategoryAxis yAxis = (CategoryAxis) scattterChart.getYAxis();
      NumberAxis xAxis = (NumberAxis) scattterChart.getXAxis();

      double xAxisShift = getSceneXShift(xAxis);
      double yAxisShift = getSceneYShift(yAxis);

      double height = getRowHeigt();
            
      Parameter parameter = this.parameterMap.GetParameter(yParameter);
      Value value = parameter.getValue(yParameterValue);
      
      for(int turtleIndex : selectedTurtles) {
        
        double currentCategory = -1;
        boolean currentVisibility = true;
        int currentFrame = 0;
        double currentPosition = xAxis.getDisplayPosition(0);
        
        Turtle turtle = data.get(turtleIndex);
        List<DataPoint> categoricalValues = turtle.GetAllValues(yParameter, yParameterIndex, yParameterValue);
        
        for(int timeFrame = 0; timeFrame < categoricalValues.size(); timeFrame++) {
          DataPoint category = categoricalValues.get(timeFrame);
          
          //if(currentCategory == Double.MIN_VALUE)
          //  currentCategory = category.getLocation().getY();

          boolean newBlock = false;
          double newCategory = currentCategory;
          
          if(category.isVisible()) {
            if(!currentVisibility || category.getLocation().getY() != currentCategory || timeFrame >= categoricalValues.size() - 1) {
              newBlock = true;
              newCategory = category.getLocation().getY();    
            }
            currentVisibility = true;
          } else {
            if(currentVisibility || timeFrame >= categoricalValues.size() - 1) {
              currentVisibility = false;
              newBlock = true;
              newCategory = -1;
            }
          }
          
          if(newBlock) {
            double xPosition = xAxis.getDisplayPosition(timeFrame);
            double yPosition = yAxis.getDisplayPosition(String.format("Turtle %d", turtleIndex + 1));

            maxY = Math.max(maxY, yPosition);
            minY = Math.min(minY, yPosition);

            Rectangle categoryBlock = RectangleBuilder.create()
                    .x(currentPosition + xAxisShift)
                    .y(yPosition + yAxisShift - (height / 2))
                    .height(height)
                    .width(xPosition - currentPosition)
                    .userData(new Object[]{ String.format("Turtle %d", turtleIndex + 1), currentFrame, timeFrame })
                      .styleClass(String.format("default-color%d-status-symbol", value.getCategoryIndex((int)currentCategory)))
                    .build();

            this.rootPane.getChildren().add(categoryBlock);

            currentPosition = xPosition;
            currentFrame = timeFrame;
            currentCategory = newCategory;
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
          createRectangleSelectionEvents(child, xAxis, yAxis);
        }
      }
      
      createLegend();
    }
  }
    
  private void createLegend() {
      Parameter parameter = this.parameterMap.GetParameter(yParameter);
      Value value = parameter.getValue(yParameterValue);
      List<Category> categories = value.getCategories();
      
      Legend legend = (Legend)scattterChart.lookup(".chart-legend");
      legend.getStylesheets().add("prototype/plot.css");
          
      List<LegendItem> items = new ArrayList();
      
      for(int index = 0; index < categories.size(); index++) {
          Rectangle legendPoint = RectangleBuilder.create()
                  .height(10)
                  .width(10)
                  .build();
        
        LegendItem item = new Legend.LegendItem(categories.get(index).getName(), legendPoint);
        item.getSymbol().getStyleClass().add(String.format("default-color%d-status-symbol", index));
        
        items.add(item);
      }
      
      legend.getItems().setAll(items);
  }
  
  private void resizeChart() {    
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
      } else {
        height = height - 10;
      }
      return height;
  }
  
  private void createRectangleSelectionEvents(Node node, NumberAxis xAxis, CategoryAxis yAxis) {
    ScatterChart<Number,String> scattterChart = (ScatterChart<Number,String>)rootPane.getCenter();
    
    node.setOnMousePressed((MouseEvent event) -> {
      double xAxisShift = getSceneXShift(xAxis);
      double yAxisShift = getSceneYShift(yAxis);
      
      selectionPoint = new Point2D(event.getX(), event.getY());
      
      Rectangle selection = getSelectionRectangle(event.getX(), event.getY(), xAxisShift, yAxisShift, xAxis.getWidth(), yAxis.getHeight());
            
      int start = xAxis.getValueForDisplay(selection.getX() - xAxisShift).intValue();
      int end = xAxis.getValueForDisplay(selection.getX() + selection.getWidth() - xAxisShift).intValue();
      
      notifyListeners(start, end, false);
    });

    node.setOnMouseDragged((MouseEvent event) -> {
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
