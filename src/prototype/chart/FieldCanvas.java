package prototype.chart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
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
import prototype.object.CombinedBall;
import prototype.object.CombinedOpponent;

public class FieldCanvas extends Pane implements Chart{
  private static double TRANSPARENCY = 0.35;
  private List<Turtle> data;
  private Range<Integer> selection;
  private boolean forward;
  private Rectangle field;
  private List<SelectionEventListener> listenerList = new ArrayList();
  
  public boolean liveUpdate;  
  public int[] selectedTurtles;
  public boolean turtleHistory;
  
  private DockNode dockNode;
  
  private final List<Circle> shape_ball;
  private final List<Pair<Polygon, Text>> shape_turtles;
  private List<Rectangle> shape_opponents;
  private List<Path> paths = new ArrayList();
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
  
  private ParameterMap parameterMap;
  private Configuration configuration;
  
  private boolean showTurtlePerspective;
  private boolean showOpponentPerspective;
  private boolean showBallPerspective;
  private int[] viewTurtleIds;
  private double viewOpponentId;
  private Point2D source;
  private Map<Integer, List<Point2D>> targets;
  private List<Path> hoverPaths = new ArrayList();
  
  public FieldCanvas(List<Turtle> data, boolean liveUpdate, int[] selectedTurtles, boolean turtleHistory) {
    this.data = data;
    
    this.selection = new Range(0, 0);
    this.forward = true;
    
    this.liveUpdate = liveUpdate;
    this.turtleHistory = turtleHistory;
    this.selectedTurtles = selectedTurtles;
    
    this.configuration = new Configuration();
    this.parameterMap = new ParameterMap();
    
    this.shape_ball = new ArrayList();
    this.shape_opponents = new ArrayList();
    this.shape_turtles = new ArrayList();
    this.hoverPaths = new ArrayList();
  
    this.showTurtlePerspective = false;
    this.showOpponentPerspective = false;
    this.showBallPerspective = false;
    this.viewTurtleIds = new int[0];
    
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
    
  public FieldCanvas(List<Turtle> data, int selectionStart, int selectionEnd, boolean forward) {    
    this(data, true, new int[]{0, 1, 2, 3, 4, 5, 6 }, false);
    
    this.selection = new Range(selectionStart, selectionEnd);
    this.forward = forward;
    drawMovingShapes();
  }
  
  @Override
  public void updateData(List<Turtle> data) {
    this.data = data;
    this.selection = new Range(0, 0);
    this.forward = true;
   
    this.configuration = new Configuration();
    this.parameterMap = new ParameterMap();
    
    drawMovingShapes();
  }
  
  @Override
  public void selectFrames(int startIndex, int endIndex, boolean drag, boolean forward) {
    if((!drag || liveUpdate) && data.size() > 0) {
      this.selection = new Range(startIndex, endIndex);
      this.forward = forward;
      
      this.showTurtlePerspective = false;
      this.showOpponentPerspective = false;
      this.showBallPerspective = false;
      this.source = null;
      this.targets = new HashMap();
            
      drawMovingShapes();
      setDockTitle();
    } else {
      this.selection = new Range(0, 0);
      this.forward = true;
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
    return new FieldCanvas(data, liveUpdate, selectedTurtles, turtleHistory);
  }
    
  @Override
  public void addSelectionEventListener(SelectionEventListener listener) {
    listenerList.add(listener);
  }
  
  @Override
  public void update() {
    drawMovingShapes();
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
        
    grid.add(liveCheckbox, 0, 0);
    
    grid.add(new Label("Turtles"), 0, 1);
    grid.add(turtleCheckbox, 0, 2);
    grid.add(turtleListView, 0, 3);

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
    if(field != null && selection != null) {
      
      int index = 0;
      if(this.forward)
        index = selection.GetMax();
      else
        index = selection.GetMin();
      
      targets = new HashMap();
      
      drawTurtleLines();

      drawOpponents(index); 
      
      drawturtles(index);

      drawBall(index);

      drawHoverPaths();
      
      resizePanel();
    }
  }
  
  private void drawturtles(int index) {
    for(Pair<Polygon, Text> turtle_shape : shape_turtles) {
      this.getChildren().remove(turtle_shape.getKey());
      this.getChildren().remove(turtle_shape.getValue());
    }
    shape_turtles.clear();
    
    if(data != null && data.size() > 0) {
      try {
        List<Integer> visible = getVisibleTurtles();
        
        for(int selectedTurtle : selectedTurtles) {
          Turtle turtle  = data.get(selectedTurtle);

          DataPoint inField = turtle.GetValue(this.configuration.RobotInField, 0, this.configuration.RobotInFieldIndex, index);
          DataPoint poseX = turtle.GetValue(this.configuration.Pose, 0, this.configuration.PoseX, index);
          DataPoint poseY = turtle.GetValue(this.configuration.Pose, 0, this.configuration.PoseY, index);
          DataPoint orientationValue = turtle.GetValue(this.configuration.Pose, 0, this.configuration.PoseRot, index);

          if(inField != null && inField.getValue() > 0) {
            Point2D turtlePos = getPosition(poseX.getValue(), poseY.getValue());
            double orientation = orientationValue.getValue();

            int turtleId = turtle.getID();         

            Polygon turtlePolygon = createTurtleShape(turtleId);

            turtlePolygon.setLayoutX(turtlePos.getX() - 15);
            turtlePolygon.setLayoutY(turtlePos.getY() - 12.5);
            turtlePolygon.setRotate(Math.toDegrees(orientation));

            Text turtleText = new Text(turtlePos.getX() - 5, turtlePos.getY() + 5, String.valueOf(turtleId + 1));

            turtleText.setFill(Color.WHITE);
            turtleText.setRotate(Math.toDegrees(orientation));

            if(!visible.contains(selectedTurtle)) {
              turtlePolygon.setOpacity(TRANSPARENCY);
              turtleText.setOpacity(TRANSPARENCY);
            } else {
              if(!targets.containsKey(selectedTurtle))
                targets.put(selectedTurtle, new ArrayList());
              targets.get(selectedTurtle).add(turtlePos);
            }
            
            shape_turtles.add(new Pair(turtlePolygon, turtleText));
            this.getChildren().add(turtlePolygon);
            this.getChildren().add(turtleText);
            
            turtlePolygon.setOnMouseMoved(new EventHandler() {
              @Override
              public void handle(Event event) {
                if(source != turtlePos) {
                  viewTurtleIds = new int[] { turtleId };
                showTurtlePerspective = true;
                showOpponentPerspective = false;
                showBallPerspective = false;
                  
                  source = turtlePos;
                  targets = new HashMap();
                
                  drawMovingShapes();
                }
              }
            });
            
          }
        }
      } catch(Exception ex) {
        ex.printStackTrace();        
      }
    }
  }
  
  private Polygon createTurtleShape(int turtleId) {
    Polygon polygon = new Polygon();
    polygon.getPoints().addAll(new Double[]{
        0.0, 0.0,
        30.0, 0.0,
        30.0, 5.0,
        30.0, 15.0,
        15.0, 25.0,
        0.0, 15.0,
        0.0, 5.0 });
    polygon.getStyleClass().add(String.format("default-color%d-agent", turtleId));
    polygon.setStrokeWidth(1);
    
    return polygon;
  }
  
  private void drawOpponents(int index) {
    
    for(Rectangle opponent_shape : shape_opponents) {
      this.getChildren().remove(opponent_shape);
    }
    shape_opponents.clear();
    
    if(selection != null && data != null && data.size() > 0) {
      try {
        Map<Integer, List<Point2D>> turltePoints = new HashMap();
        
        for(int turtleIndex : selectedTurtles) {
          turltePoints.put(turtleIndex, new ArrayList());
          
          Turtle turtle  = data.get(turtleIndex);

          Parameter parameter = this.parameterMap.GetParameter(this.configuration.Opponent);

          for(int i = 0; i < parameter.getCount(); i++) {

            DataPoint opponentX = turtle.GetValue(this.configuration.Opponent, i, this.configuration.OpponentX, index);
            DataPoint opponentY = turtle.GetValue(this.configuration.Opponent, i, this.configuration.OpponentY, index);
            
            if(opponentX != null && opponentX.getValue() != 0 && opponentY.getValue() != 0) {
              Point2D opponentPos = getPosition(opponentX.getValue(), opponentY.getValue());
              turltePoints.get(turtleIndex).add(opponentPos);
            }
          }
        }
        
        List<CombinedOpponent> filtered = filterOpponents(turltePoints, 20);      
        List<Integer> visible = getVisibleTurtles();

        for(CombinedOpponent newPoint : filtered) {
          int[] pointTurtles = newPoint.getTurtles();
          double opacity = 1;
          Point2D opponentPos = newPoint.getAveragePosition();
          
          EventHandler mouseMove = new EventHandler() {
            @Override
            public void handle(Event event) {
              if(source != opponentPos) {
                showTurtlePerspective = false;
                showOpponentPerspective = true;
                showBallPerspective = false;
                
                viewTurtleIds = newPoint.getTurtles();
                viewOpponentId = newPoint.getId();

                source = newPoint.getAveragePosition();
                targets = new HashMap();

                drawMovingShapes();
              }
            }
          };
          
          if((showOpponentPerspective && viewOpponentId != newPoint.getId()) || showBallPerspective) {
            opacity = TRANSPARENCY;
          } else {
            boolean contains = false;
            for(int i = 0; i < pointTurtles.length; i++) {
              if(visible.contains(pointTurtles[i])) {
                contains = true;
              }
            }

            if(!contains) {
              opacity = TRANSPARENCY;
            }
          }
          
          if(opacity == 1) {
            for(int i = 0; i < viewTurtleIds.length; i++) {
              if(!targets.containsKey(viewTurtleIds[i]))
                targets.put(viewTurtleIds[i], new ArrayList());
              targets.get(viewTurtleIds[i]).add(opponentPos);
            }
          }
          
          for(Rectangle shape : newPoint.createShape(opacity)) {            
            this.getChildren().add(shape);
            shape.setOnMouseMoved(mouseMove);            
            shape_opponents.add(shape);
          }
        }      
      } catch(Exception ex) {
        ex.printStackTrace();        
      }
    }
  }
    
  private void drawBall(int index) {
    for(Circle ball_shape : shape_ball) {
      this.getChildren().remove(ball_shape);
    }
    shape_ball.clear();
    
    if(selection != null && data != null && data.size() > 0) {      
      try {
        Map<Integer, Point2D> turltePoints = new HashMap();

        for(int turtleIndex : selectedTurtles) {
          Turtle turtle  = data.get(turtleIndex);

          DataPoint ballFound = turtle.GetValue(this.configuration.BallFound, 0, this.configuration.BallFoundIndex, index);
          DataPoint ballX = turtle.GetValue(this.configuration.Ball, 0, this.configuration.BallX, index);
          DataPoint ballY = turtle.GetValue(this.configuration.Ball, 0, this.configuration.BallY, index);

          if(ballFound != null&& ballFound.getValue() > 0 && ballX.getValue() != 0 && ballY.getValue() != 0) {
            Point2D ballPos = getPosition(ballX.getValue(), ballY.getValue());
            turltePoints.put(turtleIndex, ballPos);
          }
        }

        List<CombinedBall> filtered = filterBall(turltePoints, 8);
        List<Integer> visible = getVisibleTurtles();

        for(CombinedBall newPoint : filtered) {
          Point2D averagePos = newPoint.getAveragePosition();
          int[] pointTurtles = newPoint.getTurtles();
          Circle ball = new Circle(averagePos.getX(), averagePos.getY(), 7.0f, Color.ORANGE);

          double opacity = 1;
          
          if(showOpponentPerspective) {
            opacity = TRANSPARENCY;
          } else {
            boolean contains = false;
            for(int i = 0; i < pointTurtles.length; i++) {
              if(visible.contains(pointTurtles[i])) {
                contains = true;
              }
            }

            if(!contains) {
              opacity = TRANSPARENCY;
            }
          }
          
          if(opacity == 1) {
            for(int i = 0; i < viewTurtleIds.length; i++) {
              if(!targets.containsKey(viewTurtleIds[i]))
                targets.put(viewTurtleIds[i], new ArrayList());
              targets.get(viewTurtleIds[i]).add(averagePos);
            }
          }
          
          ball.setStroke(Color.DARKORANGE);
          ball.setOpacity(opacity);
          shape_ball.add(ball);
          this.getChildren().add(ball);

          ball.setOnMouseMoved(new EventHandler() {
              @Override
              public void handle(Event event) {
                if(source != averagePos) {
                  viewTurtleIds = pointTurtles;
                showTurtlePerspective = false;
                showOpponentPerspective = false;
                showBallPerspective = true;
                  
                  source = averagePos;
                  targets = new HashMap();
                  
                  drawMovingShapes();
                }
              }
            });            
        }        
      } catch(Exception ex) {
        ex.printStackTrace();        
      }
    }
  }
  
  private List<CombinedOpponent> filterOpponents(Map<Integer, List<Point2D>> opponentPoints, double distance) {
    List<CombinedOpponent> combinedPoints = new ArrayList();
    
    for(Entry<Integer, List<Point2D>> points : opponentPoints.entrySet()) {
      int turtle = points.getKey();
      
      for(CombinedOpponent combination : combinedPoints) {
        if(!combination.containsTurtle(turtle)) {
          
          Point2D found = null;
          for(Point2D point : points.getValue()) {
            if(combination.distance(point) <= distance) {
              try {
                if(found == null || combination.distance(point) < found.distance(point)) {
                  found = point;
                }
                break;
              } catch (Exception ex) {
                Logger.getLogger(FieldCanvas.class.getName()).log(Level.SEVERE, null, ex);
              }
            }
          }

          if(found != null) {
           try {
              combination.addTurtle(turtle, found);
              points.getValue().remove(found);
            } catch (Exception ex) {
              Logger.getLogger(FieldCanvas.class.getName()).log(Level.SEVERE, null, ex);
            }
          }
        }
      }

      for(Point2D point : points.getValue()) {
        double id = point.getX();
        CombinedOpponent newCombined = new CombinedOpponent(id, turtle, point);
        combinedPoints.add(newCombined);
      }
    }
    
    return combinedPoints;
  }
  
  private List<CombinedBall> filterBall(Map<Integer, Point2D> ballPoints, double distance) {
    List<CombinedBall> combinedPoints = new ArrayList();
    
    for(Entry<Integer, Point2D> points : ballPoints.entrySet()) {
      int turtle = points.getKey();    
      Point2D point = points.getValue();
      
      boolean added = false;
      for(CombinedBall combination : combinedPoints) {
        if(!combination.containsTurtle(turtle)) {      
          
          if(combination.distance(point) <= distance) {
           try {
              combination.addTurtle(turtle, point);
              added = true;
            } catch (Exception ex) {
              Logger.getLogger(FieldCanvas.class.getName()).log(Level.SEVERE, null, ex);
            }
          }
        }
      }

      if(!added) {
        double id = point.getX();
        CombinedBall newCombined = new CombinedBall(id, turtle, point);
        combinedPoints.add(newCombined);
      }
    }
    
    return combinedPoints;
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
      
      EventHandler mouseMove = new EventHandler() {
        @Override
        public void handle(Event event) {
          if(showTurtlePerspective || showOpponentPerspective || showBallPerspective) {
            showTurtlePerspective = false;
            showOpponentPerspective = false;
            showBallPerspective = false;
            source = null;
            targets = new HashMap();
            drawMovingShapes();
          }
        }
      };
      
      shape_field.setOnMouseMoved(mouseMove);
      shape_goal1.setOnMouseMoved(mouseMove);
      shape_goal2.setOnMouseMoved(mouseMove);
      shape_goalarea1.setOnMouseMoved(mouseMove);
      shape_goalarea2.setOnMouseMoved(mouseMove);
      shape_penalty1.setOnMouseMoved(mouseMove);
      shape_penalty2.setOnMouseMoved(mouseMove);
      shape_penaltyspot1.setOnMouseMoved(mouseMove);
      shape_penaltyspot2.setOnMouseMoved(mouseMove);
      shape_fieldcenter.setOnMouseMoved(mouseMove);
      shape_fieldcenteroval.setOnMouseMoved(mouseMove);
    }
  }
  
  private void drawTurtleLines() {
    removePaths();
    
    if(turtleHistory && selection != null && data != null && data.size() > 0) {
      List<Pair<Integer, List<Point2D>>> turtle_paths = new ArrayList();

      for(int selectedTurtle : getVisibleTurtles()) {
        Turtle turtle  = data.get(selectedTurtle);

        Parameter parameter = this.parameterMap.GetParameter(this.configuration.Pose);
            
        for(int parameterIndex = 0; parameterIndex < parameter.getCount(); parameterIndex++) {

          List<DataPoint> posXValues = turtle.GetValues(this.configuration.Pose, parameterIndex, this.configuration.PoseX, selection.GetMin(), selection.GetMax());
          List<DataPoint> posYValues = turtle.GetValues(this.configuration.Pose, parameterIndex, this.configuration.PoseY, selection.GetMin(), selection.GetMax());          
          
          List<Point2D> newPath = new ArrayList();

          for(int valueIndex = 0; valueIndex < posXValues.size(); valueIndex++) {
            DataPoint dataX = posXValues.get(valueIndex);
            DataPoint dataY = posYValues.get(valueIndex);

            double posX = dataX.getValue();
            double posY = dataY.getValue();
            
            if(dataX.isVisible() && dataY.isVisible() && posX != 0 && posY != 0) {   
              Point2D position = getPosition(posX, posY);
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
  
  private void drawPaths(List<Pair<Integer, List<Point2D>>> paths, String style) {
    if(selection != null) {      
      for(Pair<Integer, List<Point2D>> points : paths) {
        if(points.getValue().size() > 0) {
          Path path = new Path();
          path.getStyleClass().add(String.format(style, (int)points.getKey()));
          path.setStrokeWidth(2);
          
          MoveTo moveTo = new MoveTo(points.getValue().get(0).getX(), points.getValue().get(0).getY());
          path.getElements().add(moveTo); 

          for(int i = 1; i < points.getValue().size(); i++) {
            Point2D point = points.getValue().get(i);
            LineTo lineTo = new LineTo(point.getX(), point.getY());
            path.getElements().add(lineTo);
          }

          this.paths.add(path);
          this.getChildren().add(11, path);
        }
      }
    }
  }
  
  private void drawHoverPaths() {
    for(Path path : hoverPaths) {
      this.getChildren().remove(path);
    }
    hoverPaths.clear();
    
    if((showTurtlePerspective || showOpponentPerspective || showBallPerspective) && source != null) {
      for(int turtle : targets.keySet()) {
        for(Point2D point : targets.get(turtle)) {
          
          Path path = new Path();
          path.getStyleClass().add(String.format("default-color%d-agent-line", turtle));
          path.setStrokeWidth(2);
          path.setOpacity(0.6);
          
          MoveTo moveTo = new MoveTo(source.getX(), source.getY());
          path.getElements().add(moveTo); 

          LineTo lineTo = new LineTo(point.getX(), point.getY());
          path.getElements().add(lineTo);

          
          this.hoverPaths.add(path);
          this.getChildren().add(11, path);        
        }
      }
    }
  }
  
  private List<Integer> getVisibleTurtles() {
    List<Integer> results = new ArrayList();
    if(showTurtlePerspective || showOpponentPerspective || showBallPerspective) {
      for (int index = 0; index < viewTurtleIds.length; index++) {
        results.add(viewTurtleIds[index]);
      }
    } else {
      for (int index = 0; index < selectedTurtles.length; index++) {
        results.add(selectedTurtles[index]);
      }
    }
    return results;
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
  
  private Point2D getPosition(double center_x, double center_y) {
    
    double field_center_x = (double)initial_width / 2;
    double field_center_y = (double)initial_height / 2;
    
    double widthRatio = (double)initial_width  / (double)this.configuration.FieldWidth;
    double heightRatio = (double)initial_height / (double)this.configuration.FieldLength;
    
    double center_x_in_field = center_x * widthRatio;
    double center_y_in_field = center_y * heightRatio;
    
    return new Point2D(field_center_x + center_x_in_field, field_center_y + center_y_in_field);
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
