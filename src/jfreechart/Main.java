/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfreechart;

import java.io.File;
import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.dockfx.DockNode;
import org.dockfx.DockPane;
import org.dockfx.DockPos;
import org.dockfx.NodeManager;
import org.dockfx.events.DockNodeEvent;
import org.dockfx.events.DockNodeEventListenerInterface;

public class Main extends Application {
  private List<Chart> charts;
  private List<TimeFrame> data;
  private DockPane dockPane;
  private NodeManager nodeManager;
  private int startIndex;
  private int endIndex;
  private boolean drag;
  
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
    
    MenuBar menuBar = new MenuBar();
    menuBar.setUseSystemMenuBar(true);
    menuBar.prefWidthProperty().bind(stage.widthProperty());

    root.setTop(menuBar);

    
    Menu fileMenu = new Menu("File");
    MenuItem openMenu = new MenuItem("Open");
    openMenu.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
    openMenu.setOnAction(new EventHandler() {
      @Override
      public void handle(Event t) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open MAT File");
        File file = fileChooser.showOpenDialog(stage);
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("MAT files (*.mat)", "*.mat");
        fileChooser.getExtensionFilters().add(extFilter);

        if (file != null) {
          try {
            Parser parser = new Parser();
            List<TimeFrame> result = parser.parse(file.getAbsolutePath());
            updateLayout(result);
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        }
      }
    });
    fileMenu.getItems().add(openMenu);
    
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
    
    Menu elementMenu = new Menu("Elements");
    
    MenuItem newScatterMenu = new MenuItem("Add scatterplot");
    newScatterMenu.setOnAction(new EventHandler() {
      @Override
      public void handle(Event t) {
        ScatterPlot plot = new ScatterPlot(scene, new int[]{ 0 }, "batteryVoltage", 0, new ArrayList());
        addChart(dockPane, null, plot);
      }
    });
    elementMenu.getItems().add(newScatterMenu);
    
    MenuItem newLineMenu = new MenuItem("Add lineplot");
    newLineMenu.setOnAction(new EventHandler() {
      @Override
      public void handle(Event t) {
        LinePlot plot = new LinePlot(scene, new int[]{ 0 }, "batteryVoltage", 0, new ArrayList());
        addChart(dockPane, null, plot);
      }
    });
    elementMenu.getItems().add(newLineMenu);
    
    MenuItem newAgentMenu = new MenuItem("Add agentplot");
    newAgentMenu.setOnAction(new EventHandler() {
      @Override
      public void handle(Event t) {
        AgentPlot plot = new AgentPlot(scene, new int[]{ 0, 1, 2, 3, 4 }, new ArrayList());
        addChart(dockPane, null, plot);
      }
    });
    elementMenu.getItems().add(newAgentMenu);
    
    MenuItem newFieldMenu = new MenuItem("Add field");
    newFieldMenu.setOnAction(new EventHandler() {
      @Override
      public void handle(Event t) {
        FieldCanvas field = new FieldCanvas(new ArrayList(), new int[]{0, 1, 2, 3, 4 }, "pose");
        addChart(dockPane, null, field);
      }
    });
    elementMenu.getItems().add(newFieldMenu);
            
    menuBar.getMenus().addAll(fileMenu, elementMenu);

    createDefaultLayout(scene);
    
    Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
    DockPane.initializeDefaultUserAgentStylesheet();

    scene.getStylesheets().add("jfreechart/plot.css");
    stage.setScene(scene);
    stage.show();
  }
  
  private DockPane createDefaultLayout(Scene scene) {
    
    ScatterPlot scatter = new ScatterPlot(scene, new int[]{0}, "batteryVoltage", 0, new ArrayList());
    addChart(dockPane, DockPos.TOP, scatter);
    
    LinePlot line = new LinePlot(scene, new int[]{0}, "cpu0Load", 0, new ArrayList());
    addChart(dockPane, DockPos.TOP, line);
    
    AgentPlot agent = new AgentPlot(scene, new int[]{ 0, 1, 2, 3, 4, 5 }, new ArrayList());
    addChart(dockPane, DockPos.TOP, agent);
    
    FieldCanvas field = new FieldCanvas(new ArrayList(), new int[]{0, 1, 2, 3, 4, 5 }, "pose");
    addChart(dockPane, DockPos.RIGHT, field);
    
    return dockPane;
  }
  
  private void setEventListener() {
    nodeManager.addEventListener(new DockNodeEventListenerInterface() {
      @Override public void dockNodeClosed(DockNodeEvent e) {
        Chart chart = (Chart)e.getSource().getUserData();
        charts.remove(chart);
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
    
  private void addChart(DockPane dockPane, DockPos position, Chart chart) {
    this.charts.add(chart);

    chart.updateData(this.data);
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
    chartDock.setPrefSize(500, 500);
    
    if(position == null)
      chartDock.floatNode(dockPane, true);
    else
      chartDock.dock(dockPane, position);
  }
  
  private void updateLayout(List<TimeFrame> data) {
    this.data = data;
    
    for (Chart chart : this.charts) {
      chart.updateData(data);
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
