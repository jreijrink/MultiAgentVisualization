package prototype.settings;

import prototype.object.Parameter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import prototype.object.Category;
import prototype.object.Type;
import prototype.object.Value;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public abstract class DataMapping {
  
  public static List<Parameter> loadParameters() {
    JSONParser parser = new JSONParser();
    ObservableList<Parameter> parameters =  FXCollections.observableArrayList();
    
    try
    {
      Object obj = parser.parse(new FileReader(getParametersFile()));

      JSONObject jsonObject = (JSONObject) obj;

      JSONArray jsonParameters = (JSONArray)jsonObject.get("parameters");
      for(Object parameter : jsonParameters) {
        JSONObject jsonParameter = (JSONObject)parameter;
        String name = (String)jsonParameter.get("name");
        Type type = Type.fromString((String)jsonParameter.get("type"));
        long count = (long)jsonParameter.get("count");
        
        JSONArray jsonValues = (JSONArray)jsonParameter.get("values");        
        List<Value> values = new ArrayList();
        
        if(jsonValues != null) {
          for(Object value : jsonValues) {
            JSONObject jsonValue = (JSONObject)value;
            String valueName = (String)jsonValue.get("name");
            long valueIndex = (long)jsonValue.get("index");
            String valueUnit = (String)jsonValue.get("unit");
            String valueMask = (String)jsonValue.get("decimalmask");
            boolean rangeEnabled = (Boolean)jsonValue.get("rangeEnabled");
            long min = (long)jsonValue.get("min");
            long max = (long)jsonValue.get("max");

            JSONArray jsonCategories = (JSONArray)jsonValue.get("categories");
            List<Category> categories = new ArrayList();
            
            if(jsonCategories != null) {
              for(Object category : jsonCategories) {
                JSONObject jsonCategory = (JSONObject)category;
                long categoryValue = (long)jsonCategory.get("value");
                String categoryName = (String)jsonCategory.get("name");
                Category newCategory = new Category((int)categoryValue, categoryName);
                categories.add(newCategory);
              }
            }

            Value newValue = new Value(valueName, (int)valueIndex, valueUnit, valueMask, rangeEnabled, (int)min, (int)max, categories);
            values.add(newValue);
          }
        }
        
        Parameter newParameter = new Parameter(name, type, (int)count, values);
        parameters.add(newParameter);
      }
    } catch (Exception e) {
        e.printStackTrace();
    }

    return parameters;
  }
  
  public static void saveParameters(List<Parameter> parameters) {
    try
    {
      JSONObject rootObject = new JSONObject();
      JSONArray jsonParameters = new JSONArray();
      
      for(Parameter parameter : parameters) {
        JSONObject jsonParameter = new JSONObject();
        jsonParameter.put("name", parameter.getName());
        jsonParameter.put("type", parameter.getType());
        jsonParameter.put("count", parameter.getCount());
        
        JSONArray jsonValues = new JSONArray();
        for(Value value : parameter.getValuesCopy()) {
          JSONObject jsonValue = new JSONObject();
          jsonValue.put("name", value.getName());
          jsonValue.put("index", value.getIndex());
          jsonValue.put("unit", value.getUnit());
          jsonValue.put("decimalmask", value.getDecimalmask());
          jsonValue.put("rangeEnabled", value.getRangeEnabled());
          jsonValue.put("min", value.getMin());
          jsonValue.put("max", value.getMax());
          
          JSONArray jsonCategories = new JSONArray();
          for(Category category : value.getCategories()) {
            JSONObject jsonCategory = new JSONObject();
            jsonCategory.put("value", category.getValue());
            jsonCategory.put("name", category.getName());
            jsonCategories.add(jsonCategory);
          }
          jsonValue.put("categories", jsonCategories);
          
          jsonValues.add(jsonValue);
        }
        jsonParameter.put("values", jsonValues);
      
        jsonParameters.add(jsonParameter);
      }
      rootObject.put("parameters", jsonParameters);
      
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      JsonParser jp = new JsonParser();
      JsonElement je = jp.parse(rootObject.toJSONString());
      String prettyJsonString = gson.toJson(je);
      
      FileWriter file = new FileWriter(getParametersFile());
      file.write(prettyJsonString);
      file.close();      
    }
    catch(Exception ex) {
      ex.printStackTrace();
    } 
  }

  private static File getParametersFile() throws Exception {
    String fileName = "parameters.json";    
    
    URL url = DataMapping.class.getProtectionDomain().getCodeSource().getLocation();
    String jarPath = new File(url.toURI()).getParentFile() + File.separator + fileName;
    
    File jarFile = new File(jarPath);
    if(jarFile.exists())
      return jarFile;
    
    File localFile = new File(fileName);
    String abs = localFile.getAbsolutePath();
    if(localFile.exists())
      return localFile;
    
    return jarFile;
  }
}