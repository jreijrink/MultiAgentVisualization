package jfreechart.settings.ui;

import jfreechart.object.Parameter;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfreechart.settings.DataMapping;
import jfreechart.object.Type;
import jfreechart.object.Value;

public class FXMLParametersController implements Initializable {
  
  @FXML private ListView listParameters;
  @FXML private Button addParameter;
  @FXML private Button editParameter;
  @FXML private Button deleteParameter;
  
  @FXML private Button upParameter;
  @FXML private Button downParameter;
  
  @FXML private TextField parameterName;
  @FXML private ChoiceBox parameterType;
  @FXML private TextField parameterCount;
  @FXML private ListView parameterValues;
  
  @FXML private ListView listValues;
  @FXML private Button addValue;
  @FXML private Button editValue;
  @FXML private Button deleteValue;
  
  @FXML private Button cancelParameter;
  @FXML private Button saveParameter;
  
  private BooleanProperty inEditingMode = new SimpleBooleanProperty(false);  
  public BooleanProperty inEditingModeProperty() { return this.inEditingMode; }
  public void setInEditingMode(boolean editing) { this.inEditingMode.set(editing); }
  public boolean getInEditingMode() { return this.inEditingMode.getValue(); }
  
  private BooleanProperty isParameterSelected = new SimpleBooleanProperty(false);  
  public BooleanProperty isParameterSelectedProperty() { return this.isParameterSelected; }  
  public void setIsParameterSelected(boolean selected) { this.isParameterSelected.set(selected); }  
  public boolean getIsParameterSelected() { return this.isParameterSelected.getValue(); }  
    
  private BooleanProperty isValueSelected = new SimpleBooleanProperty(false);  
  public BooleanProperty isValueSelectedProperty() { return this.isValueSelected; }  
  public void setIsValueSelected(boolean selected) { this.isValueSelected.set(selected); }  
  public boolean getIsValueSelected() { return this.isValueSelected.getValue(); }  
  
