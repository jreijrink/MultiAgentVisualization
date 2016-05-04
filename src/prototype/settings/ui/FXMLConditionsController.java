package prototype.settings.ui;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
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
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import prototype.object.Condition;
import prototype.settings.DataGeneration;

public class FXMLConditionsController implements Initializable {

  @FXML private ListView valueConditions;
  
  private BooleanProperty conditionSelected = new SimpleBooleanProperty(false);  
  public BooleanProperty conditionSelectedProperty() { return this.conditionSelected; }
  public void setConditionSelected(boolean enabled) { this.conditionSelected.set(enabled); }
  public boolean getConditionSelected() { return this.conditionSelected.getValue(); }
  
  private BooleanProperty selectedEnabled = new SimpleBooleanProperty(true);  
  public BooleanProperty selectedEnabledProperty() { return this.selectedEnabled; }
  public void setSelectedEnabled(boolean enabled) { this.selectedEnabled.set(enabled); }
  public boolean getSelectedEnabled() { return this.selectedEnabled.getValue(); }
  
  private Stage dialogStage;
  private Condition selectedCondition;
  private ObservableList<Condition> conditions;
  private boolean changed = false;
  
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    conditions = FXCollections.observableArrayList(DataGeneration.loadConditions());
    Collections.sort(conditions, (o1, o2) -> o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase()));
    valueConditions.setItems(conditions);
    
    valueConditions.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        Object item = valueConditions.getSelectionModel().getSelectedItem();
        if(item != null) {
          selectedCondition = (Condition)item;
          setConditionSelected(true);
        } else {
          selectedCondition = null;
          setConditionSelected(false);
        }
      }
    });
  }
  
  public void setDialogStage(Stage dialogStage) {
    this.dialogStage = dialogStage;
  }

  public void disableSelection() {
    setSelectedEnabled(false);
  }
  
  @FXML
  private void addConditionAction() {
    Condition newCondition = new Condition();
    if(showConditionDialog(newCondition)) {
      conditions.add(newCondition);
      update();
    }
  }

  @FXML
  private void editConditionAction() {
    if(selectedCondition != null) {
      if(showConditionDialog(selectedCondition)) {
       update();
      }
    }
  }

  @FXML
  private void deleteConditionAction() {
    if(selectedCondition != null) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("Confirmation Dialog");
      alert.setHeaderText("Please confirm");
      alert.setContentText("Sure you want to delete?");
      if (alert.showAndWait().get() == ButtonType.OK) {
        conditions.remove(selectedCondition);
        update();
      }
    }
  }
  
  @FXML
  private void selectionConditionAction() {
    dialogStage.close();
  }
  
  @FXML
  private void cancelSelectionAction() {
    selectedCondition = null;
    dialogStage.close();
  }
  
  private void update() {
    Collections.sort(conditions, (o1, o2) -> o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase()));
    valueConditions.setItems(conditions);
    DataGeneration.saveConditions(conditions);
    this.changed = true;
  }
  
  public Condition getCondition() {
    return selectedCondition;
  }
  
  public boolean hasChanged() {
    return this.changed;
  }
  
  private boolean showConditionDialog(Condition condition) {
    try {
      FXMLLoader loader = new FXMLLoader(FXMLConditionController.class.getResource("FXMLCondition.fxml"));
      AnchorPane page = (AnchorPane) loader.load();
      Stage dialogStage = new Stage();
      dialogStage.setTitle("Condition");
      dialogStage.initModality(Modality.WINDOW_MODAL);
      dialogStage.initOwner(this.dialogStage);
      Scene scene = new Scene(page);
      dialogStage.setScene(scene);

      dialogStage.setResizable(false);

      FXMLConditionController controller = loader.getController();
      controller.setDialogStage(dialogStage);
      controller.setCondition(this.conditions, condition);

      dialogStage.showAndWait();

      return controller.isSaveClicked();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }
}