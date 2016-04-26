package prototype.settings.ui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import prototype.object.Parameter;
import prototype.settings.Configuration;
import prototype.object.ParameterMap;
import prototype.object.Value;

public class FXMLConfigurationController implements Initializable {

  @FXML private TextField maxTurtleTextField;
  @FXML private TextField maxOpponentsTextField;
  
  @FXML private TextField penaltyWidthTextField;
  @FXML private TextField penaltyLengthTextField;
  @FXML private TextField penaltySpotTextField;
  
  @FXML private TextField fieldWidthTextField;
  @FXML private TextField fieldLengthTextField;
  @FXML private TextField goalAreaWidthTextField;
  @FXML private TextField goalAreaLengthTextField;
  @FXML private TextField goalWidthTextField;
  @FXML private TextField goalDepthTextField;
  
  @FXML private ChoiceBox turtlePoseChoiceBox;
  @FXML private ChoiceBox turtlePoseXChoiceBox;
  @FXML private ChoiceBox turtlePoseYChoiceBox;
  @FXML private ChoiceBox turtlePoseRotChoiceBox;
  
  @FXML private ChoiceBox turtleInFieldChoiceBox;
  @FXML private ChoiceBox turtleInFieldIndexChoiceBox;
  
  @FXML private ChoiceBox OponnentPosesChoiceBox;
  @FXML private ChoiceBox OponnentPosesXChoiceBox;
  @FXML private ChoiceBox OponnentPosesYChoiceBox;
  
  @FXML private ChoiceBox OpponentLabelsChoiceBox;
  @FXML private ChoiceBox OpponentLabelsIndexChoiceBox;
  
  @FXML private ChoiceBox ballPoseChoiceBox;
  @FXML private ChoiceBox ballPoseXChoiceBox;
  @FXML private ChoiceBox ballPoseYChoiceBox;  
  
  @FXML private ChoiceBox ballFoundChoiceBox;
  @FXML private ChoiceBox ballFoundIndexChoiceBox;
  
  private Stage dialogStage;
  private Configuration configuration;
  private ParameterMap parameterMap;
  private boolean changes = false;
  
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    this.configuration = new Configuration();
    this.parameterMap = new ParameterMap();
    
    maxTurtleTextField.setText(String.valueOf(configuration.MaxTurtles));
    maxOpponentsTextField.setText(String.valueOf(configuration.MaxOpponents));
    
    penaltyWidthTextField.setText(String.valueOf(configuration.PenaltyWidth));
    penaltyLengthTextField.setText(String.valueOf(configuration.PenaltyLength));
    penaltySpotTextField.setText(String.valueOf(configuration.PenaltySpot));
    fieldWidthTextField.setText(String.valueOf(configuration.FieldWidth));
    fieldLengthTextField.setText(String.valueOf(configuration.FieldLength));
    goalAreaWidthTextField.setText(String.valueOf(configuration.GoalAreaWidth));
    goalAreaLengthTextField.setText(String.valueOf(configuration.GoalAreaLength));
    goalWidthTextField.setText(String.valueOf(configuration.GoalWidth));
    goalDepthTextField.setText(String.valueOf(configuration.GoalDepth));
    
