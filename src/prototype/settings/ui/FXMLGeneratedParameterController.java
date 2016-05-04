package prototype.settings.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import prototype.object.CombinedANDConditions;
import prototype.object.GeneratedParameter;

public class FXMLGeneratedParameterController implements Initializable {

  @FXML private TextField generatedName;
  @FXML private ListView preConditionsListView;
  @FXML private ListView postConditionsSuccessListView;
  @FXML private ListView postConditionsFailedListView;
  
  private BooleanProperty preConditionSelected = new SimpleBooleanProperty(false);  
  public BooleanProperty preConditionSelectedProperty() { return this.preConditionSelected; }
  public void setPreConditionSelected(boolean enabled) { this.preConditionSelected.set(enabled); }
  public boolean getPreConditionSelected() { return this.preConditionSelected.getValue(); }
  
  private BooleanProperty postConditionSuccessSelected = new SimpleBooleanProperty(false);  
  public BooleanProperty postConditionSuccessSelectedProperty() { return this.postConditionSuccessSelected; }
  public void setPostConditionSuccessSelected(boolean enabled) { this.postConditionSuccessSelected.set(enabled); }
  public boolean getPostConditionSuccessSelected() { return this.postConditionSuccessSelected.getValue(); }
  
  private BooleanProperty postConditionFailedSelected = new SimpleBooleanProperty(false);  
  public BooleanProperty postConditionFailedSelectedProperty() { return this.postConditionFailedSelected; }
  public void setPostConditionFailedSelected(boolean enabled) { this.postConditionFailedSelected.set(enabled); }
  public boolean getPostConditionFailedSelected() { return this.postConditionFailedSelected.getValue(); }
  
