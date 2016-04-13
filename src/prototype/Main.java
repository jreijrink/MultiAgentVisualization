package prototype;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import prototype.chart.Chart;
import prototype.chart.XYBaseChart;
import prototype.chart.FieldCanvas;
import prototype.chart.AgentChart;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.AbstractMap.SimpleEntry;
import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import prototype.chart.XYBaseChart.ChartType;
import prototype.object.Turtle;
import prototype.settings.Configuration;
import prototype.settings.ui.FXMLConfigurationController;
import prototype.settings.ui.FXMLParametersController;
import org.dockfx.DockNode;
import org.dockfx.DockPane;
import org.dockfx.DockPos;
import org.dockfx.NodeManager;
import org.dockfx.events.DockNodeEvent;
import org.dockfx.events.DockNodeEventListenerInterface;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import prototype.chart.CategoricalChart;
import prototype.object.DateComparator;
import prototype.object.LayoutChart;
import prototype.settings.DataMapping;

public class Main extends Application {
  private List<Chart> charts;
  private List<Turtle> data;
  private DockPane dockPane;
  private NodeManager nodeManager;
  private int startIndex;
  private int endIndex;
  private boolean drag;
  private Menu loadMenu;  
  private File currentFile;
  
  @Override
  public void start(Stage stage) {
    stage.setTitle("Prototype");
    
    charts = new ArrayList();
    data = new ArrayList();
    
    BorderPane root = new BorderPane();

    Scene scene = new Scene(root, 800, 500);

    this.dockPane = new DockPane();
    this.nodeManager = new NodeManager(dockPane);
        
    setEventListener();
    
    root.setCenter(dockPane);
    
    createMenu(stage, scene, root);
    
    Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
    DockPane.initializeDefaultUserAgentStylesheet();
    
    scene.getStylesheets().add("prototype/plot.css");
    stage.setScene(scene);
    
    stage.getIcons().add(new Image(Main.class.getClassLoader().getResource("prototype/icon.png").toExternalForm()));
    
    stage.show();
    
    showDefaultLayout(scene);
  }
  
