/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfreechart.chart;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Pair;
import jfreechart.object.ParameterMap;
import jfreechart.listener.SelectionEventListener;
import jfreechart.object.StringValuePair;
import jfreechart.object.TimeFrame;
import jfreechart.object.Turtle;
import static jfreechart.Parser.MAX_OPPONENTS;
import static jfreechart.Parser.MAX_TURTLES;
import static jfreechart.chart.Chart.getCheckbox;
import static jfreechart.chart.Chart.getTurtleListView;

public class FieldCanvas extends Pane implements Chart{
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
  private boolean liveUpdate;
  private int[] selectedTurtles;
  private boolean turtleHistory;
  private int[] selectedBall;
  private boolean ballHistory;
  private int[] selectedOpponents;
  private boolean opponentsHistory;
  
  private final Circle[] shape_ball = new Circle[MAX_TURTLES];
  private final Pair<Rectangle, Text>[][] shape_opponents = new Pair[MAX_TURTLES][MAX_OPPONENTS];
  private final Pair<Rectangle, Text>[] shape_turtles = new Pair[MAX_TURTLES];
  private Rectangle shape_field;
  private Rectangle shape_goal1;
  private Rectangle shape_goal2;
  private Rectangle shape_goalarea1;
  private Rectangle shape_goalarea2;
  private Rectangle shape_penalty1;
  private Rectangle shape_penalty2;
  private Circle shape_penaltyspot1;
  private Circle shape_penaltyspot2;
  private Rectangle shape_fieldcenter;
  private Circle shape_fieldcenteroval;
  
  private double initial_width;
  private double initial_height;
  
