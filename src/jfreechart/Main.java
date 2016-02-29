/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfreechart;

import javafx.scene.image.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
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
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.dockfx.DockNode;
import org.dockfx.DockPane;
import org.dockfx.DockPos;

public class Main extends Application {
  private List<FieldCanvas> fields;
  private List<ScatterPlot> plots;
  private List<AgentPlot> agents;  
  private List<TimeFrame> data;
  
  @Override
  public void start(Stage stage) {
    stage.setTitle("Prototype");

    fields = new ArrayList();
    agents = new ArrayList();
    plots = new ArrayList();
    data = new ArrayList();
    
    BorderPane root = new BorderPane();

    Scene scene = new Scene(root, 900, 600, Color.WHITE);

    DockPane dockPane = createDefaultLayout(scene);
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
    
    Menu elementMenu = new Menu("Elements");
    MenuItem newFieldMenu = new MenuItem("Add field");
    newFieldMenu.setOnAction(new EventHandler() {
      @Override
      public void handle(Event t) {
        addField(scene, dockPane);
      }
    });
    elementMenu.getItems().add(newFieldMenu);
    MenuItem newChartMenu = new MenuItem("Add chart");
    newChartMenu.setOnAction(new EventHandler() {
      @Override
      public void handle(Event t) {
        addChart(scene, dockPane);
      }
    });
    elementMenu.getItems().add(newChartMenu);
    
    menuBar.getMenus().addAll(fileMenu, elementMenu);

    Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
    DockPane.initializeDefaultUserAgentStylesheet();

    scene.getStylesheets().add("jfreechart/plot.css");
    stage.setScene(scene);
    stage.show();
  }

  private DockPane createDefaultLayout(Scene scene) {
    DockPane dockPane = new DockPane();
    
    Image dockImage = new Image(DockPane.class.getResource("docknode.png").toExternalForm());
    
    FieldCanvas newField = new FieldCanvas(new ArrayList(), new int[]{0}, "ball");
    fields.add(newField);
    
    plots.add(new ScatterPlot(scene, new int[]{0}, "batteryVoltage", 0, new ArrayList()));
    plots.add(new ScatterPlot(scene, new int[]{1}, "batteryVoltage", 0, new ArrayList()));
    //plots.add(new ScatterPlot(scene, new int[]{2}, "batteryVoltage", 0, new ArrayList()));
    //plots.add(new ScatterPlot(scene, new int[]{3}, "batteryVoltage", 0, new ArrayList()));
    
    AgentPlot agentPlot = new AgentPlot(scene, new ArrayList());
    agents.add(agentPlot);
    
    for (ScatterPlot scatterPlot : plots) {
      scatterPlot.addSelectionEventListener((int startIndex, int endIndex) -> {
        for (ScatterPlot plot : plots) {
          plot.selectFrames(startIndex, endIndex);
        }
        for (FieldCanvas field : fields) {
          field.selectFrames(startIndex, endIndex);
        }
      });
      
      DockNode chartDock = new DockNode(scatterPlot.getChart(), "Scatterchart", new ImageView(dockImage));
      dockPane.dock(chartDock, DockPos.TOP);
    }
    
    DockNode agentDock = new DockNode(agentPlot.getChart(), "AgentPlot", new ImageView(dockImage));
    dockPane.dock(agentDock, DockPos.TOP);

    DockNode fieldDock = new DockNode(newField, "Field", new ImageView(dockImage));
    fieldDock.setPrefSize(100, 100);
    dockPane.dock(fieldDock, DockPos.RIGHT);
    
    
    return dockPane;
  }
  
  private void addField(Scene scene, DockPane dockPane) {
    FieldCanvas field = new FieldCanvas(data, new int[]{0}, "ball");
    fields.add(field);
    
    Image dockImage = new Image(DockPane.class.getResource("docknode.png").toExternalForm());
    DockNode fieldDock = new DockNode(field, "Field", new ImageView(dockImage));
    dockPane.dock(fieldDock, DockPos.RIGHT);
  }
  
  private void addChart(Scene scene, DockPane dockPane) {
    ScatterPlot newPlot = new ScatterPlot(scene, new int[]{0}, "batteryVoltage", 0, data);
    plots.add(newPlot);

    newPlot.addSelectionEventListener((int startIndex, int endIndex) -> {
      for (ScatterPlot plot : plots) {
        plot.selectFrames(startIndex, endIndex);
      }
      for (FieldCanvas field : fields) {
      field.selectFrames(startIndex, endIndex);
      }
    });
    
    Image dockImage = new Image(DockPane.class.getResource("docknode.png").toExternalForm());
    DockNode chartDock = new DockNode(newPlot.getChart(), "Scatterchart", new ImageView(dockImage));
    dockPane.dock(chartDock, DockPos.LEFT);
  }

  private void updateLayout(List<TimeFrame> data) {
    this.data = data;
    
    for (ScatterPlot scatterPlot : plots) {
      scatterPlot.updateData(data);
    }
    for (FieldCanvas field : fields) {
      field.updateData(data);      
    }
    for (AgentPlot agentPlot : agents) {
      agentPlot.updateData(data);      
    }
    
  }

  public static void main(String[] args) {
    launch(args);
  }
}
