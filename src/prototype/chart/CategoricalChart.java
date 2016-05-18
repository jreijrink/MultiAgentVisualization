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
import javafx.scene.Cursor;
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
import static prototype.chart.Chart.getAllTurtles;
import prototype.object.Filter;

public class CategoricalChart implements Chart {
  private static final int MIN_WIDTH = 1;
  
  private Scene scene;
  private List<Turtle> data;
  private List<SelectionEventListener> listenerList = new ArrayList();
  
  public int[] selectedTurtles;
  public String parameter;
  public int parameterIndex;
  public String parameterValue;
  public boolean liveUpdate;
  
  private DockNode dockNode;
  
  private ParameterMap parameterMap;
  
  private BorderPane rootPane; 
  private ScatterChart<Number,String> scattterChart;
  
  private Rectangle selectionRectangle;
  private Point2D selectionPoint;
  private Rectangle selectionFrame;
  
  private Point2D basePoint;
  private List<Rectangle> filterRectangles;
  private int selectedFilterIndex;
  private boolean newFilter;
  
  private double initSelectionX = 0;
  private double initSelectionWidth = 0;
  private Object[] initSelectionData = new Object[]{ 0, 0 };
  
  private int selectedStartIndex;
  private int selectedEndIndex;
  private boolean forward;
  
  public CategoricalChart(Scene scene, int[] selectedTurtles, String yParameter, int yParameterIndex, String yParameterValue, List<Turtle> data, boolean liveUpdate) {
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
    
    initialize();
  }
  