  private Stage primaryStage;
  private ObservableList<Parameter> parameters;
  private Parameter selectedParameter;
  private List<Value> selectedValues;
  private Value selectedValue;
    
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    parameters = FXCollections.observableArrayList(DataMapping.loadParameters());    
    parameterType.getItems().setAll(Type.values());
    listParameters.setItems(parameters);    
    listParameters.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        Object item = listParameters.getSelectionModel().getSelectedItem();
        if(item != null) {
          showParameter((Parameter)item);
        } else {
          showParameter(null);
        }
      }
    });
  
    showParameter(null);
  }
  
  public void setStage(Stage primaryStage) {
    this.primaryStage = primaryStage;
  }
  
  @FXML
  private void addParameterAction(ActionEvent event) {
    listParameters.getSelectionModel().select(null);
    setInEditingMode(true);
  }
  
  @FXML
  private void editParameterAction(ActionEvent event) {
    setInEditingMode(true);
  }
  
  @FXML
  private void upParameterAction(ActionEvent event) {
    if(selectedParameter != null) {
      int index = parameters.indexOf(selectedParameter);
      if(index > 0) {
        Parameter current = parameters.get(index);
        Parameter prev = parameters.get(index - 1);
        parameters.remove(current);
        parameters.remove(prev);
        parameters.add(index - 1, current);
        parameters.add(index, prev);
        listParameters.getSelectionModel().select(current);
        DataMapping.saveParameters(parameters);
      }
    }
  }
  
  @FXML
  private void downParameterAction(ActionEvent event) {
    if(selectedParameter != null) {
      int index = parameters.indexOf(selectedParameter);
      if(index < parameters.size() - 1) {
        Parameter current = parameters.get(index);
        Parameter next = parameters.get(index + 1);
        parameters.remove(current);
        parameters.remove(next);
        parameters.add(index, next);
        parameters.add(index + 1, current);
        listParameters.getSelectionModel().select(current);
        DataMapping.saveParameters(parameters);
      }
    }
  }
  
  @FXML
  private void deleteParameterAction(ActionEvent event) {
    if(selectedParameter != null) {
      parameters.remove(selectedParameter);
      DataMapping.saveParameters(parameters);
      listParameters.getSelectionModel().select(null);
    }
  }
  
  @FXML
  private void cancelParameterAction(ActionEvent event) {
    showParameter(selectedParameter);
    setInEditingMode(false);
  }
  
  @FXML
  private void saveParameterAction(ActionEvent event) {
    if(saveParameter()) {
      showParameter(selectedParameter);
      setInEditingMode(false);
    }
  }
  
  @FXML
  private void addValueAction(ActionEvent event) {
    if(selectedValues != null) {
      Value newValue = new Value();
      if(showValueDialog(newValue, (Type)parameterType.getSelectionModel().getSelectedItem())) {
        selectedValues.add(newValue);
        sortValues();
        listValues.getItems().setAll(selectedValues);
      }
    }
  }
  
  @FXML
  private void editValueAction(ActionEvent event) {
    if(selectedValue != null) {
      if(showValueDialog(selectedValue, (Type)parameterType.getSelectionModel().getSelectedItem())) {
        listValues.getItems().setAll(selectedValues);
      }
    }
  }
  
  @FXML
  private void deleteValueAction(ActionEvent event) {
    if(selectedValue != null) {
      selectedValues.remove(selectedValue);
      listValues.getItems().setAll(selectedValues);
    }
  }  
    
  private boolean saveParameter() {
    if(isInputValid()) {
      try {
        String name = parameterName.getText();
        Type type = (Type)parameterType.getSelectionModel().getSelectedItem();
        int count = Integer.parseInt(parameterCount.getText());

        if(selectedParameter == null) {
          selectedParameter = new Parameter();
          parameters.add(selectedParameter);
        }

        selectedParameter.setName(name);
        selectedParameter.setType(type);
        selectedParameter.setCount(count);
        selectedParameter.setValues(selectedValues);

        //Notify the change
        parameters.set(parameters.indexOf(selectedParameter), selectedParameter);

        DataMapping.saveParameters(parameters);

        listParameters.getSelectionModel().select(selectedParameter);
        return true;
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    
    return false;
  }
  
  private void showParameter(Parameter parameter) {
    if(parameter == null) {
      parameter = new Parameter();
      selectedParameter = null;
      setIsParameterSelected(false);
      listValues.getItems().clear();
    } else {
      selectedParameter = parameter;
      setIsParameterSelected(true);
    }

    selectedValues = parameter.getValuesCopy();
    sortValues();
    listValues.getItems().setAll(selectedValues);
    
    listValues.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        Object item = listValues.getSelectionModel().getSelectedItem();
        if(item != null) {
          selectedValue = (Value)item;
          setIsValueSelected(true);
        } else {
          selectedValue = null;
          setIsValueSelected(false);
        }
      }
    });

    parameterName.setText(parameter.getName());
    parameterType.getSelectionModel().select(parameter.getType());
    parameterCount.setText(String.valueOf(parameter.getCount()));
  }
  
  private void sortValues() {
    Collections.sort(selectedValues, new Comparator<Value>() {
      @Override
      public int compare(Value value1, Value value2) {
        return value1.getIndex() - value2.getIndex();
      }
    });
  }
  
  private boolean showValueDialog(Value value, Type parameterType) {
  try {
    FXMLLoader loader = new FXMLLoader(FXMLValueController.class.getResource("FXMLValue.fxml"));
    AnchorPane page = (AnchorPane) loader.load();
    Stage dialogStage = new Stage();
    dialogStage.setTitle("Value");
    dialogStage.initModality(Modality.WINDOW_MODAL);
    dialogStage.initOwner(primaryStage);
    Scene scene = new Scene(page);
    dialogStage.setScene(scene);

    dialogStage.setResizable(false);
    
    FXMLValueController controller = loader.getController();
    controller.setDialogStage(dialogStage);
    controller.setValue(value, parameterType);

    dialogStage.showAndWait();
    
    return controller.isSaveClicked();
  } catch (IOException e) {
    e.printStackTrace();
    return false;
  }
}
  
  private boolean isInputValid() {
    String errorMessage = "";

    if (parameterName.getText() == null || parameterName.getText().length() == 0) {
        errorMessage += "No valid name! \n"; 
    }
    if (parameterCount.getText() == null || parameterCount.getText().length() == 0) {
        errorMessage += "No valid count! \n"; 
    }
    try {
      int count = Integer.parseInt(parameterCount.getText());
      /*
      int highestIndex = -1;
      for(Value value : selectedValues) {
        highestIndex = Math.max(highestIndex, value.getIndex());
      }
      if(count != (highestIndex + 1)) {
        errorMessage += "Count does not match the values! \n";          
      }
      */
    } catch(Exception ex) {
        errorMessage += "Count is not a number! \n";       
    }

    if (errorMessage.length() == 0) {
        return true;
    } else {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setTitle("Invalid parameter");
      alert.setHeaderText("Some fields are filled incorrectly");
      alert.setContentText(errorMessage);
      alert.show();
      return false;
    }
  }
}