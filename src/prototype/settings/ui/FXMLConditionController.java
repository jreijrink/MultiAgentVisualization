package prototype.settings.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.CheckComboBox;
import prototype.object.Category;
import prototype.object.Condition;
import prototype.object.Equation;
import prototype.object.Parameter;
import prototype.object.ParameterMap;
import prototype.object.Range;
import prototype.object.Type;
import prototype.object.Value;

public class FXMLConditionController implements Initializable {

  @FXML private TextField conditionName;
  @FXML private ComboBox parameterName;
  @FXML private ComboBox parameterIndex;
  @FXML private ComboBox valueName;
  
  @FXML private ComboBox equationType;
  @FXML private TextField equationMin;
  @FXML private TextField equationMax; 
  
  @FXML private CheckComboBox equationValues;
  
  private BooleanProperty rangeEnabled = new SimpleBooleanProperty(false);  
  public BooleanProperty rangeEnabledProperty() { return this.rangeEnabled; }
  public void setRangeEnabled(boolean enabled) { this.rangeEnabled.set(enabled); }
  public boolean getRangeEnabled() { return this.rangeEnabled.getValue(); }
  
  private BooleanProperty valuesEnabled = new SimpleBooleanProperty(false);  
  public BooleanProperty valuesEnabledProperty() { return this.valuesEnabled; }
  public void setValuesEnabled(boolean enabled) { this.valuesEnabled.set(enabled); }
  public boolean getValuesEnabled() { return this.valuesEnabled.getValue(); }
  