  public CategoricalChart(Scene scene, List<Turtle> data, int selectionStart, int selectionEnd, boolean forward) {
    this.scene = scene;
    
    this.parameterMap = new ParameterMap();
    
    Configuration configuration = new Configuration();
    this.selectedTurtles = new int[configuration.MaxTurtles];
    for(int i = 0; i < configuration.MaxTurtles; i++) {
      this.selectedTurtles[i] = i;
    }
    
    this.selectedStartIndex = selectionStart;    
    this.selectedEndIndex = selectionEnd;    
    this.forward = forward;
    
    this.parameterIndex = 0;
    this.data = data;
    this.liveUpdate = true;
    
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
  public Chart getCopy() {
    return new CategoricalChart(scene, selectedTurtles, parameter, parameterIndex, parameterValue, data, liveUpdate);
  }
  
  @Override
  public void addSelectionEventListener(SelectionEventListener listener) {
    listenerList.add(listener);
  }
  
  @Override
  public void updateData(List<Turtle> data) {
    clearFilter();
    this.data = data;
    initialize();
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
  public void selectFrames(int startIndex, int endIndex, boolean drag, boolean forward) {
    if((!drag || liveUpdate) && data. size() > 0) {
      selectedStartIndex = startIndex;
      selectedEndIndex = endIndex;
      this.forward = forward;
      
      NumberAxis xAxis = (NumberAxis) scattterChart.getXAxis();
      double xAxisShift = getSceneXShift(xAxis);
      double start = xAxis.getDisplayPosition(startIndex);
      double end = xAxis.getDisplayPosition(endIndex);
      
      if(start == end)
        end +=1;
      
      if(selectionRectangle != null) {        
        selectionRectangle.setX(xAxisShift + start);
        selectionRectangle.setWidth(end - start);
        selectionRectangle.setUserData(new Object[]{ startIndex, endIndex });
      } else {
        initSelectionX = xAxisShift + start;
        initSelectionWidth = end - start;
        initSelectionData = new Object[]{ startIndex, endIndex };
      }
      
      if(selectionFrame != null) {
        if(forward)
          selectionFrame.setX(xAxisShift + end);
        else
          selectionFrame.setX(xAxisShift + start);
        selectionFrame.setUserData(forward);
      }
      
      setDockTitle();
    }
  }
  
  @Override
  public void update() {
    initialize();
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

    List<Parameter> choices = parameterMap.getParametersOfType(Type.Categorical);
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
      initialize();
    }
  }
  
  private void setDockTitle() {
    if(this.dockNode != null) {
      this.dockNode.setTitle(String.format("%s - %s[%d] (%s) [%d - %d]", getName(), this.parameter, this.parameterIndex, this.parameterValue, this.selectedStartIndex, this.selectedEndIndex));
    }
  }
  
  private void notifyListeners(int startIndex, int endIndex, boolean drag, boolean forward) {
    for(SelectionEventListener listener : listenerList) {
      listener.timeFrameSelected(startIndex, endIndex, drag, forward);
    }
  }
  
  private void initialize() {
    this.parameterMap = new ParameterMap();
    createChart();
  }
  
  private void setCursor(Cursor cursor) {
    scene.setCursor(cursor);
    rootPane.setCursor(cursor);    
  }
  
  private void createChart() {
    ObservableList<String> categories = FXCollections.observableArrayList();
    
    try {
      Parameter parameter = this.parameterMap.getParameter(this.parameter);
      Value value = parameter.getValue(parameterValue);
      for(Category category : value.getCategories()) {
        categories.add(category.getName());
      }
    } catch(Exception ex) {
      ex.printStackTrace();
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
              selectFrames(selectedStartIndex, selectedEndIndex, false, forward);
              timer.cancel();
              timer.purge();
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
      CategoryAxis yAxis = (CategoryAxis) scattterChart.getYAxis();
      NumberAxis xAxis = (NumberAxis) scattterChart.getXAxis();

      double xAxisShift = getSceneXShift(xAxis);
      double yAxisShift = getSceneYShift(yAxis);

      double height = getRowHeigt();
            
      Parameter parameter = this.parameterMap.getParameter(this.parameter);
      Value value = parameter.getValue(parameterValue);
      
      for(Category category : value.getCategories()) {        
        double yPosition = yAxis.getDisplayPosition(category.getName());
        int timeframes = this.data.get(0).getTimeFrameCount();

        Rectangle categoryBlock = RectangleBuilder.create()
                .x(xAxisShift)
                .y(yPosition + yAxisShift - (height / 2))
                .height(height)
                .width(xAxis.getWidth())
                .userData(new Object[]{ category.getName(), 0, 0, timeframes })
                .styleClass("default-category-background")
                .id("background")
                .build();

        this.rootPane.getChildren().add(categoryBlock);
      }
            
      int selectionSize = selectedTurtles.length;
      double offset = height * 0.05;
      double turtleHeight = (height - (offset * 2)) / selectionSize;
      
      for(int index = 0; index < selectionSize; index++) {
        int turtleIndex = selectedTurtles[index];
        
        double currentCategory = -1;
        boolean currentVisibility = true;
        int currentFrame = 0;
        double currentPosition = xAxis.getDisplayPosition(0);
        
        Turtle turtle = data.get(turtleIndex);
        List<DataPoint> categoricalValues = turtle.getAllValues(this.parameter, parameterIndex, parameterValue);
        
        for(int timeFrame = 0; timeFrame < categoricalValues.size(); timeFrame++) {
          DataPoint category = categoricalValues.get(timeFrame);
          
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
            double yPosition = yAxis.getDisplayPosition(value.getCategoryName((int)currentCategory));

            Rectangle categoryBlock = RectangleBuilder.create()
                    .x(currentPosition + xAxisShift)
                    .y(yPosition + yAxisShift - (height / 2) + (turtleHeight * index) + offset)
                    .height(turtleHeight)
                    .width(Math.max(xPosition - currentPosition, MIN_WIDTH))
                    .userData(new Object[]{ value.getCategoryName((int)currentCategory), index, currentFrame, timeFrame })
                    .styleClass(String.format("default-secondary-color%d-fill", turtleIndex))
                    .build();

            this.rootPane.getChildren().add(categoryBlock);

            currentPosition = xPosition;
            currentFrame = timeFrame;
            currentCategory = newCategory;
          }
        }
      }

      if(selectionRectangle != null) 
        rootPane.getChildren().remove(selectionRectangle);
      
      selectionRectangle = RectangleBuilder.create()
              .x(initSelectionX)
              .y(yAxisShift + 5)
              .height(yAxis.getHeight() - 10)
              .width(initSelectionWidth)
              .fill(Color.web("0x222222"))
              .opacity(0.2)
              .id("selection")
              .userData(initSelectionData)
              .build();    
      rootPane.getChildren().add(selectionRectangle);
      
      if(selectionFrame != null) 
        rootPane.getChildren().remove(selectionFrame);
      
      selectionFrame = RectangleBuilder.create()
              .x(0)
              .y(0)
              .height(yAxis.getHeight())
              .width(2)
              .fill(Color.web("0x222222"))
              .opacity(0.4)
              .id("selection")
              .build();
      selectionFrame.setUserData(true);
      rootPane.getChildren().add(selectionFrame);
      
      for(Node child : this.rootPane.getChildren()) {
        if(child.getClass() == Rectangle.class) {
          createRectangleSelectionEvents(child, xAxis, yAxis);
        }
      }
      
      createAxisFilter();
      
      createLegend();
    }
  }
    