  public FieldCanvas(List<TimeFrame> data, boolean liveUpdate, int[] selectedTurtles, boolean turtleHistory, int[] selectedBall, boolean ballHistory, int[] selectedOpponents, boolean opponentsHistory) {
    this.data = data;
    
    this.liveUpdate = liveUpdate;
    this.turtleHistory = turtleHistory;
    this.selectedTurtles = selectedTurtles;
    this.ballHistory = ballHistory;
    this.selectedBall = selectedBall;
    this.selectedOpponents = selectedOpponents;
    this.opponentsHistory = opponentsHistory;
    
    this.getStylesheets().add("jfreechart/plot.css");
    
    selection = new ArrayList();
          
    widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
      field = getField();
      setClipping();
    });

    heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
      field = getField();    
      setClipping();
    });
    
    initField();
    drawField();    
  }
    
  public FieldCanvas() {
    this(new ArrayList(), true, new int[]{0, 1, 2, 3, 4, 5, 6 }, false, new int[]{ 0 }, false, new int[]{ 0 }, false);
  }
  
  @Override
  public void updateData(List<TimeFrame> data) {
    this.data = data;
    selection = new ArrayList();
    drawMovingShapes();
  }
  
  @Override
  public void selectFrames(int startIndex, int endIndex, boolean drag) {
    if((!drag || liveUpdate) && data.size() > 0) {
      this.selection = data.subList(startIndex, endIndex);
      drawMovingShapes();
    }
  }
  
  @Override
  public Node getNode() {
    return this;
  }
  
  @Override
  public String getName() {
    return "Field";
  }

  @Override
  public void addSelectionEventListener(SelectionEventListener listener) {
    listenerList.add(SelectionEventListener.class, listener);
  }
  
  @Override
  protected void layoutChildren() {
    super.layoutChildren();
        
    resizePanel();
  }

  private void initField() {    
    int width = 500;
    int height = 500;
    
    double fieldRatio = (double)FIELDLENGTH / (double)FIELDWIDTH;
    double canvasRatio = (double)height / (double)width;
    
    int rectHeight = height;
    int rectWidth = width;
    if(fieldRatio > canvasRatio) {
      rectWidth = (int)((double)height / fieldRatio);
    } else {
      rectHeight = (int)((double)width * fieldRatio);
    }
    
    field = new Rectangle(0, 0, rectWidth, rectHeight);
      
    initial_width= rectWidth;
    initial_height= rectHeight;
  }
  
  private void setClipping() {
    Rectangle clip = new Rectangle(0, 0, Math.max(500, this.getWidth()), Math.max(500, this.getHeight()));
    this.setClip(clip);    
  }
  
  @Override
  public void showParameterDialog() {
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(10, 10, 10, 10));

    Dialog<Boolean> parameterDialog = new Dialog();
    parameterDialog.setTitle("Field options");
    parameterDialog.setHeaderText("Choose field options");
    parameterDialog.setContentText("Choose field options:");
    parameterDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    CheckBox liveCheckbox = getCheckbox("Live update", liveUpdate);
    
    ListView turtleListView = getTurtleListView(selectedTurtles);
    CheckBox turtleCheckbox = getCheckbox("Show history", turtleHistory);
    
    ListView ballListView = getTurtleListView(selectedBall);
    CheckBox ballCheckbox = getCheckbox("Show history", ballHistory);
    
    ListView opponentsListView = getTurtleListView(selectedOpponents);
    CheckBox opponentsCheckbox = getCheckbox("Show history", opponentsHistory);
    
    grid.add(liveCheckbox, 0, 0);
    
    grid.add(new Label("Turtles"), 0, 1);
    grid.add(turtleCheckbox, 0, 2);
    grid.add(turtleListView, 0, 3);

    grid.add(new Label("Ball"), 1, 1);
    grid.add(ballCheckbox, 1, 2);
    grid.add(ballListView, 1, 3);
    
    grid.add(new Label("Opponents"), 2, 1);
    grid.add(opponentsCheckbox, 2, 2);
    grid.add(opponentsListView, 2, 3);
    
    parameterDialog.getDialogPane().setContent(grid);
    
    parameterDialog.setResultConverter(dialogButton -> {
      if(dialogButton == ButtonType.OK) {

        liveUpdate = liveCheckbox.isSelected();
        
        ObservableList<StringValuePair<String, Integer>> turtleSelection = turtleListView.getSelectionModel().getSelectedItems();
        selectedTurtles = new int[turtleSelection.size()];
        for(int i = 0; i < turtleSelection.size(); i++) {
          selectedTurtles[i] = turtleSelection.get(i).getValue();
        }
        turtleHistory = turtleCheckbox.isSelected();
        
        ObservableList<StringValuePair<String, Integer>> ballSelection = ballListView.getSelectionModel().getSelectedItems();
        selectedBall = new int[ballSelection.size()];
        for(int i = 0; i < ballSelection.size(); i++) {
          selectedBall[i] = ballSelection.get(i).getValue();
        }
        ballHistory = ballCheckbox.isSelected();
        
        ObservableList<StringValuePair<String, Integer>> opponentsSelection = opponentsListView.getSelectionModel().getSelectedItems();
        selectedOpponents = new int[opponentsSelection.size()];
        for(int i = 0; i < opponentsSelection.size(); i++) {
          selectedOpponents[i] = opponentsSelection.get(i).getValue();
        }
        opponentsHistory = opponentsCheckbox.isSelected();

        return true;
      }
      return null;
    });

    Optional<Boolean> result = parameterDialog.showAndWait();

    if(result.isPresent() && result.get() != null) {
      drawMovingShapes();
    }
  }
  
  private void drawMovingShapes() {
    if(field != null) {
      
      removePaths();
      drawTurtleLines();
      drawBallLines();
      drawOpponentLines();

      drawOpponents(); 

      drawturtles();

      drawBall();

      resizePanel();
    }
  }
  
  private void drawturtles() {

    for(int i = 0; i < shape_turtles.length; i++) {
      if(shape_turtles[i] != null) {          
        shape_turtles[i].getKey().setVisible(false);
        shape_turtles[i].getValue().setVisible(false);
      }
    }
    
    if(selection.size() > 0) {
      List<Turtle> turtles = selection.get(selection.size() - 1).getTurtles();
      //for(Turtle turtle : selection.get(selection.size() - 1).getTurtles()) {
      for(int selectedTurtle : selectedTurtles) {
        Turtle turtle  = turtles.get(selectedTurtle);
        ParameterMap paremeters = turtle.getParameters();
        if(paremeters.getValue("robotInField")[0][0] > 0) {
          double[][] position = paremeters.getValue("pose");
          Point turtlePos = getPosition(field, position[0][0], position[0][1]);
          double orientation =  position[0][2];
          
          int index = turtle.getID();

          if(shape_turtles[index] == null) {            
            
            
              Rectangle turtleRect = RectangleBuilder.create()
                      .x(turtlePos.getX() - 10)
                      .y(turtlePos.getY() - 10)
                      .height(20)
                      .width(20)
                      .styleClass(String.format("default-color%d-agent", (int)index))
                      .build();
                            
              turtleRect.setRotate(Math.toDegrees(orientation));
              Text turtleText = new Text(turtlePos.getX() - 5, turtlePos.getY() + 5, String.valueOf(index + 1));
              turtleText.setFill(Color.WHITE);
              turtleText.setRotate(Math.toDegrees(orientation));

              shape_turtles[index] = new Pair(turtleRect, turtleText);
              this.getChildren().add(turtleRect);
              this.getChildren().add(turtleText);
          } else {
            if(turtlePos.getX() != 0 || turtlePos.getY() != 0) {
              shape_turtles[index].getKey().setX(turtlePos.getX() - 10);
              shape_turtles[index].getKey().setY(turtlePos.getY() - 10);
              shape_turtles[index].getKey().setRotate(Math.toDegrees(orientation));
              
              shape_turtles[index].getValue().setX(turtlePos.getX() - 5);
              shape_turtles[index].getValue().setY(turtlePos.getY() + 5);
              shape_turtles[index].getValue().setRotate(Math.toDegrees(orientation));
              
              shape_turtles[index].getKey().setVisible(true);
              shape_turtles[index].getValue().setVisible(true);
            } else {
              shape_turtles[index].getKey().setVisible(false);
              shape_turtles[index].getValue().setVisible(false);
            }
          }
        }
      }
    }
  }
  
  private void drawOpponents() {
    
    for(int turtle = 0; turtle < shape_opponents.length; turtle++) { 
      for(int opponent = 0; opponent < shape_opponents[turtle].length; opponent++) { 
        if(shape_opponents[turtle][opponent] != null) {          
          shape_opponents[turtle][opponent].getKey().setVisible(false);
          shape_opponents[turtle][opponent].getValue().setVisible(false);
        }          
      }
    }

    if(selection.size() > 0) {
      
      List<Turtle> turtles = selection.get(selection.size() - 1).getTurtles();
      for(int selectedTurtle : selectedOpponents) {        
        Turtle turtle  = turtles.get(selectedTurtle);
           
        ParameterMap paremeters = turtle.getParameters();
        double[][] opponents = paremeters.getValue("opponent");
        double[][] opponentLabels = paremeters.getValue("opponentlabelnumber");

        for(int i = 0; i < opponents.length; i++) {
          
          if(opponents[i][0] != 0 && opponents[i][1] != 0 && opponents[i][0] >= -32 && opponents[i][1] >= -32) {
            int label = (int)opponentLabels[i][0];
            Point opponentPos = getPosition(field, opponents[i][0], opponents[i][1]);
            
            if(shape_opponents[selectedTurtle][i] == null) {            
                Rectangle opponent = new Rectangle(opponentPos.getX() - 10, opponentPos.getY() - 10, 20, 20);
                opponent.setFill(Color.DARKSLATEGREY);
                opponent.getStyleClass().add(String.format("default-color%d-opponent", (int)selectedTurtle));
                Text opponentText = new Text(opponentPos.getX() - 5, opponentPos.getY() + 5, String.valueOf(label));
                opponentText.getStyleClass().add(String.format("default-color%d-opponent-text", (int)selectedTurtle));
                
                shape_opponents[selectedTurtle][i] = new Pair(opponent, opponentText);
                this.getChildren().add(opponent);
                this.getChildren().add(opponentText);
            } else {
              if(opponentPos.getX() != 0 || opponentPos.getY() != 0) {
                shape_opponents[selectedTurtle][i].getKey().setX(opponentPos.getX() - 10);
                shape_opponents[selectedTurtle][i].getKey().setY(opponentPos.getY() - 10);

                shape_opponents[selectedTurtle][i].getValue().setX(opponentPos.getX() - 5);
                shape_opponents[selectedTurtle][i].getValue().setY(opponentPos.getY() + 5);

                shape_opponents[selectedTurtle][i].getKey().setVisible(true);
                shape_opponents[selectedTurtle][i].getValue().setVisible(true);
              }
            }
          }
        }        
      }
    }
  }
  
  private void drawBall() {

    for(int i = 0; i < shape_ball.length; i++) { 
      if(shape_ball[i] != null) {          
        shape_ball[i].setVisible(false);
      }          
    }

    if(selection.size() > 0) {
      List<Turtle> turtles = selection.get(selection.size() - 1).getTurtles();
      for(int selectedTurtle : selectedBall) {        
        Turtle turtle  = turtles.get(selectedTurtle);
        
        ParameterMap paremeters = turtle.getParameters();
        if(paremeters.getValue("ballFound")[0][0] != 0) {
          double[][] ball = paremeters.getValue("ball");       
          if(ball[0][0] != 0 && ball[0][1] != 0 && ball[0][0] >= -32 && ball[0][1] >= -32) {
            Point ballPos = getPosition(field, ball[0][0], ball[0][1]);

            if(shape_ball[selectedTurtle] == null) {        
              shape_ball[selectedTurtle] = new Circle(ballPos.getX(), ballPos.getY(), 5, Color.ORANGE);
              this.getChildren().add(shape_ball[selectedTurtle]);
            } else {
              shape_ball[selectedTurtle].setVisible(true);
              shape_ball[selectedTurtle].setCenterX(ballPos.getX());
              shape_ball[selectedTurtle].setCenterY(ballPos.getY());
            }
          }
        }
      }
    }
  }
    
  private void drawField() {    
    double width_center = (FIELDWIDTH / 2);
    double height_center = (FIELDLENGTH / 2);
    
    if(shape_field == null) {      
      shape_field = new Rectangle(field.getX(), field.getY(), field.getWidth(), field.getHeight());
      shape_field.setFill(Color.GREEN);
      shape_field.setStroke(Color.GREEN);
      
      Rectangle position = translateToField(width_center - (GOALWIDTH / 2), -GOALDEPTH, GOALWIDTH, GOALDEPTH);
      shape_goal1 = new Rectangle(position.getX(), position.getY(), position.getWidth(), position.getHeight());
      shape_goal1.setFill(Color.TRANSPARENT);
      shape_goal1.setStroke(Color.WHITE);
      
      position = translateToField(width_center - (GOALWIDTH / 2), FIELDLENGTH, GOALWIDTH, GOALDEPTH);
      shape_goal2 = new Rectangle(position.getX(), position.getY(), position.getWidth(), position.getHeight());
      shape_goal2.setFill(Color.TRANSPARENT);
      shape_goal2.setStroke(Color.WHITE);

      position = translateToField(width_center - (GOALAREAWIDTH / 2), 0, GOALAREAWIDTH, GOALAREALENGTH);
      shape_goalarea1 = new Rectangle(position.getX(), position.getY(), position.getWidth(), position.getHeight());
      shape_goalarea1.setFill(Color.TRANSPARENT);
      shape_goalarea1.setStroke(Color.WHITE);
      
      position = translateToField(width_center - (GOALAREAWIDTH / 2), FIELDLENGTH - GOALAREALENGTH, GOALAREAWIDTH, GOALAREALENGTH);
      shape_goalarea2 = new Rectangle(position.getX(), position.getY(), position.getWidth(), position.getHeight());
      shape_goalarea2.setFill(Color.TRANSPARENT);
      shape_goalarea2.setStroke(Color.WHITE);

      position = translateToField(width_center - (PENALTYAREAWIDTH / 2), 0, PENALTYAREAWIDTH, PENALTYAREALENGTH);
      shape_penalty1 = new Rectangle(position.getX(), position.getY(), position.getWidth(), position.getHeight());
      shape_penalty1.setFill(Color.TRANSPARENT);
      shape_penalty1.setStroke(Color.WHITE);
      
      position = translateToField(width_center - (PENALTYAREAWIDTH / 2), FIELDLENGTH - PENALTYAREALENGTH, PENALTYAREAWIDTH, PENALTYAREALENGTH);
      shape_penalty2 = new Rectangle(position.getX(), position.getY(), position.getWidth(), position.getHeight());
      shape_penalty2.setFill(Color.TRANSPARENT);
      shape_penalty2.setStroke(Color.WHITE);

      position = translateToField(width_center, PENALTYSPOT, 0.2, 0.2);
      shape_penaltyspot1 = new Circle(position.getX(), position.getY(), position.getWidth());
      shape_penaltyspot1.setFill(Color.WHITE);
      shape_penaltyspot1.setStroke(Color.WHITE);

      position = translateToField(width_center, FIELDLENGTH - PENALTYSPOT, 0.2, 0.2);    
      shape_penaltyspot2 = new Circle(position.getX(), position.getY(), position.getWidth());
      shape_penaltyspot2.setFill(Color.WHITE);
      shape_penaltyspot2.setStroke(Color.WHITE);

      position = translateToField(0, height_center - 0.02, FIELDWIDTH, 0.04);
      shape_fieldcenter = new Rectangle(position.getX(), position.getY(), position.getWidth(), position.getHeight());
      shape_fieldcenter.setFill(Color.TRANSPARENT);
      shape_fieldcenter.setStroke(Color.WHITE);

      position = translateToField(width_center, height_center, 1.5, 1.5);
      shape_fieldcenteroval = new Circle(position.getX(), position.getY(), position.getWidth());
      shape_fieldcenteroval.setFill(Color.TRANSPARENT);
      shape_fieldcenteroval.setStroke(Color.WHITE);

      this.getChildren().add(shape_field);
      this.getChildren().add(shape_goal1);
      this.getChildren().add(shape_goal2);
      this.getChildren().add(shape_goalarea1);
      this.getChildren().add(shape_goalarea2);
      this.getChildren().add(shape_penalty1);
      this.getChildren().add(shape_penalty2);      
      this.getChildren().add(shape_penaltyspot1);      
      this.getChildren().add(shape_penaltyspot2);      
      this.getChildren().add(shape_fieldcenter);      
      this.getChildren().add(shape_fieldcenteroval);      
    }
  }
  
  private void drawTurtleLines() {
    if(turtleHistory) {      
      addPaths(selectedTurtles, "pose", "default-color%d-agent-line");
    }
  }
  
  private void drawBallLines() {
    if(ballHistory) {      
      addPaths(selectedBall, "ball", "default-color%d-ball-line");
    }
  }
  
  private void drawOpponentLines() {
    if(opponentsHistory) {      
      addPaths(selectedOpponents, "opponent", "default-color%d-opponent-line");
    }
  }
  
  private void addPaths(int[] selected, String parameter, String style) {
    if(selection.size() > 0) {
      List<Pair<Integer, List<Point>>> paths = new ArrayList();

      for(int turtleIndex : selected) {
        
        ParameterMap initalCount = selection.get(0).getTurtles().get(turtleIndex).getParameters();
        int size = initalCount.getValue(parameter).length;
        List<Point>[] newPaths = new List[size];
        for(int i = 0; i < size; i++) {
          newPaths[i] = new ArrayList();
        }

        for(int frameIndex = 0; frameIndex < selection.size(); frameIndex++) {
          Turtle turle = selection.get(frameIndex).getTurtles().get(turtleIndex);
          ParameterMap paremeters = turle.getParameters();
          
          double[][] value = paremeters.getValue(parameter);

          for(int i = 0; i < value.length; i++) {
            if(value[i][0] != 0 && value[i][1] != 0 && value[i][0] >= -32 && value[i][1] >= -32) {
              Point position = getPosition(field, value[i][0], value[i][1]);
              newPaths[i].add(position);
            }
          }
        }

        for(List<Point> path : newPaths) {
          paths.add(new Pair(turtleIndex,path));
        }
      }
      
      for(Pair<Integer, List<Point>> points : paths) {
        if(points.getValue().size() > 0) {

          Path path = new Path();
          path.getStyleClass().add(String.format(style, (int)points.getKey()));
          path.setStrokeWidth(2);
                  
          MoveTo moveTo = new MoveTo(points.getValue().get(0).x, points.getValue().get(0).y);
          path.getElements().add(moveTo); 

          for(Point point : points.getValue()) {
            LineTo lineTo = new LineTo(point.x, point.y);
            path.getElements().add(lineTo);
          }

          this.paths.add(path);
          this.getChildren().add(11, path);
        }
      }
    }
  }
  
  private void resizePanel() {      
      if(field != null) {
        double cx = this.getBoundsInParent().getMinX();
        double cy = this.getBoundsInParent().getMinY();

        double scaleX = field.getWidth()/ initial_width;
        double scaleY = field.getHeight() / initial_height;

        this.getTransforms().clear();
        this.getTransforms().add(new Translate(-cx, -cy));
        this.getTransforms().add(new Scale(scaleX, scaleY, cx, cy));
        this.getTransforms().add(new Translate(cx, cy));      
      }
  }
  
  private List<Path> paths = new ArrayList();
  
  private void removePaths() {
    for(Path path : paths) {
      this.getChildren().remove(path);
    }
    paths.clear();
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
    
    double field_center_x = (double)initial_width / 2;
    double field_center_y = (double)initial_height / 2;
    
    double widthRatio = (double)initial_width  / (double)FIELDWIDTH;
    double heightRatio = (double)initial_height / (double)FIELDLENGTH;
    
    double center_x_in_field = center_x * widthRatio;
    double center_y_in_field = center_y * heightRatio;
    
    return new Point((int)(field_center_x + center_x_in_field), (int)(field_center_y + center_y_in_field));
  }
  
  private Rectangle translateToField(double x, double y, double width, double height) {
    double widthRatio = (double)field.getWidth() / (double)FIELDWIDTH;
    double heightRatio = (double)field.getHeight() / (double)FIELDLENGTH;
    
    double x_in_field = x * widthRatio;
    double y_in_field = y * heightRatio;
    double width_in_field = width * widthRatio;
    double height_in_field = height * heightRatio;
    
    return new Rectangle(x_in_field, y_in_field, width_in_field, height_in_field);
  }
    
  private Rectangle getField() {
    int width = (int)this.getWidth();
    int height = (int)this.getHeight();
    
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
}
