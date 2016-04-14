package prototype.chart;

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
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Pair;
import prototype.object.ParameterMap;
import prototype.listener.SelectionEventListener;
import prototype.object.StringValuePair;
import prototype.object.Turtle;
import static prototype.chart.Chart.getCheckbox;
import static prototype.chart.Chart.getTurtleListView;
import prototype.object.Parameter;
import prototype.object.Range;
import prototype.settings.Configuration;
import org.dockfx.DockNode;

public class FieldCanvas extends Pane implements Chart{
  private List<Turtle> data;
  private Range selection;
  private Rectangle field;
  private List<SelectionEventListener> listenerList = new ArrayList();
  
  public boolean liveUpdate;  
  public int[] selectedTurtles;
  public boolean turtleHistory;
  public int[] selectedBall;
  public boolean ballHistory;
  public int[] selectedOpponents;
  public boolean opponentsHistory;
  
  private DockNode dockNode;
  
  private final Circle[] shape_ball;
  private final Pair<Rectangle, Text>[][] shape_opponents;
  private final Pair<Polygon, Text>[] shape_turtles;
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
  
  private List<Path> paths = new ArrayList();
  
  private double initial_width;
  private double initial_height;
  
  private ParameterMap parameterMap;
  private Configuration configuration;
  
  public FieldCanvas(List<Turtle> data, boolean liveUpdate, int[] selectedTurtles, boolean turtleHistory, int[] selectedBall, boolean ballHistory, int[] selectedOpponents, boolean opponentsHistory) {
    this.data = data;
    
    this.liveUpdate = liveUpdate;
    this.turtleHistory = turtleHistory;
    this.selectedTurtles = selectedTurtles;
    this.ballHistory = ballHistory;
    this.selectedBall = selectedBall;
    this.selectedOpponents = selectedOpponents;
    this.opponentsHistory = opponentsHistory;
    
    this.configuration = new Configuration();
    this.parameterMap = new ParameterMap();
    
    this.shape_ball = new Circle[this.configuration.MaxTurtles];
    this.shape_opponents = new Pair[this.configuration.MaxTurtles][this.configuration.MaxOpponents];
    this.shape_turtles = new Pair[this.configuration.MaxTurtles];
  
    this.getStylesheets().add("prototype/plot.css");
              
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
  public void updateData(List<Turtle> data) {
    this.data = data;
    this.selection = new Range(0, 0);
   
    this.configuration = new Configuration();
    this.parameterMap = new ParameterMap();
    
    drawMovingShapes();
  }
  
  @Override
  public void selectFrames(int startIndex, int endIndex, boolean drag) {
    if((!drag || liveUpdate) && data.size() > 0) {
      this.selection = new Range(startIndex, endIndex);
      drawMovingShapes();
      setDockTitle();
    } else {
      this.selection = new Range(0, 0);
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
  public Chart getCopy() {
    return new FieldCanvas(data, liveUpdate, selectedTurtles, turtleHistory, selectedBall, ballHistory, selectedOpponents, opponentsHistory);
  }
    
  @Override
  public void addSelectionEventListener(SelectionEventListener listener) {
    listenerList.add(listener);
  }
  
  @Override
  public void update() {
    initField();
    drawField();   
  }
  
  @Override
  public void clearFilter() {
  
  }

  @Override
  public void setDockNode(DockNode dockNode) {
    this.dockNode = dockNode;
    setDockTitle();
  }
  
  private void setDockTitle() {
    if(this.dockNode != null) {
      this.dockNode.setTitle(String.format("%s [%d - %d]", getName(), this.selection.GetMin(), this.selection.GetMax()));
    }
  }
  
  @Override
  protected void layoutChildren() {
    super.layoutChildren();
        
    resizePanel();
  }

  private void initField() {    
    int width = 500;
    int height = 500;
    
    double fieldRatio = (double)this.configuration.FieldLength / (double)this.configuration.FieldWidth;
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
    
    if(selection != null && data != null && data.size() > 0) {
      try {
        for(int selectedTurtle : selectedTurtles) {
          Turtle turtle  = data.get(selectedTurtle);

          DataPoint inField = turtle.GetValue(this.configuration.RobotInField, 0, this.configuration.RobotInFieldIndex, selection.GetMax());
          DataPoint poseX = turtle.GetValue(this.configuration.Pose, 0, this.configuration.PoseX, selection.GetMax());
          DataPoint poseY = turtle.GetValue(this.configuration.Pose, 0, this.configuration.PoseY, selection.GetMax());
          DataPoint orientationValue = turtle.GetValue(this.configuration.Pose, 0, this.configuration.PoseRot, selection.GetMax());

          if(inField != null && inField.getValue() > 0) {
            Point turtlePos = getPosition(field, poseX.getValue(), poseY.getValue());
            double orientation = orientationValue.getValue();

            int index = turtle.getID();

            if(shape_turtles[index] == null) {            
               
                Polygon turtlePolygon = createTurtleShape(index);
                
                turtlePolygon.setLayoutX(turtlePos.getX() - 15);
                turtlePolygon.setLayoutY(turtlePos.getY() - 12.5);
                turtlePolygon.setRotate(Math.toDegrees(orientation));

                Text turtleText = new Text(turtlePos.getX() - 5, turtlePos.getY() + 5, String.valueOf(index + 1));
                
                turtleText.setFill(Color.WHITE);
                turtleText.setRotate(Math.toDegrees(orientation));

                shape_turtles[index] = new Pair(turtlePolygon, turtleText);
                this.getChildren().add(turtlePolygon);
                this.getChildren().add(turtleText);
            } else {
              if(turtlePos.getX() != 0 || turtlePos.getY() != 0) {
                shape_turtles[index].getKey().setTranslateY(orientation);
                
                shape_turtles[index].getKey().setLayoutX(turtlePos.getX() - 15);
                shape_turtles[index].getKey().setLayoutY(turtlePos.getY() - 12.5);
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
      } catch(Exception ex) {
        ex.printStackTrace();        
      }
    }
  }
  
  private Polygon createTurtleShape(int index) {
    Polygon polygon = new Polygon();
    polygon.getPoints().addAll(new Double[]{
        0.0, 0.0,
        30.0, 0.0,
        30.0, 5.0,
        30.0, 15.0,
        15.0, 25.0,
        0.0, 15.0,
        0.0, 5.0 });
    polygon.getStyleClass().add(String.format("default-color%d-agent", index));
    polygon.setStrokeWidth(1);
    
    return polygon;
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
    
    if(selection != null && data != null && data.size() > 0) {
      try {
        for(int selectedTurtle : selectedOpponents) {        
          Turtle turtle  = data.get(selectedTurtle);

          Parameter parameter = this.parameterMap.GetParameter(this.configuration.Opponent);

          for(int i = 0; i < parameter.getCount(); i++) {

            DataPoint opponentX = turtle.GetValue(this.configuration.Opponent, i, this.configuration.OpponentX, selection.GetMax());
            DataPoint opponentY = turtle.GetValue(this.configuration.Opponent, i, this.configuration.OpponentY, selection.GetMax());
            DataPoint opponentlabel = turtle.GetValue(this.configuration.Opponentlabelnumber, i, this.configuration.OpponentlabelnumberIndex, selection.GetMax());
 
            if(opponentX != null && opponentX.getValue() != 0 && opponentY.getValue() != 0) {
              int label = (int)Math.round(opponentlabel.getValue());
              Point opponentPos = getPosition(field, opponentX.getValue(), opponentY.getValue());

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
                  shape_opponents[selectedTurtle][i].getValue().setText(String.valueOf(label));

                  shape_opponents[selectedTurtle][i].getKey().setVisible(true);
                  shape_opponents[selectedTurtle][i].getValue().setVisible(true);
                }
              }
            }
          }
        }
      } catch(Exception ex) {
        ex.printStackTrace();        
      }
    }
  }
  
  private void drawBall() {

    for(int i = 0; i < shape_ball.length; i++) { 
      if(shape_ball[i] != null) {          
        shape_ball[i].setVisible(false);
      }          
    }

    if(selection != null && data != null && data.size() > 0) {
      
      try {
        for(int selectedTurtle : selectedBall) {
          Turtle turtle  = data.get(selectedTurtle);

          DataPoint ballFound = turtle.GetValue(this.configuration.BallFound, 0, this.configuration.BallFoundIndex, selection.GetMax());
          DataPoint ballX = turtle.GetValue(this.configuration.Ball, 0, this.configuration.BallX, selection.GetMax());
          DataPoint ballY = turtle.GetValue(this.configuration.Ball, 0, this.configuration.BallY, selection.GetMax());

          if(ballFound != null&& ballFound.getValue() > 0 && ballX.getValue() != 0 && ballY.getValue() != 0) {
            Point ballPos = getPosition(field, ballX.getValue(), ballY.getValue());

            if(shape_ball[selectedTurtle] == null) {        
              shape_ball[selectedTurtle] = new Circle(ballPos.getX(), ballPos.getY(), 7.0f, Color.ORANGE);
              shape_ball[selectedTurtle].setStroke(Color.DARKORANGE);
              this.getChildren().add(shape_ball[selectedTurtle]);
            } else {
              shape_ball[selectedTurtle].setVisible(true);
              shape_ball[selectedTurtle].setCenterX(ballPos.getX());
              shape_ball[selectedTurtle].setCenterY(ballPos.getY());
            }
          }
        }
      } catch(Exception ex) {
        ex.printStackTrace();        
      }
    }
  }
  
  private void drawField() {
    double width_center = (this.configuration.FieldWidth / 2);
    double height_center = (this.configuration.FieldLength / 2);
    
    if(shape_field == null) {      
      shape_field = new Rectangle(field.getX(), field.getY(), field.getWidth(), field.getHeight());
      shape_field.setFill(Color.GREEN);
      shape_field.setStroke(Color.GREEN);
      
      Rectangle position = translateToField(width_center - (this.configuration.GoalWidth / 2), -this.configuration.GoalDepth, this.configuration.GoalWidth, this.configuration.GoalDepth);
      shape_goal1 = new Rectangle(position.getX(), position.getY(), position.getWidth(), position.getHeight());
      shape_goal1.setFill(Color.TRANSPARENT);
      shape_goal1.setStroke(Color.WHITE);
      
      position = translateToField(width_center - (this.configuration.GoalWidth / 2), this.configuration.FieldLength, this.configuration.GoalWidth, this.configuration.GoalDepth);
      shape_goal2 = new Rectangle(position.getX(), position.getY(), position.getWidth(), position.getHeight());
      shape_goal2.setFill(Color.TRANSPARENT);
      shape_goal2.setStroke(Color.WHITE);

      position = translateToField(width_center - (this.configuration.GoalAreaWidth / 2), 0, this.configuration.GoalAreaWidth, this.configuration.GoalAreaLength);
      shape_goalarea1 = new Rectangle(position.getX(), position.getY(), position.getWidth(), position.getHeight());
      shape_goalarea1.setFill(Color.TRANSPARENT);
      shape_goalarea1.setStroke(Color.WHITE);
      
      position = translateToField(width_center - (this.configuration.GoalAreaWidth / 2), this.configuration.FieldLength - this.configuration.GoalAreaLength, this.configuration.GoalAreaWidth, this.configuration.GoalAreaLength);
      shape_goalarea2 = new Rectangle(position.getX(), position.getY(), position.getWidth(), position.getHeight());
      shape_goalarea2.setFill(Color.TRANSPARENT);
      shape_goalarea2.setStroke(Color.WHITE);

      position = translateToField(width_center - (this.configuration.PenaltyWidth / 2), 0, this.configuration.PenaltyWidth, this.configuration.PenaltyLength);
      shape_penalty1 = new Rectangle(position.getX(), position.getY(), position.getWidth(), position.getHeight());
      shape_penalty1.setFill(Color.TRANSPARENT);
      shape_penalty1.setStroke(Color.WHITE);
      
      position = translateToField(width_center - (this.configuration.PenaltyWidth / 2), this.configuration.FieldLength - this.configuration.PenaltyLength, this.configuration.PenaltyWidth, this.configuration.PenaltyLength);
      shape_penalty2 = new Rectangle(position.getX(), position.getY(), position.getWidth(), position.getHeight());
      shape_penalty2.setFill(Color.TRANSPARENT);
      shape_penalty2.setStroke(Color.WHITE);

      position = translateToField(width_center, this.configuration.PenaltySpot, 0.15, 0.15);
      shape_penaltyspot1 = new Circle(position.getX(), position.getY(), position.getWidth());
      shape_penaltyspot1.setStroke(Color.rgb(154, 205, 50));
      shape_penaltyspot1.setFill(Color.rgb(154, 205, 50));
      
      position = translateToField(width_center, this.configuration.FieldLength - this.configuration.PenaltySpot, 0.15, 0.15);    
      shape_penaltyspot2 = new Circle(position.getX(), position.getY(), position.getWidth());
      shape_penaltyspot2.setStroke(Color.rgb(154, 205, 50));
      shape_penaltyspot2.setFill(Color.rgb(154, 205, 50));

      position = translateToField(0, height_center - 0.02, this.configuration.FieldWidth, 0.04);
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
    if(turtleHistory && selection != null && data != null && data.size() > 0) {
      List<Pair<Integer, List<Point>>> turtle_paths = new ArrayList();

      for(int selectedTurtle : selectedTurtles) {
        Turtle turtle  = data.get(selectedTurtle);

        Parameter parameter = this.parameterMap.GetParameter(this.configuration.Pose);
            
        for(int parameterIndex = 0; parameterIndex < parameter.getCount(); parameterIndex++) {

          List<DataPoint> posXValues = turtle.GetValues(this.configuration.Pose, parameterIndex, this.configuration.PoseX, selection.GetMin(), selection.GetMax());
          List<DataPoint> posYValues = turtle.GetValues(this.configuration.Pose, parameterIndex, this.configuration.PoseY, selection.GetMin(), selection.GetMax());          
          
          List<Point> newPath = new ArrayList();

          for(int valueIndex = 0; valueIndex < posXValues.size(); valueIndex++) {
            DataPoint dataX = posXValues.get(valueIndex);
            DataPoint dataY = posYValues.get(valueIndex);

            double posX = dataX.getValue();
            double posY = dataY.getValue();
            
            if(dataX.isVisible() && dataY.isVisible() && posX != 0 && posY != 0) {   
              Point position = getPosition(field, posX, posY);
              newPath.add(position);
            } else {
              if(newPath.size() > 0) {
                turtle_paths.add(new Pair(selectedTurtle, newPath));
                newPath = new ArrayList();
              }
            }
          }

          turtle_paths.add(new Pair(selectedTurtle, newPath));
        }
      }

      drawPaths(turtle_paths, "default-color%d-agent-line");
    }
  }
  
  private void drawBallLines() {
    if(ballHistory && selection != null && data != null && data.size() > 0) {
      List<Pair<Integer, List<Point>>> ball_paths = new ArrayList();

      for(int selectedTurtle : selectedBall) {
        Turtle turtle  = data.get(selectedTurtle);

        Parameter parameter = this.parameterMap.GetParameter(this.configuration.Ball);

        for(int parameterIndex = 0; parameterIndex < parameter.getCount(); parameterIndex++) {

          List<DataPoint> posXValues = turtle.GetValues(this.configuration.Ball, parameterIndex, this.configuration.BallX, selection.GetMin(), selection.GetMax());
          List<DataPoint> posYValues = turtle.GetValues(this.configuration.Ball, parameterIndex, this.configuration.BallY, selection.GetMin(), selection.GetMax());
          
          List<DataPoint> turtleXValues = turtle.GetValues(this.configuration.Pose, parameterIndex, this.configuration.PoseX, selection.GetMin(), selection.GetMax());
          List<DataPoint> turtleYValues = turtle.GetValues(this.configuration.Pose, parameterIndex, this.configuration.PoseY, selection.GetMin(), selection.GetMax());        
          
          List<Point> newPath = new ArrayList();

          for(int valueIndex = 0; valueIndex < posXValues.size(); valueIndex++) {
            DataPoint dataX = posXValues.get(valueIndex);
            DataPoint dataY = posYValues.get(valueIndex);

            double posX = dataX.getValue();
            double posY = dataY.getValue();
            
            double turtleX = turtleXValues.get(valueIndex).getValue();
            double turtleY = turtleYValues.get(valueIndex).getValue();

            if(dataX.isVisible() && dataY.isVisible() && posX != 0 && posY != 0 && posX != turtleX && posY != turtleY) {   
              Point position = getPosition(field, posX, posY);
              newPath.add(position);
            } else {
              if(newPath.size() > 0) {
                ball_paths.add(new Pair(selectedTurtle, newPath));
                newPath = new ArrayList();
              }
            }
          }

          ball_paths.add(new Pair(selectedTurtle, newPath));
        }
      }

      drawPaths(ball_paths, "default-color%d-ball-line");
    }
  }
  
  private void drawOpponentLines() {
    if(opponentsHistory && selection != null && data != null && data.size() > 0) {
      List<Pair<Integer, List<Point>>> opponent_paths = new ArrayList();

      for(int selectedTurtle : selectedOpponents) {
        Turtle turtle  = data.get(selectedTurtle);

        Parameter parameter = this.parameterMap.GetParameter(this.configuration.Opponent);

        for(int parameterIndex = 0; parameterIndex < parameter.getCount(); parameterIndex++) {

          List<DataPoint> posXValues = turtle.GetValues(this.configuration.Opponent, parameterIndex, this.configuration.OpponentX, selection.GetMin(), selection.GetMax());
          List<DataPoint> posYValues = turtle.GetValues(this.configuration.Opponent, parameterIndex, this.configuration.OpponentY, selection.GetMin(), selection.GetMax());          
          List<DataPoint> labelValues = turtle.GetValues(this.configuration.Opponentlabelnumber, parameterIndex, this.configuration.OpponentlabelnumberIndex, selection.GetMin(), selection.GetMax());

          List<Point> newPath = new ArrayList();
          if(labelValues.size() > 0) {
            double currentLabel = labelValues.get(0).getValue();

            for(int valueIndex = 0; valueIndex < posXValues.size(); valueIndex++) {
              DataPoint dataX = posXValues.get(valueIndex);
              DataPoint dataY = posYValues.get(valueIndex);

              double posX = dataX.getValue();
              double posY = dataY.getValue();
              double label = labelValues.get(valueIndex).getValue();

              if(dataX.isVisible() && dataY.isVisible() && currentLabel == label && posX != 0 && posY != 0) {   
                Point position = getPosition(field, posX, posY);
                newPath.add(position);
              } else {
                currentLabel = label;
                if(newPath.size() > 0) {
                  opponent_paths.add(new Pair(selectedTurtle, newPath));
                  newPath = new ArrayList();
                }
              }
            }

            opponent_paths.add(new Pair(selectedTurtle, newPath));
          }
        }
      }

      drawPaths(opponent_paths, "default-color%d-opponent-line");
    }
  }
  
  private void drawPaths(List<Pair<Integer, List<Point>>> paths, String style) {
    if(selection != null) {      
      for(Pair<Integer, List<Point>> points : paths) {
        if(points.getValue().size() > 0) {
          Path path = new Path();
          path.getStyleClass().add(String.format(style, (int)points.getKey()));
          path.setStrokeWidth(2);
          
          MoveTo moveTo = new MoveTo(points.getValue().get(0).x, points.getValue().get(0).y);
          path.getElements().add(moveTo); 

          for(int i = 1; i < points.getValue().size(); i++) {
            Point point = points.getValue().get(i);
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
    
    double widthRatio = (double)initial_width  / (double)this.configuration.FieldWidth;
    double heightRatio = (double)initial_height / (double)this.configuration.FieldLength;
    
    double center_x_in_field = center_x * widthRatio;
    double center_y_in_field = center_y * heightRatio;
    
    return new Point((int)(field_center_x + center_x_in_field), (int)(field_center_y + center_y_in_field));
  }
  
  private Rectangle translateToField(double x, double y, double width, double height) {
    double widthRatio = (double)field.getWidth() / (double)this.configuration.FieldWidth;
    double heightRatio = (double)field.getHeight() / (double)this.configuration.FieldLength;
    
    double x_in_field = x * widthRatio;
    double y_in_field = y * heightRatio;
    double width_in_field = width * widthRatio;
    double height_in_field = height * heightRatio;
    
    return new Rectangle(x_in_field, y_in_field, width_in_field, height_in_field);
  }
    
  private Rectangle getField() {
    int width = (int)this.getWidth();
    int height = (int)this.getHeight();
    
    double fieldRatio = (double)this.configuration.FieldLength / (double)this.configuration.FieldWidth;
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
