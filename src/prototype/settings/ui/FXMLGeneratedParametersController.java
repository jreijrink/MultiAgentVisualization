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
import prototype.object.GeneratedParameter;
import prototype.settings.DataGeneration;

public class FXMLGeneratedParametersController implements Initializable {

  @FXML private ListView generatedParameters;
  
  private BooleanProperty generatedSelected = new SimpleBooleanProperty(false);  
  public BooleanProperty generatedSelectedProperty() { return this.generatedSelected; }
  public void setGeneratedSelected(boolean enabled) { this.generatedSelected.set(enabled); }
  public boolean getGeneratedSelected() { return this.generatedSelected.getValue(); }
  
  private Stage dialogStage;
  private GeneratedParameter selectedGenerated;
  private ObservableList<GeneratedParameter> generated;
  private boolean changed = false;
  
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    generated = FXCollections.observableArrayList(DataGeneration.loadGenerated());
    Collections.sort(generated, (o1, o2) -> o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase()));
    generatedParameters.setItems(generated);    
    
    generatedParameters.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        Object item = generatedParameters.getSelectionModel().getSelectedItem();
        if(item != null) {
          selectedGenerated = (GeneratedParameter)item;
          setGeneratedSelected(true);
        } else {
          selectedGenerated = null;
          setGeneratedSelected(false);
        }
      }
    });
  }
  
  public void setDialogStage(Stage dialogStage) {
    this.dialogStage = dialogStage;
  }

  @FXML
  private void addGeneratedAction() {
    GeneratedParameter selectedGenerated = new GeneratedParameter("");
    if(showGeneratedParameterDialog(selectedGenerated)) {
      generated.add(selectedGenerated);
      update();
    }
  }

  @FXML
  private void editGeneratedAction() {
    if(selectedGenerated != null) {
      if(showGeneratedParameterDialog(selectedGenerated)) {
       update();
      }
    }
  }

  @FXML
  private void deleteGeneratedAction() {
    if(selectedGenerated != null) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("Confirmation Dialog");
      alert.setHeaderText("Please confirm");
      alert.setContentText("Sure you want to delete?");
      if (alert.showAndWait().get() == ButtonType.OK) {
        generated.remove(selectedGenerated);
        update();
      }
    }
  }
  
  private void update() {
    Collections.sort(generated, (o1, o2) -> o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase()));
    generatedParameters.setItems(generated);
    DataGeneration.saveGenerated(generated);
    changed = true;
  }
  
  public boolean hasChanged() {
    return changed;
  }
  
  private boolean showGeneratedParameterDialog(GeneratedParameter selectedGenerated) {
    try {
      FXMLLoader loader = new FXMLLoader(FXMLGeneratedParameterController.class.getResource("FXMLGeneratedParameter.fxml"));
      AnchorPane page = (AnchorPane) loader.load();
      Stage dialogStage = new Stage();
      dialogStage.setTitle("Derive Attribute");
      dialogStage.initModality(Modality.WINDOW_MODAL);
      dialogStage.initOwner(this.dialogStage);
      Scene scene = new Scene(page);
      dialogStage.setScene(scene);

      dialogStage.setResizable(false);

      FXMLGeneratedParameterController controller = loader.getController();
      controller.setDialogStage(dialogStage);
      controller.setGeneratedParameter(this.generated, selectedGenerated);

      dialogStage.showAndWait();

      return controller.isSaveClicked();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }
}