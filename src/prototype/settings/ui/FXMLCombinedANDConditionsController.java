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
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import prototype.object.CombinedANDConditions;
import prototype.object.Condition;

public class FXMLCombinedANDConditionsController implements Initializable {

  @FXML private ListView conditionsListView;
  
  private BooleanProperty conditionSelected = new SimpleBooleanProperty(false);  
  public BooleanProperty conditionSelectedProperty() { return this.conditionSelected; }
  public void setConditionSelected(boolean enabled) { this.conditionSelected.set(enabled); }
  public boolean getConditionSelected() { return this.conditionSelected.getValue(); }
  
  private Stage dialogStage;
  private CombinedANDConditions ANDConditions;
  private Condition selectedCondition;
  private ObservableList<Condition> conditions;
  
  @Override
  public void initialize(URL location, ResourceBundle resources) {    
    conditionsListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        Object item = conditionsListView.getSelectionModel().getSelectedItem();
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
    
    this.dialogStage.setOnCloseRequest((WindowEvent event) -> {
      ANDConditions.setConditions(conditions);
    });
  }
  
  public void setCombinedANDConditions(CombinedANDConditions ANDConditions) {
    this.ANDConditions = ANDConditions;
    conditions = FXCollections.observableList(ANDConditions.getConditions());
    update();
  }
  
  @FXML
  private void addConditionAction() {
    Condition newCondition = showConditionDialog();
    if(newCondition != null) {
      conditions.add(newCondition);
      update();
    }
  }

  @FXML
  private void deleteConditionAction() {
    if(selectedCondition != null) {
      Alert alert = new Alert(AlertType.CONFIRMATION);
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
  private void closeANDConditionsAction() {
    ANDConditions.setConditions(conditions);
    dialogStage.close();
  }

  private void update() {
    Collections.sort(conditions, (o1, o2) -> o1.GetName().toLowerCase().compareTo(o2.GetName().toLowerCase()));
    conditionsListView.setItems(conditions);
  }
  
  private Condition showConditionDialog() {
    try {
      FXMLLoader loader = new FXMLLoader(FXMLConditionsController.class.getResource("FXMLConditions.fxml"));
      AnchorPane page = (AnchorPane) loader.load();
      Stage dialogStage = new Stage();
      dialogStage.setTitle("Select condition");
      dialogStage.initModality(Modality.WINDOW_MODAL);
      dialogStage.initOwner(this.dialogStage);
      Scene scene = new Scene(page);
      dialogStage.setScene(scene);

      dialogStage.setResizable(false);

      FXMLConditionsController controller = loader.getController();
      controller.setDialogStage(dialogStage);

      dialogStage.showAndWait();

      return controller.getCondition();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}