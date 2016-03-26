package prototype.settings.ui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import prototype.object.Category;

public class FXMLCategoryController implements Initializable {

  @FXML private TextField categoryValue;
  @FXML private TextField categoryName;
  
  private Stage dialogStage;
  private Category category;
  private boolean save = false;
  
  @Override
  public void initialize(URL location, ResourceBundle resources) {
  }
  
  public void setDialogStage(Stage dialogStage) {
    this.dialogStage = dialogStage;
  }

  public void setCategory(Category category) {
    this.category = category;
    
    categoryValue.setText(String.valueOf(category.getValue()));
    categoryName.setText(category.getName());
  }
  
  @FXML
  private void saveCategoryAction() {
    if (isInputValid()) {
      category.setValue(Integer.valueOf(categoryValue.getText()));
      category.setName(categoryName.getText());

      save = true;
      
      dialogStage.close();
    }
  }

  @FXML
  private void cancelCategoryAction() {
      dialogStage.close();
  }
  
  public boolean isSaveClicked() {
    return save;
  }

  private boolean isInputValid() {
    String errorMessage = "";

    if (categoryName.getText() == null || categoryName.getText().length() == 0) {
      errorMessage += "No valid name! \n"; 
    }
    if (categoryValue.getText() == null || categoryValue.getText().length() == 0) {
      errorMessage += "No valid value! \n"; 
    }
    try {
      int value = Integer.parseInt(categoryValue.getText());
      if(value < 0) {
        errorMessage += "Value must be zero or higher! \n";              
      }
    } catch(Exception ex) {
      errorMessage += "Value is not a number! \n";       
    }
    
    if (errorMessage.length() == 0) {
        return true;
    } else {
      Alert alert = new Alert(AlertType.WARNING);
      alert.setTitle("Invalid category");
      alert.setHeaderText("Some fields are filled incorrectly");
      alert.setContentText(errorMessage);
      alert.show();
      return false;
    }
  }
}