  private void createLegend() {
      Legend legend = (Legend)scattterChart.lookup(".chart-legend");
      legend.getStylesheets().add("prototype/plot.css");
          
      List<LegendItem> items = new ArrayList();
      
      for(int turtle = 0; turtle < new Configuration().MaxTurtles; turtle++) {
          Rectangle legendPoint = RectangleBuilder.create()
                  .height(10)
                  .width(10)
                  .build();
        
        LegendItem item = new Legend.LegendItem(String.format("Turtle %d", turtle + 1), legendPoint);
        item.getSymbol().getStyleClass().add(String.format("default-color%d-fill", turtle));
        
        items.add(item);
      }
      
      legend.getItems().setAll(items);
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
    
    NumberAxis xAxis = (NumberAxis) this.scattterChart.getXAxis();
    CategoryAxis yAxis = (CategoryAxis) this.scattterChart.getYAxis();
    
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
        
        createRectangleSelectionEvents(filterRectangle, xAxis, yAxis);
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

            List<String> filterCategories = new ArrayList();
            boolean started = false;
            for(String category : yAxis.getCategories()) {
              if(category.equals(minValue))
                started = true;
              if(started)
                filterCategories.add(category);
              if(category.equals(maxValue))
                started = false;
            }

            Filter filter = filter(filterCategories);
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
    
  private Filter filter(List<String> filterCategories) {
    if(filterCategories.size() > 0) {
      Parameter selectedParameter = this.parameterMap.getParameter(this.parameter);
      Value value = selectedParameter.getValue(parameterValue);

      List<Double> filterValues = new ArrayList();
      for(String filterCategory : filterCategories) {
        for(Category category : value.getCategories()) {
          if(category.getName().equals(filterCategory)) {
            filterValues.add((double)category.getValue());
            break; 
          }
        }
      }

      Filter filter = new Filter(this, this.parameter, this.parameterIndex, this.parameterValue, getAllTurtles(), filterValues);

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
    CategoryAxis yAxis = (CategoryAxis) scattterChart.getYAxis();
    NumberAxis xAxis = (NumberAxis) scattterChart.getXAxis();

    double xAxisShift = getSceneXShift(xAxis);
    double yAxisShift = getSceneYShift(yAxis);
    
    double height = getRowHeigt();
    int selectionSize = selectedTurtles.length;
    double offset = height * 0.05;
    double turtleHeight = (height - (offset * 2)) / selectionSize;

    for(Node child : this.rootPane.getChildren()) {
      if(child.getClass() == Rectangle.class && child.getId() == null) {
        Rectangle rectChild = (Rectangle)child;
        
        Object[] userData = (Object[])rectChild.getUserData();
        String category = (String)userData[0];
        int index = (int)userData[1];
        int startFrame = (int)userData[2];
        int endFrame = (int)userData[3];

        double startPosition = xAxis.getDisplayPosition(startFrame);
        double endPosition = xAxis.getDisplayPosition(endFrame);
        double yPosition = yAxis.getDisplayPosition(category);

        rectChild.setX(startPosition + xAxisShift);
        rectChild.setWidth(Math.max(endPosition - startPosition, MIN_WIDTH));
        rectChild.setY(yPosition + yAxisShift - (height / 2) + (turtleHeight * index) + offset);
        rectChild.setHeight(turtleHeight);
      } else {
        if(child.getClass() == Rectangle.class && "background".equals(child.getId())) {
          Rectangle rectChild = (Rectangle)child;

          Object[] userData = (Object[])rectChild.getUserData();
          String category = (String)userData[0];
          int index = (int)userData[1];
          int startFrame = (int)userData[2];
          int endFrame = (int)userData[3];

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
      
      Rectangle selection = getSelectionRectangle(selectionPoint, event.getX(), event.getY(), xAxisShift, yAxisShift, xAxis.getWidth(), yAxis.getHeight());
            
      int start = xAxis.getValueForDisplay(selection.getX() - xAxisShift).intValue();
      int end = xAxis.getValueForDisplay(selection.getX() + selection.getWidth() - xAxisShift).intValue();
      
      notifyListeners(start, end, false, true);
    });

    node.setOnMouseDragged((MouseEvent event) -> {      
      if(selectionPoint != null) {
        double xAxisShift = getSceneXShift(xAxis);
        double yAxisShift = getSceneYShift(yAxis);

        Rectangle selection = getSelectionRectangle(selectionPoint, event.getX(), event.getY(), xAxisShift, yAxisShift, xAxis.getWidth(), yAxis.getHeight());

        int start = xAxis.getValueForDisplay(selection.getX() - xAxisShift).intValue();
        int end = xAxis.getValueForDisplay(selection.getX() + selection.getWidth() - xAxisShift).intValue();

        boolean forward = true;
        if( selectionPoint.getX() == (selection.getX() + selection.getWidth())) {
          //Backward selection
          forward = false;
        }

        notifyListeners(start, end, true, forward);
        
        selectFrames(start, end, false, forward); //Always update this selection
      }
    });
    
    node.setOnMouseReleased((MouseEvent event) -> {      
      if(selectionPoint != null) {
        double xAxisShift = getSceneXShift(xAxis);
        double yAxisShift = getSceneYShift(yAxis);

        Rectangle selection = getSelectionRectangle(selectionPoint, event.getX(), event.getY(), xAxisShift, yAxisShift, xAxis.getWidth(), yAxis.getHeight());

        int start = xAxis.getValueForDisplay(selection.getX() - xAxisShift).intValue();
        int end = xAxis.getValueForDisplay(selection.getX() + selection.getWidth() - xAxisShift).intValue();

        boolean forward = true;
        if( selectionPoint.getX() == (selection.getX() + selection.getWidth())) {
          //Backward selection
          forward = false;
        }

        notifyListeners(start, end, true, forward);
      }
    });
  }
  
  private Rectangle getSelectionRectangle(Point2D start, double mouseX, double mouseY, double xShift, double yShift, double chartWidth, double chartHeight) {
    
    //Bound the rectangle to be only within the chart
    mouseX = Math.max(mouseX, xShift);
    mouseX = Math.min(mouseX, chartWidth + xShift);    
    mouseY = Math.max(mouseY, yShift);
    mouseY = Math.min(mouseY, chartHeight + yShift);
    
    double width = mouseX - start.getX();
    double height = mouseY - start.getY();
    double x = Math.max(0, start.getX() + Math.min(width, 0));
    double y = start.getY() + Math.min(height, 0);
      
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
