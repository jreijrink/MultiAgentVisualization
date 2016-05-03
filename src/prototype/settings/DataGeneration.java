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
import java.util.Arrays;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import prototype.object.Category;
import prototype.object.CombinedANDConditions;
import prototype.object.Type;
import prototype.object.Value;
import prototype.object.Condition;
import prototype.object.Equation;
import prototype.object.GeneratedParameter;
import prototype.object.Range;

public abstract class DataGeneration {
  
  public static List<GeneratedParameter> loadGenerated() {
    List<GeneratedParameter> result = new ArrayList();
    
    List<Category> categories = new ArrayList();
    categories.add(new Category(1, "Success"));
    categories.add(new Category(2, "Failed"));
    
    List<Value> values = new ArrayList();
    values.add(new Value("result", 0, "", "", false, 0, 0, categories));
    
    Condition opponentWithBallCondition = new Condition(1, "Opponnant has ball", "Opponent with ball", 0, "opponent-with-ball", Equation.IS_NOT, Arrays.asList("None"));
    Condition skillIDInterceptCondition = new Condition(2, "skillID Intercept", "Skill ID", 0, "skill ID", Equation.IS, Arrays.asList("Intercept"));
    Condition skillIDShieldCondition = new Condition(3, "skillID Shield", "Skill ID", 0, "skill ID", Equation.IS, Arrays.asList("Shield"));
    Condition skillIDSMoveCondition = new Condition(4, "skillID Move", "Skill ID", 0, "skill ID", Equation.IS, Arrays.asList("Move"));
    Condition skillIDBallCondition = new Condition(5, "skillID Ball", "Skill ID", 0, "skill ID", Equation.IS, Arrays.asList("Shield", "Kick", "Aim", "Dribbel"));
    
    Condition shotTypePassCondition = new Condition(6, "ShotType Pass", "Skill ID", 0, "shottype", Equation.IS, Arrays.asList("Static pass", "Dynamic pass"));
    Condition shotTypeShotLobCondition = new Condition(7, "ShotType Shot", "Skill ID", 0, "shottype", Equation.IS, Arrays.asList("Flat shot", "Dynamic lob", "Static lob", "Dynamic push", "Penalty"));

    Condition HasBallCondition = new Condition(8, "Has Ball", "CPB", 0, "CPB", Equation.IS, Arrays.asList("True"));
    Condition NotBallCondition = new Condition(9, "Not Ball", "CPB", 0, "CPB", Equation.IS, Arrays.asList("False"));
    Condition CPBTeamCondition = new Condition(10, "CPB Team", "CPB team", 0, "CPB-team", Equation.IS_NOT, Arrays.asList("None"));
    Condition NotBallTeamCondition = new Condition(11, "Team Not Ball", "CPB team", 0, "CPB-team", Equation.IS, Arrays.asList("None"));
    
    Condition refboxStopCondition = new Condition(12, "Refbox Stop", "Refbox command", 0, "all", Equation.IS, Arrays.asList("Stop"));
    Condition refboxNotGoalCondition = new Condition(13, "Refbox Not Goal", "Refbox command", 0, "all", Equation.IS_NOT, Arrays.asList("Stop", "Start", "Goal magenta", "Goal cyan", "Kickoff magenta", "Kickoff cyan", "Subgoal magenta", "Subgoal cyan", "Repair in cyan", "Repair out cyan", "Repair in magenta", "Repair out magenta"));
    Condition refboxGoalCondition = new Condition(14, "Refbox Goal", "Refbox command", 0, "all", Equation.IS, Arrays.asList("Goal magenta", "Goal cyan", "Kickoff magenta", "Kickoff cyan", "Subgoal magenta", "Subgoal cyan"));
    
    //Goal
    GeneratedParameter goalParameter = new GeneratedParameter("*Goal", Type.Categorical, 1, values);    
    goalParameter.addPreCondition(new CombinedANDConditions(HasBallCondition, shotTypeShotLobCondition));
    goalParameter.addPostConditionSuccess(new CombinedANDConditions(refboxGoalCondition));
    goalParameter.addPostConditionFailed(new CombinedANDConditions(refboxNotGoalCondition));    
    result.add(goalParameter);
    
    //INTERCEPT
    GeneratedParameter interceptParameter = new GeneratedParameter("*Intercept", Type.Categorical, 1, values);
    interceptParameter.addPreCondition(new CombinedANDConditions(skillIDInterceptCondition));    
    interceptParameter.addPostConditionSuccess(new CombinedANDConditions(HasBallCondition));    
    interceptParameter.addPostConditionFailed(new CombinedANDConditions(opponentWithBallCondition));
    interceptParameter.addPostConditionFailed(new CombinedANDConditions(refboxStopCondition));
    result.add(interceptParameter);
    
    //SHIELD
    GeneratedParameter shieldParameter = new GeneratedParameter("*Shield", Type.Categorical, 1, values);
    shieldParameter.addPreCondition(new CombinedANDConditions(HasBallCondition, skillIDShieldCondition));
    shieldParameter.addPostConditionSuccess(new CombinedANDConditions(HasBallCondition));
    shieldParameter.addPostConditionFailed(new CombinedANDConditions(opponentWithBallCondition));
    shieldParameter.addPostConditionFailed(new CombinedANDConditions(refboxStopCondition));    
    result.add(shieldParameter);
    
    //PASS
    GeneratedParameter passParameter = new GeneratedParameter("*Pass", Type.Categorical, 1, values);
    passParameter.addPreCondition(new CombinedANDConditions(HasBallCondition, shotTypePassCondition));
    passParameter.addPostConditionSuccess(new CombinedANDConditions(CPBTeamCondition, NotBallCondition));
    passParameter.addPostConditionFailed(new CombinedANDConditions(HasBallCondition));
    passParameter.addPostConditionFailed(new CombinedANDConditions(opponentWithBallCondition));
    passParameter.addPostConditionFailed(new CombinedANDConditions(refboxStopCondition));    
    result.add(passParameter);
    
        
    //Ball loss
    GeneratedParameter ballLossParameter = new GeneratedParameter("*Ball loss", Type.Categorical, 1, values);    
    ballLossParameter.addPreCondition(new CombinedANDConditions(HasBallCondition));    
    ballLossParameter.addPostConditionSuccess(new CombinedANDConditions(NotBallTeamCondition, NotBallCondition, opponentWithBallCondition));
    ballLossParameter.addPostConditionFailed(new CombinedANDConditions(CPBTeamCondition));
    ballLossParameter.addPostConditionFailed(new CombinedANDConditions(refboxStopCondition)); 
    result.add(ballLossParameter);
    
    //Illegal SkillID
    GeneratedParameter illegalBallParameter = new GeneratedParameter("*Illegal SkillID (no ball)", Type.Categorical, 1, values);    
    illegalBallParameter.addPreCondition(new CombinedANDConditions(NotBallCondition, skillIDBallCondition));    
    result.add(illegalBallParameter);
            
    //Illegal SkillID Ball
    GeneratedParameter illegalSkillParameter = new GeneratedParameter("*Illegal Move (has ball)", Type.Categorical, 1, values);    
    illegalSkillParameter.addPreCondition(new CombinedANDConditions(HasBallCondition, skillIDSMoveCondition));
    result.add(illegalSkillParameter);
            
    return result;
  }
  
  public static void saveGenerated(List<GeneratedParameter> generated) {
    
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
  
  private static File getConditionsFile() throws Exception {
    String fileName = "conditions.json";    
    
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