  private Stage dialogStage;
  private GeneratedParameter parameter;
  private List<GeneratedParameter> parameters;
  private CombinedANDConditions selectedPreCondition;
  private ObservableList<CombinedANDConditions> preConditions;
  private CombinedANDConditions selectedPostConditionSuccess;
  private ObservableList<CombinedANDConditions> postConditionsSuccess;
  private CombinedANDConditions selectedPostConditionFailed;
  private ObservableList<CombinedANDConditions> postConditionsFailed;
  private boolean save = false;
  
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    preConditionsListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        Object item = preConditionsListView.getSelectionModel().getSelectedItem();
        if(item != null) {
          selectedPreCondition = (CombinedANDConditions)item;
          setPreConditionSelected(true);
        } else {
          selectedPreCondition = null;
          setPreConditionSelected(false);
        }
      }
    });
    
    postConditionsSuccessListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        Object item = postConditionsSuccessListView.getSelectionModel().getSelectedItem();
        if(item != null) {
          selectedPostConditionSuccess = (CombinedANDConditions)item;
          setPostConditionSuccessSelected(true);
        } else {
          selectedPostConditionSuccess = null;
          setPostConditionSuccessSelected(false);
        }
      }
    });
    
    postConditionsFailedListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        Object item = postConditionsFailedListView.getSelectionModel().getSelectedItem();
        if(item != null) {
          selectedPostConditionFailed = (CombinedANDConditions)item;
          setPostConditionFailedSelected(true);
        } else {
          selectedPostConditionFailed = null;
          setPostConditionFailedSelected(false);
        }
      }
    });
  }
  
  public void setDialogStage(Stage dialogStage) {
    this.dialogStage = dialogStage;
  }
  
  public void setGeneratedParameter(List<GeneratedParameter> parameters, GeneratedParameter parameter) {
    this.parameters = parameters;
    this.parameter = parameter;
    
    generatedName.setText(parameter.getName());
    
    preConditions = FXCollections.observableArrayList(new ArrayList<CombinedANDConditions>(parameter.getPreConditions()));
    preConditionsListView.setItems(preConditions);
    
    postConditionsSuccess = FXCollections.observableArrayList(new ArrayList<CombinedANDConditions>(parameter.getPostConditionsSuccess()));
    postConditionsSuccessListView.setItems(postConditionsSuccess);
    
    postConditionsFailed = FXCollections.observableArrayList(new ArrayList<CombinedANDConditions>(parameter.getPostConditionsFailed()));
    postConditionsFailedListView.setItems(postConditionsFailed);
  }
  
  @FXML
  private void addPreCondition() {
    CombinedANDConditions newANDConditions = new CombinedANDConditions();
    newANDConditions = showCombinedAndConditionsDialog(preConditions, newANDConditions, "Add pre-condition (AND)");
    if(!newANDConditions.getConditions().isEmpty()) {
      preConditions.add(newANDConditions);
      update();
    }
  }

  @FXML
  private void editPreCondition() {
    if(selectedPreCondition != null) {
      selectedPreCondition = showCombinedAndConditionsDialog(preConditions, selectedPreCondition, "Edit pre-condition (AND)");
      if(selectedPreCondition.getConditions().isEmpty()) {
        preConditions.remove(selectedPreCondition);
      }
      update();
    }
  }

  @FXML
  private void deletePreCondition() {
    if(selectedPreCondition != null) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("Confirmation Dialog");
      alert.setHeaderText("Please confirm");
      alert.setContentText("Sure you want to delete?");
      if (alert.showAndWait().get() == ButtonType.OK) {
        preConditions.remove(selectedPreCondition);
      }
      update();
    }
  }
  
  @FXML
  private void addPostConditionSuccess() {
    CombinedANDConditions newANDConditions = new CombinedANDConditions();
    newANDConditions = showCombinedAndConditionsDialog(postConditionsSuccess, newANDConditions, "Add post-condition Success (AND)");
    if(!newANDConditions.getConditions().isEmpty()) {
      postConditionsSuccess.add(newANDConditions);
    }
    update();
  }

  @FXML
  private void editPostConditionSuccess() {
    if(selectedPostConditionSuccess != null) {
      selectedPostConditionSuccess = showCombinedAndConditionsDialog(postConditionsSuccess, selectedPostConditionSuccess, "Edit post-condition Success (AND)");
      if(selectedPostConditionSuccess.getConditions().isEmpty()) {
        postConditionsSuccess.remove(selectedPostConditionSuccess);
      }
    }
    update();
  }

  @FXML
  private void deletePostConditionSuccess() {
    if(selectedPostConditionSuccess != null) {      
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("Confirmation Dialog");
      alert.setHeaderText("Please confirm");
      alert.setContentText("Sure you want to delete?");
      if (alert.showAndWait().get() == ButtonType.OK) {
        postConditionsSuccess.remove(selectedPostConditionSuccess);
      }
    }
    update();
  }
  
  @FXML
  private void addPostConditionFailed() {
    CombinedANDConditions newANDConditions = new CombinedANDConditions();
    newANDConditions = showCombinedAndConditionsDialog(postConditionsFailed, newANDConditions, "Add post-condition Failed (AND)");
    if(!newANDConditions.getConditions().isEmpty()) {
      postConditionsFailed.add(newANDConditions);
    }
    update();
  }

  @FXML
  private void editPostConditionFailed() {
    if(selectedPostConditionFailed != null) {
      selectedPostConditionFailed = showCombinedAndConditionsDialog(postConditionsFailed, selectedPostConditionFailed, "Edit post-condition Failed (AND)");
      if(selectedPostConditionFailed.getConditions().isEmpty()) {
        postConditionsFailed.remove(selectedPostConditionFailed);
      }
    }
    update();
  }

  @FXML
  private void deletePostConditionFailed() {
    if(selectedPostConditionFailed != null) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("Confirmation Dialog");
      alert.setHeaderText("Please confirm");
      alert.setContentText("Sure you want to delete?");
      if (alert.showAndWait().get() == ButtonType.OK) {
        postConditionsFailed.remove(selectedPostConditionFailed);
      }
    }
    update();
  }
  
  @FXML
  private void saveGeneratedAction() {
    if (isInputValid()) {
      parameter.setName(generatedName.getText());
      
      parameter.setPreConditions(preConditions);
      parameter.setPostConditionsSuccess(postConditionsSuccess);
      parameter.setPostConditionsFailed(postConditionsFailed);
      
      save = true;
      
      dialogStage.close();
    }
  }

  @FXML
  private void cancelGeneratedAction() {
      dialogStage.close();
  }
  
  private void update() {
    preConditionsListView.setItems(preConditions);
    postConditionsSuccessListView.setItems(postConditionsSuccess);
    postConditionsFailedListView.setItems(postConditionsFailed);
    preConditionsListView.refresh();
    postConditionsSuccessListView.refresh();
    postConditionsFailedListView.refresh();
  }
  
  private CombinedANDConditions showCombinedAndConditionsDialog(List<CombinedANDConditions> conditions, CombinedANDConditions ANDConditions, String title) {
    try {
      FXMLLoader loader = new FXMLLoader(FXMLCombinedANDConditionsController.class.getResource("FXMLCombinedANDConditions.fxml"));
      AnchorPane page = (AnchorPane) loader.load();
      Stage dialogStage = new Stage();
      dialogStage.setTitle(title);
      dialogStage.initModality(Modality.WINDOW_MODAL);
      dialogStage.initOwner(this.dialogStage);
      Scene scene = new Scene(page);
      dialogStage.setScene(scene);

      dialogStage.setResizable(false);
      
      FXMLCombinedANDConditionsController controller = loader.getController();
      controller.setDialogStage(dialogStage);
      
      CombinedANDConditions copy = ANDConditions.copy();
      if(conditions.contains(ANDConditions)) {
        conditions.set(conditions.indexOf(ANDConditions), copy);
      }
      controller.setCombinedANDConditions(copy);

      dialogStage.showAndWait();
      
      return copy;
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    return null;
  }
  
  public boolean isSaveClicked() {
    return save;
  }
  
  private boolean isInputValid() {
    String errorMessage = "";

    if (generatedName.getText() == null || generatedName.getText().length() == 0) {
      errorMessage += "No valid name! \n"; 
    }
    
    for(GeneratedParameter existing : this.parameters) {
      if(existing != this.parameter && existing.getName().equals(generatedName.getText())) {
        errorMessage += "Name must be unique! \n"; 
      }
    }

    if (errorMessage.length() == 0) {
        return true;
    } else {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setTitle("Invalid value");
      alert.setHeaderText("Some fields are filled incorrectly");
      alert.setContentText(errorMessage);
      alert.show();
      return false;
    }
  }
}
