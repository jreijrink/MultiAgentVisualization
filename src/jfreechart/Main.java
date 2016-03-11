package jfreechart;

import jfreechart.chart.Chart;
import jfreechart.chart.XYBaseChart;
import jfreechart.chart.FieldCanvas;
import jfreechart.chart.AgentChart;
import java.io.File;
import java.io.IOException;
import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfreechart.chart.XYBaseChart.ChartType;
import jfreechart.object.Turtle;
import jfreechart.settings.ui.FXMLConfigurationController;
import jfreechart.settings.ui.FXMLValueController;
import org.dockfx.DockNode;
import org.dockfx.DockPane;
import org.dockfx.DockPos;
import org.dockfx.NodeManager;
import org.dockfx.events.DockNodeEvent;
import org.dockfx.events.DockNodeEventListenerInterface;

public class Main extends Application {
  private List<Chart> charts;
  private List<Turtle> data;
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
    
    createMenu(stage, scene, root);
    
    createDefaultLayout(scene);
    
    Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
    DockPane.initializeDefaultUserAgentStylesheet();

    scene.getStylesheets().add("jfreechart/plot.css");
    stage.setScene(scene);
    stage.show();
  }
  
  private void  createMenu(Stage stage, Scene scene, BorderPane root) {
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
            List<Turtle> result = parser.parse(file.getAbsolutePath());
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
    
    MenuItem newScatterMenu = new MenuItem("Add scatterchart");
    newScatterMenu.setOnAction(new EventHandler() {
      @Override
      public void handle(Event t) {
        XYBaseChart chart = new XYBaseChart(scene, ChartType.Scatter);
        addChart(dockPane, null, chart);
      }
    });
    elementMenu.getItems().add(newScatterMenu);
    
    MenuItem newLineMenu = new MenuItem("Add linechart");
    newLineMenu.setOnAction(new EventHandler() {
      @Override
      public void handle(Event t) {
        XYBaseChart chart = new XYBaseChart(scene, ChartType.Line);
        addChart(dockPane, null, chart);
      }
    });
    elementMenu.getItems().add(newLineMenu);
    
    MenuItem newAgentMenu = new MenuItem("Add agentchart");
    newAgentMenu.setOnAction(new EventHandler() {
      @Override
      public void handle(Event t) {
        AgentChart chart = new AgentChart(scene);
        addChart(dockPane, null, chart);
      }
    });
    elementMenu.getItems().add(newAgentMenu);
    
    MenuItem newFieldMenu = new MenuItem("Add field");
    newFieldMenu.setOnAction(new EventHandler() {
      @Override
      public void handle(Event t) {
        FieldCanvas field = new FieldCanvas();
        addChart(dockPane, null, field);
      }
    });
    elementMenu.getItems().add(newFieldMenu);
    
    Menu settingsMenu = new Menu("Settings");
    MenuItem mappingMenu = new MenuItem("Datamapping");
    mappingMenu.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN));
    mappingMenu.setOnAction(new EventHandler() {
      @Override
      public void handle(Event t) {
        try {
          FXMLLoader loader = new FXMLLoader(FXMLValueController.class.getResource("FXMLParameters.fxml"));
          AnchorPane page = (AnchorPane) loader.load();
          Stage dialogStage = new Stage();
          dialogStage.setTitle("Datamapping");
          dialogStage.initModality(Modality.WINDOW_MODAL);
          dialogStage.initOwner(stage);
          Scene scene = new Scene(page);
          dialogStage.setScene(scene);

          dialogStage.setResizable(false);
          dialogStage.showAndWait();
          
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

          Scene scene = new Scene(page);
          dialogStage.setScene(scene);

          dialogStage.setResizable(false);
          dialogStage.showAndWait();
          
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
    settingsMenu.getItems().add(configurationMenu);
    
    menuBar.getMenus().addAll(fileMenu, elementMenu, settingsMenu);    
  }
  
  private DockPane createDefaultLayout(Scene scene) {
    
    XYBaseChart scatter = new XYBaseChart(scene, ChartType.Scatter);
    addChart(dockPane, DockPos.TOP, scatter);
        
    XYBaseChart line = new XYBaseChart(scene, ChartType.Line);
    addChart(dockPane, DockPos.TOP, line);
        
    AgentChart agent = new AgentChart(scene);
    addChart(dockPane, DockPos.TOP, agent);
    
    FieldCanvas field = new FieldCanvas();
    addChart(dockPane, DockPos.RIGHT, field);
    
    return dockPane;
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
  
  private void updateLayout(List<Turtle> data) {
    this.data = data;
    
    for (Chart chart : this.charts) {
      chart.updateData(data);
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
