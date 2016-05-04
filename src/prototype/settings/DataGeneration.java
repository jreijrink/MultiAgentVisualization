package prototype.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import prototype.object.CombinedANDConditions;
import prototype.object.Condition;
import prototype.object.Equation;
import prototype.object.GeneratedParameter;
import prototype.object.Range;

public abstract class DataGeneration {
  
  public static List<GeneratedParameter> loadGenerated() {
    JSONParser parser = new JSONParser();
    List<GeneratedParameter> generated = new ArrayList();
    
    try
    {
      Object obj = parser.parse(new FileReader(getGeneratedFile()));

      JSONObject jsonObject = (JSONObject) obj;

      JSONArray jsonGenerated = (JSONArray)jsonObject.get("generated");
      for(Object parameter : jsonGenerated) {
        JSONObject jsonParameter = (JSONObject)parameter;
        String name = (String)jsonParameter.get("name");
        
        GeneratedParameter newParameter = new GeneratedParameter(name);
        
        List<CombinedANDConditions> preConditions = getConditionFromJSON((JSONArray)jsonParameter.get("pre-conditions"));
        newParameter.setPreConditions(preConditions);
        
        List<CombinedANDConditions> postConditionsSuccess = getConditionFromJSON((JSONArray)jsonParameter.get("post-conditions-succes"));
        newParameter.setPostConditionsSuccess(postConditionsSuccess);
        
        List<CombinedANDConditions> postConditionsFailed = getConditionFromJSON((JSONArray)jsonParameter.get("post-conditions-failed"));                
        newParameter.setPostConditionsFailed(postConditionsFailed);
        
        generated.add(newParameter);
      }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return generated;
  }
  
  public static void saveGenerated(List<GeneratedParameter> generated) {
    try
    {
      JSONObject rootObject = new JSONObject();
      JSONArray jsonGenerated = new JSONArray();
      
      for(GeneratedParameter parameter : generated) {
        JSONObject jsonParameter = new JSONObject();
        jsonParameter.put("name", parameter.getName());
                
        JSONArray preConditions = createJSONConditions(parameter.getPreConditions());
        if(preConditions != null)
          jsonParameter.put("pre-conditions", preConditions);
        
        JSONArray postConditionsSuccess = createJSONConditions(parameter.getPostConditionsSuccess());
        if(postConditionsSuccess != null)
          jsonParameter.put("post-conditions-succes", postConditionsSuccess);
        
        JSONArray postConditionsFailed = createJSONConditions(parameter.getPostConditionsFailed());
        if(postConditionsFailed != null)
          jsonParameter.put("post-conditions-failed", postConditionsFailed);
      
        jsonGenerated.add(jsonParameter);
      }
      
      rootObject.put("generated", jsonGenerated);
      
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      JsonParser jp = new JsonParser();
      JsonElement je = jp.parse(rootObject.toJSONString());
      String prettyJsonString = gson.toJson(je);
      
      FileWriter file = new FileWriter(getGeneratedFile());
      file.write(prettyJsonString);
      file.close();      
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }    
  }
  
  public static List<Condition> loadConditions() {
    JSONParser parser = new JSONParser();
    ObservableList<Condition> conditions =  FXCollections.observableArrayList();
    
    try
    {
      Object obj = parser.parse(new FileReader(getConditionsFile()));

      JSONObject jsonObject = (JSONObject) obj;

      JSONArray jsonConditions = (JSONArray)jsonObject.get("conditions");
      for(Object parameter : jsonConditions) {
        JSONObject jsonParameter = (JSONObject)parameter;
        int id = (int)((long)jsonParameter.get("id"));
        String name = (String)jsonParameter.get("name");
        
        String parameterName = (String)jsonParameter.get("parameterName");
        long parameterIndex = (long)jsonParameter.get("parameterIndex");
        String valueName = (String)jsonParameter.get("valueName");
        
        Equation equationType = Equation.fromString((String) jsonParameter.get("equationType"));
        
        Range range = null;
        if(jsonParameter.containsKey("rangeMin") && jsonParameter.containsKey("rangeMax")) {
          long rangeMin = (long)jsonParameter.get("rangeMin");
          long rangeMax = (long)jsonParameter.get("rangeMax");
          range = new Range(rangeMin, rangeMax);
        }
              
        List<String> values = new ArrayList();
        if(jsonParameter.containsKey("values")) {
          JSONArray jsonValues = (JSONArray)jsonParameter.get("values");
          if(jsonValues != null) {
            for(Object value : jsonValues) {
              JSONObject jsonValue = (JSONObject)value;
              String stringValue = (String)jsonValue.get("value");
              values.add(stringValue);
            }
          }
        }
        
        Condition newCondition = null;
        if(range != null) {
          newCondition = new Condition(id, name, parameterName, (int)parameterIndex, valueName, equationType, range);
        } else {
          newCondition = new Condition(id, name, parameterName, (int)parameterIndex, valueName, equationType, values);
        }
        conditions.add(newCondition);
      }
    } catch (Exception e) {
        e.printStackTrace();
    }

    return conditions;
  }
  
  public static void saveConditions(List<Condition> conditions) {
    try
    {
      JSONObject rootObject = new JSONObject();
      JSONArray jsonConditions = new JSONArray();
      
      for(Condition condition : conditions) {
        JSONObject jsonCondition = new JSONObject();
        jsonCondition.put("id", condition.GetID());
        jsonCondition.put("name", condition.GetName());
        
        jsonCondition.put("parameterName", condition.GetParameterName());
        jsonCondition.put("parameterIndex", condition.GetParameterIndex());
        jsonCondition.put("valueName", condition.GetValueName());
        
        jsonCondition.put("equationType", condition.GetEquationType().toString());
        
        Range range = condition.GetRange();
        if(range != null) {
          jsonCondition.put("rangeMin", range.GetMin());
          jsonCondition.put("rangeMax", range.GetMax());          
        }
        
        List<String> values = condition.GetValues();
        if(values != null && values.size() > 0) {
          JSONArray jsonValues = new JSONArray();
          for(String value : values) {
            JSONObject jsonValue = new JSONObject();
            jsonValue.put("value", value);
            jsonValues.add(jsonValue);
          }
          jsonCondition.put("values", jsonValues);
        }
      
        jsonConditions.add(jsonCondition);
      }
      rootObject.put("conditions", jsonConditions);
      
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      JsonParser jp = new JsonParser();
      JsonElement je = jp.parse(rootObject.toJSONString());
      String prettyJsonString = gson.toJson(je);
      
      FileWriter file = new FileWriter(getConditionsFile());
      file.write(prettyJsonString);
      file.close();      
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
    
  private static JSONArray createJSONConditions(List<CombinedANDConditions> conditions) {
    JSONArray jsonCombinedAndConditions = new JSONArray();
    
    if(conditions != null) {
      for(CombinedANDConditions preCondition : conditions) {
        JSONObject jsonCombinedAndCondition = new JSONObject();
        JSONArray jsonConditions = new JSONArray();

        for(Condition condition : preCondition.getConditions()) {
          JSONObject jsonCondition = new JSONObject();
          jsonCondition.put("id", condition.GetID());
          jsonConditions.add(jsonCondition);
        }

        jsonCombinedAndCondition.put("and-conditions", jsonConditions);
        jsonCombinedAndConditions.add(jsonCombinedAndCondition);
      }
    }
    
    return jsonCombinedAndConditions;
  }
  
  private static List<CombinedANDConditions> getConditionFromJSON(JSONArray jsonConditionsArray) {
    List<CombinedANDConditions> conditions = new ArrayList();
    
    for(Object combinedAndCondition : jsonConditionsArray) {
      CombinedANDConditions combinedANDConditions = new CombinedANDConditions();
      
      JSONObject jsonCombinedAndCondition = (JSONObject)combinedAndCondition;
      JSONArray jsonConditions = (JSONArray)jsonCombinedAndCondition.get("and-conditions");
      
      for(Object condition : jsonConditions) {
        JSONObject jsonCondition = (JSONObject)condition;
        int id = (int)(long)jsonCondition.get("id");
        combinedANDConditions.addCondition(id);
      }
      
      conditions.add(combinedANDConditions);
    }
    
    return conditions;
  }
  
  private static File getConditionsFile() throws Exception {
    String fileName = "conditions.json";
    return getFile(fileName);
  }
    
  private static File getGeneratedFile() throws Exception {
    String fileName = "generated.json";
    return getFile(fileName);
  }

  private static File getFile(String fileName) throws Exception {    
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