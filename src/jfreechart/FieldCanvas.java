/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfreechart;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;
import static jfreechart.Parser.MAX_TURTLES;

public class FieldCanvas extends Pane {
  private static final double FIELDWIDTH = 11.880000;
  private static final double FIELDLENGTH = 17.880000;
  private static final double PENALTYAREAWIDTH = 6.370000;
  private static final double PENALTYAREALENGTH = 2.125000;
  private static final double GOALAREAWIDTH = 3.370000;
  private static final double GOALAREALENGTH = 0.625000;
  private static final double PENALTYSPOT = 2.870000;
  private static final double GOALWIDTH = 2.000000;
  private static final double GOALDEPTH = 0.575000;

  private List<TimeFrame> data;
  private List<TimeFrame> selection;
  private Rectangle field;
  private int[] selectedRobots;
  private String selectedParameter;
  
  private Canvas canvas;
  
  public FieldCanvas(List<TimeFrame> data,  int[] defaultRobots, String defaultParameter) {
    this.data = data;
    this.selectedRobots = defaultRobots;
    this.selectedParameter = defaultParameter;
    
    selection = new ArrayList();

    setStyle("-fx-background-color: white;");
    
    canvas = new Canvas(getWidth(), getHeight()); 
    getChildren().add(canvas);

    widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
      canvas.setWidth(newValue.intValue());
      field = getField();
    });

    heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
      canvas.setHeight(newValue.intValue());
      field = getField();
    });
    
    this.setOnMouseClicked((MouseEvent event) -> {
      showparameterDialog();
    });
  }
  
  public void updateData(List<TimeFrame> data) {
    this.data = data;
    selection = new ArrayList();
    this.requestLayout();
  }
  
  public void selectFrames(int startIndex, int endIndex) {
    this.selection = data.subList(startIndex, endIndex);
        
    this.requestLayout();
  }
  
  @Override
  protected void layoutChildren() {
    super.layoutChildren(); 

    GraphicsContext gc = canvas.getGraphicsContext2D(); 
    gc.save();
    gc.clearRect(0, 0, getWidth(), getHeight());
   
    drawField(gc);
    
    drawHistoryLines(gc);
    
    drawOpponents(gc);
    
    drawturtles(gc);
    
    drawBall(gc);
  }

  private void showparameterDialog() {
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(10, 10, 10, 10));

    Dialog<Pair<String, int[]>> parameterDialog = new Dialog();
    parameterDialog.setTitle("Parameter Choice Dialog");
    parameterDialog.setHeaderText("Parameter Choice Dialog");
    parameterDialog.setContentText("Choose parameter:");
    parameterDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    ChoiceBox<String> parameterChoiceBox = new ChoiceBox();
    ObservableList<String> list = FXCollections.observableArrayList();
    list.add("pose");
    list.add("ball");
    list.add("opponent");
    parameterChoiceBox.setItems(list);
    parameterChoiceBox.getSelectionModel().select(selectedParameter);

    ListView<StringValuePair<String, Integer>> turtleListView = new ListView();              
    ObservableList<StringValuePair<String, Integer>> turtleList = FXCollections.observableArrayList();
    turtleListView.setItems(turtleList);
    for(int i = 0; i < MAX_TURTLES; i++) {
      turtleList.add(new StringValuePair(String.format("Turtle %d", i+1), i));
    }

    turtleListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    for(int selection : selectedRobots) {
      turtleListView.getSelectionModel().select(selection);
    }        
    turtleListView.setPrefHeight(200);

    grid.add(new Label("Parameter:"), 0, 0);
    grid.add(parameterChoiceBox, 1, 0);
    grid.add(new Label("Turtle:"), 0, 1);
    grid.add(turtleListView, 1, 1);

    parameterDialog.getDialogPane().setContent(grid);

    parameterDialog.setResultConverter(dialogButton -> {
      if(dialogButton == ButtonType.OK) {
        String selectedParameter = parameterChoiceBox.getSelectionModel().getSelectedItem();

        ObservableList<StringValuePair<String, Integer>> selectedItems = turtleListView.getSelectionModel().getSelectedItems();
        int[] results = new int[selectedItems.size()];

        for(int i = 0; i < selectedItems.size(); i++) {
          results[i] = selectedItems.get(i).getValue();
        }

        return new Pair(selectedParameter, results);
      }
      return null;
    });

    Optional<Pair<String, int[]>> result = parameterDialog.showAndWait();

    if(result.isPresent() && result.get() != null && result.get().getValue().length > 0 && result.get().getValue() != selectedRobots) {
      selectedRobots = result.get().getValue();
      selectedParameter = result.get().getKey();
      requestLayout();
    }
  }
  
  private void drawturtles(GraphicsContext g) {
    if(selection.size() > 0) {
      for(Turtle turtle : selection.get(selection.size() - 1).getTurtles()) {
        ParameterMap paremeters = turtle.getParameters();
        if(paremeters.getValue("robotInField")[0][0] > 0) {
          double[][] position = paremeters.getValue("pose");
          Point turtlePos = getPosition(field, position[0][0], position[0][1]);
          double orientation =  position[0][2];
          
          g.save(); 
          g.setStroke(Color.BLACK);
          g.setFill(Color.BLACK);
          g.translate(turtlePos.getX(), turtlePos.getY()); 
          g.rotate(Math.toDegrees(orientation));
          g.fillRect(-10, -10, 20, 20);
          g.setStroke(Color.WHITE);
          g.setFill(Color.WHITE);
          g.strokeText(String.valueOf(turtle.getID() + 1), -5, 5);
          g.restore();          
        }
      }
    }
  }
  
  private void drawOpponents(GraphicsContext g) {
    
    Map<String, List<double[]>> opponentsPositions = new HashMap();
    
    if(selection.size() > 0) {
      for(Turtle turtle : selection.get(selection.size() - 1).getTurtles()) {
        ParameterMap paremeters = turtle.getParameters();
        if(paremeters.getValue("robotInField")[0][0] > 0) {
          double[][] opponents = paremeters.getValue("opponent");
          double[][] opponentLabels = paremeters.getValue("opponentlabelnumber");
          for(int i = 0; i < opponents.length; i++) {
            if(opponents[i][0] != 0 || opponents[i][1] != 0) {
              String label = String.valueOf((int)opponentLabels[i][0] + 1);
              if(!opponentsPositions.containsKey(label)) {
                opponentsPositions.put(label, new ArrayList());
              }
              opponentsPositions.get(label).add(opponents[i]);
            }
          }
        }
      }
    }
    
    for(String key : opponentsPositions.keySet()) {
      double[] position = averagePoint(opponentsPositions.get(key));
      
      Point opponentPos = getPosition(field, position[0], position[1]);
      g.setStroke(Color.PINK);
      g.setFill(Color.PINK);
      g.fillRect(opponentPos.getX() - 10, opponentPos.getY() - 10, 20, 20);
      g.setStroke(Color.BLACK);
      g.setFill(Color.BLACK);
      g.strokeText(key, opponentPos.getX() - 5, opponentPos.getY() + 5);
    }
  }
  
  private void drawBall(GraphicsContext g) {
    List<double[]> ballPositions = new ArrayList();
    if(selection.size() > 0) {
      for(Turtle turtle : selection.get(selection.size() - 1).getTurtles()) {
        ParameterMap paremeters = turtle.getParameters();
        if(paremeters.getValue("robotInField")[0][0] > 0) {
          if(paremeters.getValue("ballFound")[0][0] == 1) {
            double[][] ball = paremeters.getValue("ball");
            ballPositions.add(ball[0]);
          }
        }
      }
    }
    
    if(ballPositions.size() > 0 ) {
      double[] ball = averagePoint(ballPositions);    
      Point ballPos = getPosition(field, ball[0], ball[1]);
      g.setStroke(Color.ORANGE);
      g.setFill(Color.ORANGE);
      g.fillOval(ballPos.getX() - 10, ballPos.getY() - 10, 20, 20);
    }
  }
  
  private void drawHistoryLines(GraphicsContext g) {
    if(selection.size() > 0) {
      List<List<Point>> paths = new ArrayList();
      
      for(int turtleIndex : selectedRobots) {
        
        
        ParameterMap initalCount = selection.get(0).getTurtles().get(turtleIndex).getParameters();
        int size = initalCount.getValue(selectedParameter).length;
        List<Point>[] newPaths = new List[size];
        for(int i = 0; i < size; i++) {
          newPaths[i] = new ArrayList();
        }

        for(int frameIndex = 0; frameIndex < selection.size(); frameIndex++) {
          
          Turtle turle = selection.get(frameIndex).getTurtles().get(turtleIndex);
          ParameterMap paremeters = turle.getParameters();
          double[][] value = paremeters.getValue(selectedParameter);
          
          for(int i = 0; i < value.length; i++) {
            if(value[i][0] != 0 || value[i][1] != 0) {
              Point position = getPosition(field, value[i][0], value[i][1]);
              newPaths[i].add(position);
            }
          }
        }
        
        paths.addAll(Arrays.asList(newPaths));
      }
      
      g.setStroke(Color.LIGHTGRAY);
      g.setLineWidth(4);

      for(List<Point> path : paths) {
        if(path.size() > 0) {
          g.beginPath();
          g.moveTo(path.get(0).x, path.get(0).y);
          for(Point point : path) {
            g.lineTo(point.x, point.y);
          }
          g.stroke();
          g.closePath();
        }
      }
      
      g.setLineWidth(1);
    }
  }
  
  private double[] averagePoint(List<double[]> positions) {
    double x = 0;
    double y = 0;
    double counter = 0;
    for(double[] position : positions) {
      x += position[0];
      y += position[1];
      counter++;
    }
    x = x / counter;
    y = y / counter;
    
    return new double[] { x, y };
  }
  
  private Point getPosition(Rectangle field, double center_x, double center_y) {
    
    double field_center_x = (double)field.getWidth() / 2;
    double field_center_y = (double)field.getHeight() / 2;
    
    double widthRatio = (double)field.getWidth()  / (double)FIELDWIDTH;
    double heightRatio = (double)field.getHeight() / (double)FIELDLENGTH;
    
    double center_x_in_field = center_x * widthRatio;
    double center_y_in_field = center_y * heightRatio;
    
    return new Point((int)(field_center_x + center_x_in_field), (int)(field_center_y + center_y_in_field));
  }
  
  private void drawFillRectangleInField(GraphicsContext g, double x, double y, double width, double height) {
    double widthRatio = (double)field.getWidth() / (double)FIELDWIDTH;
    double heightRatio = (double)field.getHeight() / (double)FIELDLENGTH;
    
    double x_in_field = x * widthRatio;
    double y_in_field = y * heightRatio;
    double width_in_field = width * widthRatio;
    double height_in_field = height * heightRatio;
    
    g.fillRect((int)x_in_field, (int)y_in_field, (int)width_in_field, (int)height_in_field);
  }
  
  private void drawRectangleInField(GraphicsContext g, double x, double y, double width, double height) {
    double widthRatio = (double)field.getWidth() / (double)FIELDWIDTH;
    double heightRatio = (double)field.getHeight() / (double)FIELDLENGTH;
    
    double x_in_field = x * widthRatio;
    double y_in_field = y * heightRatio;
    double width_in_field = width * widthRatio;
    double height_in_field = height * heightRatio;
    
    g.strokeRect((int)x_in_field, (int)y_in_field, (int)width_in_field, (int)height_in_field);
  }
  
  private void drawFillOvalInField(GraphicsContext g, double x, double y, double width, double height) {
    double widthRatio = (double)field.getWidth() / (double)FIELDWIDTH;
    double heightRatio = (double)field.getHeight() / (double)FIELDLENGTH;
    
    double x_in_field = x * widthRatio;
    double y_in_field = y * heightRatio;
    double width_in_field = width * widthRatio;
    double height_in_field = height * heightRatio;
    
    g.fillOval((int)x_in_field, (int)y_in_field, (int)width_in_field, (int)height_in_field);
  }
  
  private void drawOvalInField(GraphicsContext g, double x, double y, double width, double height) {
    double widthRatio = (double)field.getWidth() / (double)FIELDWIDTH;
    double heightRatio = (double)field.getHeight() / (double)FIELDLENGTH;
    
    double x_in_field = x * widthRatio;
    double y_in_field = y * heightRatio;
    double width_in_field = width * widthRatio;
    double height_in_field = height * heightRatio;
    
    g.strokeOval((int)x_in_field, (int)y_in_field, (int)width_in_field, (int)height_in_field);
  }
  
  private Rectangle getField() {
    int height = (int)this.getHeight();
    int width = (int)this.getWidth();
    
    double fieldRatio = (double)FIELDLENGTH / (double)FIELDWIDTH;
    double canvasRatio = (double)height / (double)width;
    
    int rectHeight = height;
    int rectWidth = width;
    if(fieldRatio > canvasRatio) {
      rectWidth = (int)((double)height / fieldRatio);
    } else {
      rectHeight = (int)((double)width * fieldRatio);
    }
    
    return new Rectangle(0, 0, rectWidth, rectHeight);
  }

  private void drawField(GraphicsContext g) {
    g.setFill(Color.GREEN);
    g.setStroke(Color.GREEN);
    g.fillRect(field.getX(), field.getY(), field.getWidth(), field.getHeight());
    
    g.setFill(Color.WHITE);
    g.setStroke(Color.WHITE);
    
    double width_center = (FIELDWIDTH / 2);
    double height_center = (FIELDLENGTH / 2);
    
    drawFillRectangleInField(g, width_center - (GOALWIDTH / 2), -GOALDEPTH, GOALWIDTH, GOALDEPTH);
    drawFillRectangleInField(g, width_center - (GOALWIDTH / 2), FIELDLENGTH, GOALWIDTH, GOALDEPTH);
    
    drawRectangleInField(g, width_center - (GOALAREAWIDTH / 2), 0, GOALAREAWIDTH, GOALAREALENGTH);
    drawRectangleInField(g, width_center - (GOALAREAWIDTH / 2), FIELDLENGTH - GOALAREALENGTH, GOALAREAWIDTH, GOALAREALENGTH);
    
    drawRectangleInField(g, width_center - (PENALTYAREAWIDTH / 2), 0, PENALTYAREAWIDTH, PENALTYAREALENGTH);
    drawRectangleInField(g, width_center - (PENALTYAREAWIDTH / 2), FIELDLENGTH - PENALTYAREALENGTH, PENALTYAREAWIDTH, PENALTYAREALENGTH);
    
    drawFillOvalInField(g, width_center - 0.2, PENALTYSPOT - 0.2, 0.4, 0.4);
    drawFillOvalInField(g, width_center - 0.2, FIELDLENGTH - PENALTYSPOT - 0.2, 0.4, 0.4);    
    
    drawFillRectangleInField(g, 0, height_center - 0.02, FIELDWIDTH, 0.04);
    drawOvalInField(g, width_center - 1.5, height_center - 1.5, 3, 3);
  }
}