  private Stage dialogStage;
  private ParameterMap parameterMap;
  private List<Condition> conditions;
  private Condition condition;
  private boolean save = false;
  Category selectedCategory;
  
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    this.parameterMap = new ParameterMap();
  }
  
  public void setDialogStage(Stage dialogStage) {
    this.dialogStage = dialogStage;
  }

  public void setCondition(List<Condition> conditions, Condition condition) {
    this.conditions = conditions;
    this.condition = condition;
        
    conditionName.setText(condition.getName());
    
    List<Parameter> parameters = this.parameterMap.getMappedParameters();
    Collections.sort(parameters, (o1, o2) -> o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase()));
    parameterName.setItems((ObservableList)parameters);
    
    loadParameter(condition);
  }
  
  private void loadParameter(Condition initial) {
    Parameter parameter = getParameter(initial);
    
    if(parameter != null) {
      parameterName.getSelectionModel().select(parameter);
    }
    
    parameterName.getSelectionModel().selectedItemProperty().addListener((ObservableValue observable, Object oldValue, Object newValue) -> {
      loadParameterIndex(null);        
    });
    
    loadParameterIndex(initial);
  }
  
  private void loadParameterIndex(Condition initial) {
    Parameter parameter = getParameter(initial);
    
    ObservableList<Integer> indexOptions = FXCollections.observableArrayList();
    
    if(parameter != null) {      
      int count = parameter.getCount();
      for(int index = 0; index < count; index++) {
        indexOptions.add(index);
      }      
      parameterIndex.setItems(indexOptions);
      
      if(initial != null && indexOptions.contains(initial.getParameterIndex())) {
        parameterIndex.getSelectionModel().select(initial.getParameterIndex());
      }
    } else {
      parameterIndex.setItems(indexOptions);
    }
    
    loadValues(initial);
  }
  
  private void loadValues(Condition initial) {
    Parameter parameter = getParameter(initial);
    
    if(parameter != null) {
      List<Value> values = parameter.getValues();
      ObservableList<String> valueOptions = FXCollections.observableArrayList();
      for(Value value : values) {
        valueOptions.add(value.getName());
      }
      valueName.setItems(valueOptions);
      valueName.getSelectionModel().selectedItemProperty().addListener((ObservableValue observable, Object oldValue, Object newValue) -> {
        loadEquation(null);        
      });
      
      if(initial != null && valueOptions.contains(initial.getValueName())) {
        valueName.getSelectionModel().select(initial.getValueName());
      }
    }
    
    loadEquation(initial);
  }
  
  private void loadEquation(Condition initial) {
    if(initial != null) {
      equationType.setItems(FXCollections.observableArrayList(Equation.values()));
      equationType.getSelectionModel().select(initial.getEquationType());
    }

    setRangeEnabled(false);
    setValuesEnabled(false);

    equationMin.setText("");
    equationMax.setText("");    
    equationValues.getItems().setAll(FXCollections.observableArrayList());
    
    Parameter parameter = getParameter(initial);
    
    if(parameter != null && parameter.getType() == Type.Categorical) {      
      Value value = getValue(initial);
      if(value != null) {
        setValuesEnabled(true);
        equationValues.getItems().setAll(FXCollections.observableList(value.getCategories()));
      
        if(initial != null) {
          initial.getValues().stream().forEach((conditionValue) -> {
            for(Category category : (ObservableList<Category>)equationValues.getItems()) {
              if(category.getName().equals(conditionValue)) {
                equationValues.getCheckModel().check(category);
              }
            }
          });
        }
      }
    } else {
      setRangeEnabled(true);
      
      if(condition != null && condition.getRange() != null) {
        Range range = condition.getRange();
        equationMin.setText(range.getMin().toString());
        equationMax.setText(range.getMax().toString());
      }
    }
  }
  
  private Parameter getParameter(Condition inital) {
    if(inital != null)
      return this.parameterMap.getParameter(inital.getParameterName());
    else
      return (Parameter)parameterName.getValue();
  }
  
  private Value getValue(Condition inital) {
    Parameter parameter = getParameter(inital);
    if(parameter != null) {
      if(inital != null) {
        return parameter.getValue(inital.getValueName());
      } else {
        return parameter.getValue((String)valueName.getValue());        
      }
    }
    return null;
  }
  
  @FXML
  private void saveConditionAction() {
    if (isInputValid()) {
      condition.setName(conditionName.getText());
      
      condition.setParameterName(((Parameter)parameterName.getValue()).getName());
      condition.setParameterIndex((int)parameterIndex.getValue());
      condition.setValueName((String)valueName.getValue());
      
      condition.setEquation((Equation)equationType.getValue());
      
      if(getRangeEnabled()) {
        int min = Integer.parseInt(equationMin.getText());
        int max = Integer.parseInt(equationMax.getText());
        condition.setRange(new Range(min, max));
      } else {
        condition.setRange(null);
      }
      
      if(getValuesEnabled()) {
        List<Category> selection = equationValues.getCheckModel().getCheckedItems();
        List<String> values = new ArrayList();
        for(Category category : selection) {
          values.add(category.getName());
        }
        condition.setValue(values);
      } else {
        condition.setValue(null);
      }
      
      save = true;
      
      dialogStage.close();
    }
  }

  @FXML
  private void cancelConditionAction() {
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

    if (conditionName.getText() == null || conditionName.getText().length() == 0) {
      errorMessage += "No valid name! \n"; 
    }
    
    for(Condition existing : this.conditions) {
      if(existing != this.condition && existing.getName().equals(conditionName.getText())) {
        errorMessage += "Name must be unique! \n"; 
      }
    }
    
    if (parameterIndex.getValue() == null) {
      errorMessage += "No valid index! \n"; 
    }
    if (valueName.getValue() == null) {
      errorMessage += "No valid value! \n"; 
    }
    if (equationType.getValue() == null) {
      errorMessage += "No valid equation type! \n"; 
    }
    
    if(getRangeEnabled()) {
      try {
        int min = Integer.parseInt(equationMin.getText());
        int max = Integer.parseInt(equationMax.getText());
        if(max < min) {
          errorMessage += "Max must be greater or equal than min in range! \n";   
        }
      } catch(Exception ex) {
          errorMessage += "Range is not a number! \n";       
      }
    }
    
    if(getValuesEnabled()) {
      if (equationValues.getCheckModel().getCheckedItems() == null) {
        errorMessage += "No valid value selecion !\n"; 
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