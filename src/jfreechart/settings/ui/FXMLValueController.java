package jfreechart.settings.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfreechart.object.Category;
import jfreechart.object.Type;
import jfreechart.object.Value;

public class FXMLValueController implements Initializable {

  @FXML private TextField valueName;
  @FXML private TextField valueIndex;
  @FXML private TextField valueUnit;
  @FXML private TextField valueMask;
  
  @FXML private CheckBox valueRangeEnabled;
  @FXML private TextField valueMin;
  @FXML private TextField valueMax; 
  
  @FXML private ListView valueCategories;
  
  private BooleanProperty rangeEnabled = new SimpleBooleanProperty(false);  
  public BooleanProperty rangeEnabledProperty() { return this.rangeEnabled; }
  public void setRangeEnabled(boolean enabled) { this.rangeEnabled.set(enabled); }
  public boolean getRangeEnabled() { return this.rangeEnabled.getValue(); }
  
  private BooleanProperty categoriesEnabled = new SimpleBooleanProperty(false);  
  public BooleanProperty categoriesEnabledProperty() { return this.categoriesEnabled; }
  public void setCategoriesEnabled(boolean enabled) { this.categoriesEnabled.set(enabled); }
  public boolean getCategoriesEnabled() { return this.categoriesEnabled.getValue(); }
  
  private BooleanProperty categoriesSelected = new SimpleBooleanProperty(false);  
  public BooleanProperty categoriesSelectedProperty() { return this.categoriesSelected; }
  public void setCategoriesSelected(boolean enabled) { this.categoriesSelected.set(enabled); }
  public boolean getCategoriesSelected() { return this.categoriesSelected.getValue(); }
  
  private Stage dialogStage;
  private Value value;
  private boolean save = false;
  Category selectedCategory;
  
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    
    valueCategories.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        Object item = valueCategories.getSelectionModel().getSelectedItem();
        if(item != null) {
          selectedCategory = (Category)item;
          setCategoriesSelected(true);
        } else {
          selectedCategory = null;
          setCategoriesSelected(false);
        }
      }
    });
  }
  
  public void setDialogStage(Stage dialogStage) {
    this.dialogStage = dialogStage;
  }

  public void setValue(Value value, Type parameterType) {
    this.value = value;
    
    valueRangeEnabled.selectedProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        setRangeEnabled(valueRangeEnabled.isSelected());
      }
    });
    
    valueName.setText(value.getName());
    valueIndex.setText(String.valueOf(value.getIndex()));
    valueUnit.setText(value.getUnit());
    
    valueMask.setText(value.getDecimalmask());
    
    valueRangeEnabled.setSelected(value.getRangeEnabled());
    valueMin.setText(String.valueOf(value.getMin()));
    valueMax.setText(String.valueOf(value.getMax()));
    
    if(parameterType == Type.Categorical)
      setCategoriesEnabled(true);
    
    valueCategories.getItems().setAll(value.getCategories());
  }
  
  @FXML
  private void addCategoryAction() {
    Category newCategory = new Category();
    if(showCategoryDialog(newCategory)) {
      value.getCategories().add(newCategory);
      valueCategories.getItems().setAll(value.getCategories());
    }
  }

  @FXML
  private void editCategoryAction() {
    if(selectedCategory != null) {
      if(showCategoryDialog(selectedCategory)) {
      valueCategories.getItems().setAll(value.getCategories());
      }
    }
  }

  @FXML
  private void deleteCategoryAction() {
    if(selectedCategory != null) {
      value.getCategories().remove(selectedCategory);
      valueCategories.getItems().setAll(value.getCategories());
    }
  }

  @FXML
  private void saveValueAction() {
    if (isInputValid()) {
      value.setName(valueName.getText());
      value.setIndex(Integer.valueOf(valueIndex.getText()));
      value.setUnit(valueUnit.getText());
      value.setDecimalmask(valueMask.getText());
      
      value.setRangeEnabled(valueRangeEnabled.isSelected());
      if(valueRangeEnabled.isSelected()) {
        value.setMin(Integer.valueOf(valueMin.getText()));
        value.setMax(Integer.valueOf(valueMax.getText()));
      }

      save = true;
      
      dialogStage.close();
    }
  }

  @FXML
  private void cancelValueAction() {
      dialogStage.close();
  }
    
  private boolean showCategoryDialog(Category category) {
  try {
    FXMLLoader loader = new FXMLLoader(FXMLCategoryController.class.getResource("FXMLCategory.fxml"));
    AnchorPane page = (AnchorPane) loader.load();
    Stage dialogStage = new Stage();
    dialogStage.setTitle("Category");
    dialogStage.initModality(Modality.WINDOW_MODAL);
    dialogStage.initOwner(this.dialogStage);
    Scene scene = new Scene(page);
    dialogStage.setScene(scene);
    
    dialogStage.setResizable(false);

    FXMLCategoryController controller = loader.getController();
    controller.setDialogStage(dialogStage);
    controller.setCategory(category);

    dialogStage.showAndWait();
    
    return controller.isSaveClicked();
  } catch (IOException e) {
    e.printStackTrace();
    return false;
  }
}
  
  public boolean isSaveClicked() {
    return save;
  }

  private boolean isInputValid() {
    String errorMessage = "";

    if (valueName.getText() == null || valueName.getText().length() == 0) {
      errorMessage += "No valid name! \n"; 
    }
    if (valueIndex.getText() == null || valueIndex.getText().length() == 0) {
      errorMessage += "No valid index! \n"; 
    }
    try {
      int value = Integer.parseInt(valueIndex.getText());
      if(value < 0) {
        errorMessage += "Index must be zero or higher! \n";              
      }
    } catch(Exception ex) {
      errorMessage += "Index is not a number! \n";       
    }
    if (valueUnit.getText() == null || valueUnit.getText().length() == 0) {
      errorMessage += "No valid unit! \n"; 
    }  
    if (valueMask.getText() != null && valueMask.getText().length() > 0) {
      String mask = valueMask.getText();
      if (!mask.matches("[10]+")) {
        errorMessage += "Decimal mask can only contrain 0's or 1's! \n"; 
      }
    }
    
    if(valueRangeEnabled.isSelected()) {
      try {
        int min = Integer.parseInt(valueMin.getText());
        int max = Integer.parseInt(valueMax.getText());
        if(max < min) {
          errorMessage += "Max must be greater than min in range! \n";   
        }
      } catch(Exception ex) {
          errorMessage += "Range is not a number! \n";       
      }      
    }

    if (errorMessage.length() == 0) {
        return true;
    } else {
      Alert alert = new Alert(AlertType.WARNING);
      alert.setTitle("Invalid value");
      alert.setHeaderText("Some fields are filled incorrectly");
      alert.setContentText(errorMessage);
      alert.show();
      return false;
    }
  }
}