  private void  createMenu(Stage stage, Scene scene, BorderPane root) {
    MenuBar menuBar = new MenuBar();
    menuBar.setUseSystemMenuBar(true);
    menuBar.prefWidthProperty().bind(stage.widthProperty());

    root.setTop(menuBar);
    
    root.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
    
      Timer timer = new java.util.Timer();
      timer.schedule( 
        new java.util.TimerTask() {
          @Override
          public void run() {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                  menuBar.autosize();
                  menuBar.requestLayout();
                  timer.cancel();
                  timer.purge();
                }
              });
            }
          }, 100);
    });

    root.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
      menuBar.autosize();
      menuBar.requestLayout();
    });
    
    Menu fileMenu = fileMenu(stage, scene);
    
    Menu elementMenu = elementMenu(stage, scene);
    
    Menu layoutMenu = layoutMenu(stage, scene);
    
    Menu settingsMenu = settingsMenu(stage, scene);
    
    menuBar.getMenus().addAll(fileMenu, elementMenu, layoutMenu, settingsMenu);
  }
  
  private Menu fileMenu(Stage stage, Scene scene) {    
    Menu fileMenu = new Menu("File");
    MenuItem openMenu = new MenuItem("Open");
    openMenu.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
    openMenu.setOnAction(new EventHandler() {
      @Override
      public void handle(Event t) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open MAT File");
        currentFile = fileChooser.showOpenDialog(stage);
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("MAT files (*.mat)", "*.mat");
        fileChooser.getExtensionFilters().add(extFilter);

        loadData(scene);
      }
    });
    fileMenu.getItems().add(openMenu);
    return fileMenu;
  }
  
  private Menu elementMenu(Stage stage, Scene scene) {
    Menu elementMenu = new Menu("Elements");
    
    MenuItem newScatterMenu = new MenuItem("Add scatter-chart");
    newScatterMenu.setAccelerator(new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.CONTROL_DOWN));
    newScatterMenu.setOnAction(new EventHandler() {
      @Override
      public void handle(Event t) {
        XYBaseChart chart = new XYBaseChart(scene, ChartType.Scatter);
        addChart(dockPane, null, null, chart);
        chart.updateData(data);
      }
    });
    elementMenu.getItems().add(newScatterMenu);
    
    MenuItem newLineMenu = new MenuItem("Add line-chart");
    newLineMenu.setAccelerator(new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.CONTROL_DOWN));
    newLineMenu.setOnAction(new EventHandler() {
      @Override
      public void handle(Event t) {
        XYBaseChart chart = new XYBaseChart(scene, ChartType.Line);
        addChart(dockPane, null, null, chart);
        chart.updateData(data);
      }
    });
    elementMenu.getItems().add(newLineMenu);
    
    MenuItem newAgentMenu = new MenuItem("Add agent-chart");
    newAgentMenu.setAccelerator(new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.CONTROL_DOWN));
    newAgentMenu.setOnAction(new EventHandler() {
      @Override
      public void handle(Event t) {
        AgentChart chart = new AgentChart(scene);
        addChart(dockPane, null, null, chart);
        chart.updateData(data);
      }
    });
    elementMenu.getItems().add(newAgentMenu);
    
    MenuItem newCategoricalMenu = new MenuItem("Add categorical-chart");
    newCategoricalMenu.setAccelerator(new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.CONTROL_DOWN));
    newCategoricalMenu.setOnAction(new EventHandler() {
      @Override
      public void handle(Event t) {
        CategoricalChart chart = new CategoricalChart(scene);
        addChart(dockPane, null, null, chart);
        chart.updateData(data);
      }
    });
    elementMenu.getItems().add(newCategoricalMenu);
    
    MenuItem newFieldMenu = new MenuItem("Add field");
    newFieldMenu.setAccelerator(new KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.CONTROL_DOWN));
    newFieldMenu.setOnAction(new EventHandler() {
      @Override
      public void handle(Event t) {
        FieldCanvas field = new FieldCanvas();
        addChart(dockPane, null, null, field);
        field.updateData(data);
      }
    });
    elementMenu.getItems().add(newFieldMenu);
    
    return elementMenu;
  }
  
  private Menu layoutMenu(Stage stage, Scene scene) {
    Menu layoutMenu = new Menu("Layout");
    
    MenuItem saveMenu = new MenuItem("Save current");
    saveMenu.setOnAction(new EventHandler() {
      @Override
      public void handle(Event t) {
        saveCurrentLayout(scene);
      }
    });
    layoutMenu.getItems().add(saveMenu);
    
    loadMenu = new Menu("Load");
    initLayoutMenu(scene);
    layoutMenu.getItems().add(loadMenu);
    
    MenuItem manageMenu = new MenuItem("Manage");
    layoutMenu.getItems().add(manageMenu);
    
    return layoutMenu;
  }
  
  private void initLayoutMenu(Scene scene) {
    loadMenu.getItems().clear();
    for (File layout : getLayoutNames()) {
      String filename = layout.getName();
      String name = filename.substring(0, filename.indexOf('.'));
      Menu layoutMenu = new Menu(name);
      MenuItem layoutLoad = new MenuItem("Open");
      layoutLoad.setOnAction(new EventHandler() {
        @Override
        public void handle(Event t) {
          SortedMap.Entry<LayoutChart, SortedMap> layout = loadLayout(name + ".json");
          clearLayout();
          createLayout(scene, layout.getKey(), layout.getValue(), null);
          updateLayout(data);
          dockPane.evenDividers();
        }
      });
      MenuItem layoutDelete = new MenuItem("Remove");
      layoutDelete.setOnAction(new EventHandler() {
        @Override
        public void handle(Event t) {
          deleteLayout(scene, name + ".json");
        }
      });
      layoutMenu.getItems().add(layoutLoad);
      layoutMenu.getItems().add(layoutDelete);
      loadMenu.getItems().add(layoutMenu);
    }    
  }
  
  private Menu settingsMenu(Stage stage, Scene scene) {    
    Menu settingsMenu = new Menu("Settings");
    MenuItem mappingMenu = new MenuItem("Data mapping");
    mappingMenu.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN));
    mappingMenu.setOnAction(new EventHandler() {
      @Override
      public void handle(Event t) {
        try {
          FXMLLoader loader = new FXMLLoader(FXMLParametersController.class.getResource("FXMLParameters.fxml"));
          AnchorPane page = (AnchorPane) loader.load();
          Stage dialogStage = new Stage();
          dialogStage.setTitle("Data mapping");
          dialogStage.initModality(Modality.WINDOW_MODAL);
          dialogStage.initOwner(stage);
          Scene dialogScene = new Scene(page);
          dialogStage.setScene(dialogScene);

          dialogStage.setResizable(false);
    
          FXMLParametersController controller = loader.getController();
          controller.setDialogStage(dialogStage);    
    
          dialogStage.showAndWait();
          
          if(controller.hasChanges())
            loadData(scene);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
    settingsMenu.getItems().add(mappingMenu);
    
    MenuItem configurationMenu = new MenuItem("Configuration");
    configurationMenu.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));
    configurationMenu.setOnAction(new EventHandler() {
      @Override
      public void handle(Event t) {
        try {
          FXMLLoader loader = new FXMLLoader(FXMLConfigurationController.class.getResource("FXMLConfiguration.fxml"));
          AnchorPane page = (AnchorPane) loader.load();
          Stage dialogStage = new Stage();
          dialogStage.setTitle("Configuration");
          dialogStage.initModality(Modality.WINDOW_MODAL);
          dialogStage.initOwner(stage);

          FXMLConfigurationController controller = loader.getController();
          controller.setDialogStage(dialogStage);

          Scene dialogScene = new Scene(page);
          dialogStage.setScene(dialogScene);

          dialogStage.setResizable(false);
          dialogStage.showAndWait();
          
          if(controller.hasChanges())
            loadData(scene);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
    settingsMenu.getItems().add(configurationMenu);
    
    return settingsMenu;
  }
  
  private void loadData(Scene scene) {
    if (currentFile != null && configurationComplete()) {
      try {
        //scene.getRoot().setCursor(Cursor.WAIT);
        Parser parser = new Parser();
        List<Turtle> result = parser.parse(currentFile.getAbsolutePath());
        updateLayout(result);
      } catch (Exception ex) {
        ex.printStackTrace();
        
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Open file");
        alert.setHeaderText("Something went wrong.");
        alert.setContentText(ex.getMessage());
        alert.showAndWait();
      } finally {
        //scene.getRoot().setCursor(Cursor.DEFAULT);
      }
    }    
  }
  
  private boolean configurationComplete() {
    if(!(new Configuration()).complete()) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setTitle("Configuration error");
      alert.setHeaderText("The configuration is incomplete!");
      alert.setContentText("Please verify the configuration.");
      alert.show();
      return false;
    }
    return true;
  }
  
  private void saveCurrentLayout(Scene scene) {
    
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Please enter a name");
    dialog.setHeaderText("Enter the name of the layout");
    dialog.setContentText("Layout name:");

    Optional<String> result = dialog.showAndWait();
    if (result.isPresent()){
      saveLayout(result.get() + ".json");
      initLayoutMenu(scene);
    }
  }
  
  private void saveLayout(String name) {    
    try
    {
      Map<DockNode, Map> nodeStructure = new HashMap();
      
      List<DockNode> loopList = new ArrayList();
      for(DockNode node : nodeManager.getDockNodes()) {
        loopList.add(node);
      }
      
      while(loopList.size() > 0) {
        DockNode selected = null;
        Node sibling = null;
        
        for(DockNode node : loopList) {
          if(!nodeManager.getDockNodes().contains(node.getDockSibling())) {
            //Found a root node
            selected = node;
            break;
          } else {
            //This node has a sibling node, check if siblin in map
            if(nodeInMap(nodeStructure, node.getDockSibling())) {
              //Only add this node if the sibling is present in the map
              sibling = node.getDockSibling();
              selected = node;
              break;
            }
          }
        }
        
        if(selected != null) {
          if(sibling == null) {
          loopList.remove(selected);
          nodeStructure.put(selected, new HashMap());
          } else {
            loopList.remove(selected);
            insertInMap(nodeStructure, sibling, selected);
          }
        } else {
          break;
        }
      }
      
      JSONObject layoutObject = createJSONMap(null, nodeStructure);
      
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      JsonParser jp = new JsonParser();
      JsonElement je = jp.parse(layoutObject.toJSONString());
      String prettyJsonString = gson.toJson(je);
      
      FileWriter file = new FileWriter(getLayoutPath(name));
      file.write(prettyJsonString);
      file.close();      
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  
  private void deleteLayout(Scene scene, String name) {
    try
    {
      Alert alert = new Alert(AlertType.CONFIRMATION);
      alert.setTitle("Confirmation Dialog");
      alert.setHeaderText("Remove layout " + name + "?");
      alert.setContentText("Sure about the removal?");

      Optional<ButtonType> result = alert.showAndWait();
      if (result.get() == ButtonType.OK){
        File file = getLayoutPath(name);
        if(file.exists())
          file.delete();
        initLayoutMenu(scene);
      }
    } catch (Exception e) {
        e.printStackTrace();
    }
  }
  
  private SortedMap.Entry<LayoutChart, SortedMap> loadLayout(String name) {    
    try
    {
      JSONParser parser = new JSONParser();
    
      Object obj = parser.parse(new FileReader(getLayoutPath(name)));

      JSONObject jsonObject = (JSONObject) obj;
      return loadJSON(jsonObject);
      
    } catch (Exception e) {
        e.printStackTrace();
    }
    
    return null;
  }
  
  private List<File> getLayoutNames() {
    try {
      File path = getLayoutPath("");      
      File[] files = getWithExtension(path, ".json");
      return Arrays.asList(files);
    } catch (Exception ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    }
    return new ArrayList();
  }
  
  private SortedMap.Entry<LayoutChart, SortedMap> loadDefaultLayout() {
    return loadLayout("default");
  }
  
  private File[] getWithExtension(File path, String extension) {
    return path.listFiles((File dir, String filename) -> filename.endsWith(extension));
  }
  
  private SortedMap.Entry<LayoutChart, SortedMap> loadJSON(JSONObject root) {
    
    String type = "";
    if(root.containsKey("chart"))
      type = (String)root.get("chart");    
    String pos = "";
    if(root.containsKey("pos"))
      pos = (String)root.get("pos");    
    String time = "";
    if(root.containsKey("time"))
      time = (String)root.get("time");
    int[] selectedTurtles = new int[0];
    if(root.containsKey("selectedTurtles"))
      selectedTurtles = jsonToArray((JSONArray)root.get("selectedTurtles"));
    boolean liveUpdate = false;
    if(root.containsKey("liveUpdate"))
      liveUpdate = (boolean)root.get("liveUpdate");
    
    String parameter = "";
    if(root.containsKey("parameter"))
      parameter = (String)root.get("parameter");
    int parameterIndex = 0;
    if(root.containsKey("parameterIndex"))
      parameterIndex = (int)(long)root.get("parameterIndex");
    String parameterValue = "";
    if(root.containsKey("parameterValue"))
      parameterValue = (String)root.get("parameterValue");
    
    boolean turtleHistory = false;
    if(root.containsKey("turtleHistory"))
      turtleHistory = (boolean)root.get("turtleHistory");
    int[] selectedBall = new int[0];
    if(root.containsKey("selectedBall"))
      selectedBall = jsonToArray((JSONArray)root.get("selectedBall"));
    boolean ballHistory = false;
    if(root.containsKey("ballHistory"))
      ballHistory = (boolean)root.get("ballHistory");
    int[] selectedOpponents = new int[0];
    if(root.containsKey("selectedOpponents"))
      selectedOpponents = jsonToArray((JSONArray)root.get("selectedOpponents"));
    boolean opponentsHistory = false;
    if(root.containsKey("opponentsHistory"))
      opponentsHistory = (boolean)root.get("opponentsHistory");
    
    LayoutChart node = new LayoutChart(type, pos, time, selectedTurtles, liveUpdate,
                                      parameter, parameterIndex, parameterValue,
                                      turtleHistory, selectedBall, ballHistory, selectedOpponents, opponentsHistory);
    
    Comparator comparator = new DateComparator();
    SortedMap<LayoutChart, SortedMap> children = new TreeMap(comparator);
    JSONArray childList = (JSONArray)root.get("charts");
    for(Object child : childList) {
      SortedMap.Entry<LayoutChart, SortedMap> entry = loadJSON((JSONObject)child);
      children.put(entry.getKey(), entry.getValue());
    }
    
    return new SimpleEntry(node, children);
  }
  
  private boolean nodeInMap(Map<DockNode, Map> map, Node node) {
    for(Map.Entry<DockNode, Map> entry : map.entrySet()) {
      if (entry.getKey() == node)
        return true;
      if(nodeInMap(entry.getValue(), node))
        return true;
    }
    return false;
  }
  
  private void insertInMap(Map<DockNode, Map> map, Node sibling, DockNode node) {
    Map<DockNode, Map> siblingMap = getSiblingMap(map, sibling);
    if(siblingMap != null)
      siblingMap.put(node, new HashMap());
  }
  
  private Map<DockNode, Map> getSiblingMap(Map<DockNode, Map> map, Node sibling) {
    for(Map.Entry<DockNode, Map> entry : map.entrySet()) {
      if (entry.getKey() == sibling) {
        return entry.getValue();
      } else {
        Map<DockNode, Map> found = getSiblingMap(entry.getValue(), sibling);
        if(found != null) 
          return found;
      }
    }
    return null;
  }
  
  private JSONObject createJSONMap(DockNode node, Map<DockNode, Map> map) {
    JSONObject root = new JSONObject();
    JSONArray childList = new JSONArray();

    if(node != null) {
      Chart chart = (Chart)node.getUserData();
      root.put("chart", chart.getName());
      
      if(chart.getClass() ==  FieldCanvas.class) {
        FieldCanvas implChart = (FieldCanvas)chart;
      root.put("liveUpdate", implChart.liveUpdate);
        root.put("selectedTurtles", arrayToJson(implChart.selectedTurtles));
      root.put("turtleHistory", implChart.turtleHistory);
      root.put("selectedBall", arrayToJson(implChart.selectedBall));
      root.put("ballHistory", implChart.ballHistory);
      root.put("selectedOpponents", arrayToJson(implChart.selectedOpponents));
      root.put("opponentsHistory", implChart.opponentsHistory);
        
      }
      if(chart.getClass() ==  AgentChart.class) {
        AgentChart implChart = (AgentChart)chart;
        root.put("liveUpdate", implChart.liveUpdate);
        root.put("selectedTurtles", arrayToJson(implChart.selectedTurtles));
        root.put("parameter", implChart.parameter);
        root.put("parameterIndex", implChart.parameterIndex);
        root.put("parameterValue", implChart.parameterValue);        
      }
      if(chart.getClass() ==  CategoricalChart.class) {
        CategoricalChart implChart = (CategoricalChart)chart;
        root.put("liveUpdate", implChart.liveUpdate);
        root.put("selectedTurtles", arrayToJson(implChart.selectedTurtles));
        root.put("parameter", implChart.parameter);
        root.put("parameterIndex", implChart.parameterIndex);
        root.put("parameterValue", implChart.parameterValue);
        
      }
      if(chart.getClass() ==  XYBaseChart.class) {
        XYBaseChart implChart = (XYBaseChart)chart;
        root.put("liveUpdate", implChart.liveUpdate);
        root.put("selectedTurtles", arrayToJson(implChart.selectedTurtles));
        root.put("parameter", implChart.parameter);
        root.put("parameterIndex", implChart.parameterIndex);
        root.put("parameterValue", implChart.parameterValue);
      }
      
      if(node.isFloating()) {
        root.put("pos", "FLOAT");
      } else {
        DockPos pos = node.getDockPos();
        if(pos != null) {
          LocalDateTime datetime = node.getDockTime();
          root.put("pos", pos.toString());
          root.put("time", datetime.toString());
        }
      }
    }

    for(Map.Entry<DockNode, Map> entry : map.entrySet()) {
      childList.add(createJSONMap(entry.getKey(), entry.getValue()));
    }
    
    root.put("charts", childList);
    
    return root;    
  }
  
  private JSONArray arrayToJson(int[] array) {
    JSONArray jsonArray = new JSONArray();
    for(int i = 0; i < array.length; i++) {
      jsonArray.add(array[i]);
    }
    return jsonArray;
  }
  
  private int[] jsonToArray(JSONArray json) {
    int[] array = new int[json.size()];
    for(int i = 0; i < json.size(); i++) {
      array[i] = (int)(long)json.get(i);
    }
    return array;
  }
  
  private void showDefaultLayout(Scene scene) {
    try {
      SortedMap.Entry<LayoutChart, SortedMap> layout = loadDefaultLayout();
      clearLayout();
      createLayout(scene, layout.getKey(), layout.getValue(), null);
      updateLayout(this.data);
      dockPane.evenDividers();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  private void clearLayout() {
    this.charts.clear();
    while(this.nodeManager.getDockNodes().size() > 0) {
      DockNode child = this.nodeManager.getDockNodes().get(0);
      child.close();
    }
    this.dockPane.clearRoot();
  }
  
  private void createLayout(Scene scene, LayoutChart root, SortedMap<LayoutChart, SortedMap> children, DockNode sibling) {
    
    Chart newChart = root.GetChart(scene, new ArrayList());    
    DockPos position = root.GetPosition();
    
    DockNode newNode = null;
    if(newChart != null)
      newNode = addChart(dockPane, position, sibling, newChart);
    
    //SORT ON TIME
    for(Map.Entry<LayoutChart, SortedMap> entry : children.entrySet()) {
      createLayout(scene, entry.getKey(), entry.getValue(), newNode);
    }
  }
  
  private void setEventListener() {
    nodeManager.addEventListener(new DockNodeEventListenerInterface() {
      @Override public void dockNodeClosed(DockNodeEvent e) {
        Chart chart = (Chart)e.getSource().getUserData();
        charts.remove(chart);
      }

      @Override
      public void dockNodeSettings(DockNodeEvent e) {
        Chart chart = (Chart)e.getSource().getUserData();
        chart.showParameterDialog();
        saveLayout("default");
      }

      @Override
      public void dockUpdated(DockNodeEvent e) {
        saveLayout("default");
      }
      
      @Override public void dockNodeMaximized(DockNodeEvent e) {}
      @Override public void dockNodeWindowed(DockNodeEvent e) {}
      @Override public void dockNodeMinimized(DockNodeEvent e) {}
      @Override public void dockNodeRestored(DockNodeEvent e) {}
      @Override public void dockNodeDocked(DockNodeEvent e) {}
      @Override public void dockNodeFloated(DockNodeEvent e) {}
      @Override public void dockNodeFocused(DockNodeEvent e) {}
      @Override public void dockNodeDefocused(DockNodeEvent e) {}
    });
  }
    
  private DockNode addChart(DockPane dockPane, DockPos position, DockNode sibling, Chart chart) {
    this.charts.add(chart);

    chart.selectFrames(this.startIndex, this.endIndex, this.drag);
    
    chart.addSelectionEventListener((int startIndex, int endIndex, boolean drag) -> {
      for (Chart exisintg : this.charts) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.drag = drag;
        exisintg.selectFrames(startIndex, endIndex, drag);
      }
    });
    
    Image dockImage = new Image(DockPane.class.getResource("docknode.png").toExternalForm());
    
    DockNode chartDock = nodeManager.getDockNode(chart.getNode(), chart.getName(), new ImageView(dockImage));
    chartDock.setUserData(chart);
    chartDock.setPrefSize(500, 1500);
    chartDock.floatingProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        boolean floating = (Boolean)newValue;
        if(floating)          
          chartDock.setPrefSize(500, 500);
        else
          chartDock.setPrefSize(500, 1500);
      }
    });
    chart.setDockNode(chartDock);
    
    if(position == null) {
      chartDock.floatNode(dockPane, true);
    } else {
      if(sibling == null) {
        chartDock.dock(dockPane, position);
      } else {
        chartDock.dock(dockPane, position, sibling);
      }
    }
    
    chartDock.autosize();
    
    return chartDock;
  }
  
  private void updateLayout(List<Turtle> data) {
    this.data = data;
    
    for (Chart chart : this.charts) {
      chart.updateData(data);
    }
  }
  
  private static File getLayoutPath(String filename) throws Exception {
    
    URL url = DataMapping.class.getProtectionDomain().getCodeSource().getLocation();
    String jarPath = new File(url.toURI()).getParentFile() + File.separator + "layouts";
        
    File jarLayoutPath = new File(jarPath);
    if(jarLayoutPath.exists())
      return new File(jarPath + File.separator + filename);
    
    File localFile = new File("layouts");
    if(localFile.exists())
      return new File("layouts" + File.separator + filename);
    
    jarLayoutPath.mkdirs();
    
    return new File(jarPath + File.separator + filename);
  }

  public static void main(String[] args) {
    launch(args);
  }
}