    turtlePoseChoiceBox.getItems().setAll(this.parameterMap.GetAllParameters());
    if(this.parameterMap.ContainsParameter(configuration.Pose)) {
      Parameter parameter = this.parameterMap.GetParameter(configuration.Pose);
      turtlePoseChoiceBox.setValue(parameter);
      setValues(turtlePoseXChoiceBox, parameter, parameter.getValue(configuration.PoseX));
      setValues(turtlePoseYChoiceBox, parameter, parameter.getValue(configuration.PoseY));
      setValues(turtlePoseRotChoiceBox, parameter, parameter.getValue(configuration.PoseRot));
    }
    turtlePoseChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        Parameter selected = (Parameter)turtlePoseChoiceBox.getSelectionModel().getSelectedItem();
        setValues(turtlePoseXChoiceBox, selected, null);
        setValues(turtlePoseYChoiceBox, selected, null);
        setValues(turtlePoseRotChoiceBox, selected, null);
      }
    });
    
    turtleInFieldChoiceBox.getItems().setAll(this.parameterMap.GetAllParameters());
    if(this.parameterMap.ContainsParameter(configuration.RobotInField)) {
      Parameter parameter = this.parameterMap.GetParameter(configuration.RobotInField);
      turtleInFieldChoiceBox.setValue(parameter);      
      setValues(turtleInFieldIndexChoiceBox, parameter, parameter.getValue(configuration.RobotInFieldIndex));
    }
    turtleInFieldChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        Parameter selected = (Parameter)turtleInFieldChoiceBox.getSelectionModel().getSelectedItem();
        setValues(turtleInFieldIndexChoiceBox, selected, null);
      }
    });
    
    OponnentPosesChoiceBox.getItems().setAll(this.parameterMap.GetAllParameters());
    if(this.parameterMap.ContainsParameter(configuration.Opponent)) {
      Parameter parameter = this.parameterMap.GetParameter(configuration.Opponent);
      OponnentPosesChoiceBox.setValue(parameter);      
      setValues(OponnentPosesXChoiceBox, parameter, parameter.getValue(configuration.OpponentX));
      setValues(OponnentPosesYChoiceBox, parameter, parameter.getValue(configuration.OpponentY));
    }
    OponnentPosesChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        Parameter selected = (Parameter)OponnentPosesChoiceBox.getSelectionModel().getSelectedItem();
        setValues(OponnentPosesXChoiceBox, selected, null);
        setValues(OponnentPosesYChoiceBox, selected, null);
      }
    });
    
    OpponentLabelsChoiceBox.getItems().setAll(this.parameterMap.GetAllParameters());
    if(this.parameterMap.ContainsParameter(configuration.Opponentlabelnumber)) {
      Parameter parameter = this.parameterMap.GetParameter(configuration.Opponentlabelnumber);
      OpponentLabelsChoiceBox.setValue(parameter);      
      setValues(OpponentLabelsIndexChoiceBox, parameter, parameter.getValue(configuration.OpponentlabelnumberIndex));
    }
    OpponentLabelsChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        Parameter selected = (Parameter)OpponentLabelsChoiceBox.getSelectionModel().getSelectedItem();
        setValues(OpponentLabelsIndexChoiceBox, selected, null);
      }
    });
    
    ballPoseChoiceBox.getItems().setAll(this.parameterMap.GetAllParameters());
    if(this.parameterMap.ContainsParameter(configuration.Ball)) {
      Parameter parameter = this.parameterMap.GetParameter(configuration.Ball);
      ballPoseChoiceBox.setValue(parameter);      
      setValues(ballPoseXChoiceBox, parameter, parameter.getValue(configuration.BallX));
      setValues(ballPoseYChoiceBox, parameter, parameter.getValue(configuration.BallY));
    }
    ballPoseChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        Parameter selected = (Parameter)ballPoseChoiceBox.getSelectionModel().getSelectedItem();
        setValues(ballPoseXChoiceBox, selected, null);
        setValues(ballPoseYChoiceBox, selected, null);
      }
    });
    
    ballFoundChoiceBox.getItems().setAll(this.parameterMap.GetAllParameters());
    if(this.parameterMap.ContainsParameter(configuration.BallFound)) {
      Parameter parameter = this.parameterMap.GetParameter(configuration.BallFound);
      ballFoundChoiceBox.setValue(parameter);
      setValues(ballFoundIndexChoiceBox, parameter, parameter.getValue(configuration.BallFoundIndex));
    }
    ballFoundChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        Parameter selected = (Parameter)ballFoundChoiceBox.getSelectionModel().getSelectedItem();
        setValues(ballFoundIndexChoiceBox, selected, null);     
      }
    });
  }
  
  public boolean hasChanges() {
    return changes;
  }
  
  private void setValues(ChoiceBox box, Parameter parameter, Value value) {
    box.setDisable(false);
    box.getItems().setAll(parameter.getValues());
    box.setValue(value);
  }

  public void setDialogStage(Stage dialogStage) {
    this.dialogStage = dialogStage;
  }
  
  @FXML
  private void cancelConfigurationAction(ActionEvent event) {
    dialogStage.close();
  }
  
  @FXML
  private void saveConfigurationAction(ActionEvent event) {
    try {
      configuration.MaxTurtles = Integer.valueOf(maxTurtleTextField.getText());
      configuration.MaxOpponents = Integer.valueOf(maxOpponentsTextField.getText());

      configuration.PenaltyWidth = Double.valueOf(penaltyWidthTextField.getText());
      configuration.PenaltyLength = Double.valueOf(penaltyLengthTextField.getText());
      configuration.PenaltySpot = Double.valueOf(penaltySpotTextField.getText());
      configuration.FieldWidth = Double.valueOf(fieldWidthTextField.getText());
      configuration.FieldLength = Double.valueOf(fieldLengthTextField.getText());
      configuration.GoalAreaWidth = Double.valueOf(goalAreaWidthTextField.getText());
      configuration.GoalAreaLength = Double.valueOf(goalAreaLengthTextField.getText());
      configuration.GoalWidth = Double.valueOf(goalWidthTextField.getText());
      configuration.GoalDepth = Double.valueOf(goalDepthTextField.getText());

      configuration.Pose = ((Parameter)turtlePoseChoiceBox.getSelectionModel().getSelectedItem()).getName();
      configuration.PoseX = ((Value)turtlePoseXChoiceBox.getSelectionModel().getSelectedItem()).getName();
      configuration.PoseY = ((Value)turtlePoseYChoiceBox.getSelectionModel().getSelectedItem()).getName();
      configuration.PoseRot = ((Value)turtlePoseRotChoiceBox.getSelectionModel().getSelectedItem()).getName();

      configuration.RobotInField = ((Parameter)turtleInFieldChoiceBox.getSelectionModel().getSelectedItem()).getName();
      configuration.RobotInFieldIndex = ((Value)turtleInFieldIndexChoiceBox.getSelectionModel().getSelectedItem()).getName();

      configuration.Opponent = ((Parameter)OponnentPosesChoiceBox.getSelectionModel().getSelectedItem()).getName();
      configuration.OpponentX = ((Value)OponnentPosesXChoiceBox.getSelectionModel().getSelectedItem()).getName();
      configuration.OpponentY = ((Value)OponnentPosesYChoiceBox.getSelectionModel().getSelectedItem()).getName();

      configuration.Opponentlabelnumber = ((Parameter)OpponentLabelsChoiceBox.getSelectionModel().getSelectedItem()).getName();
      configuration.OpponentlabelnumberIndex = ((Value)OpponentLabelsIndexChoiceBox.getSelectionModel().getSelectedItem()).getName();

      configuration.Ball = ((Parameter)ballPoseChoiceBox.getSelectionModel().getSelectedItem()).getName();
      configuration.BallX = ((Value)ballPoseXChoiceBox.getSelectionModel().getSelectedItem()).getName();
      configuration.BallY = ((Value)ballPoseYChoiceBox.getSelectionModel().getSelectedItem()).getName();

      configuration.BallFound = ((Parameter)ballFoundChoiceBox.getSelectionModel().getSelectedItem()).getName();
      configuration.BallFoundIndex = ((Value)ballFoundIndexChoiceBox.getSelectionModel().getSelectedItem()).getName();

      configuration.Save();
      
      changes = true;
      dialogStage.close();
      
    } catch(Exception ex) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setTitle("Save failed");
      alert.setHeaderText("Saving the configuration failed!");
      alert.setContentText("Please verify the configuration.");
      alert.show();    
    }
  }
  
}
