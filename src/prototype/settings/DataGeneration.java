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
    
    Condition opponentWithBallCondition = new Condition("Opponnant has ball", "Opponent with ball", 0, "opponent-with-ball", Equation.IS_NOT, Arrays.asList("None"));
    Condition skillIDInterceptCondition = new Condition("skillID Intercept", "Skill ID", 0, "skill ID", Equation.IS, Arrays.asList("Intercept"));
    Condition skillIDShieldCondition = new Condition("skillID Shield", "Skill ID", 0, "skill ID", Equation.IS, Arrays.asList("Shield"));
    Condition skillIDSMoveCondition = new Condition("skillID Move", "Skill ID", 0, "skill ID", Equation.IS, Arrays.asList("Move"));
    Condition skillIDBallCondition = new Condition("skillID Ball", "Skill ID", 0, "skill ID", Equation.IS, Arrays.asList("Shield", "Kick", "Aim", "Dribbel"));
    
    Condition shotTypePassCondition = new Condition("ShotType Pass", "Skill ID", 0, "shottype", Equation.IS, Arrays.asList("Static pass", "Dynamic pass"));
    Condition shotTypeShotLobCondition = new Condition("ShotType Shot", "Skill ID", 0, "shottype", Equation.IS, Arrays.asList("Flat shot", "Dynamic lob", "Static lob", "Dynamic push", "Penalty"));

    Condition HasBallCondition = new Condition("Has Ball", "CPB", 0, "CPB", Equation.IS, Arrays.asList("True"));
    Condition NotBallCondition = new Condition("Not Ball", "CPB", 0, "CPB", Equation.IS, Arrays.asList("False"));
    Condition CPBTeamCondition = new Condition("CPB Team", "CPB team", 0, "CPB-team", Equation.IS_NOT, Arrays.asList("None"));
    Condition NotBallTeamCondition = new Condition("Team Not Ball", "CPB team", 0, "CPB-team", Equation.IS, Arrays.asList("None"));
    
    Condition refboxStopCondition = new Condition("Refbox Stop", "Refbox command", 0, "all", Equation.IS, Arrays.asList("Stop"));
    Condition refboxNotGoalCondition = new Condition("Refbox Not Goal", "Refbox command", 0, "all", Equation.IS_NOT, Arrays.asList("Stop", "Start", "Goal magenta", "Goal cyan", "Kickoff magenta", "Kickoff cyan", "Subgoal magenta", "Subgoal cyan", "Repair in cyan", "Repair out cyan", "Repair in magenta", "Repair out magenta"));
    Condition refboxGoalCondition = new Condition("Refbox Goal", "Refbox command", 0, "all", Equation.IS, Arrays.asList("Goal magenta", "Goal cyan", "Kickoff magenta", "Kickoff cyan", "Subgoal magenta", "Subgoal cyan"));
    
    //INTERCEPT
    GeneratedParameter interceptParameter = new GeneratedParameter("*Intercept", Type.Categorical, 1, values);
    
    interceptParameter.addANDPreCondition(skillIDInterceptCondition);    
    
    interceptParameter.addANDPostConditionSuccess(HasBallCondition);
    
    interceptParameter.addORPostConditionFailed(opponentWithBallCondition);
    interceptParameter.addORPostConditionFailed(refboxStopCondition);
    
    result.add(interceptParameter);
    
    //SHIELD
    GeneratedParameter shieldParameter = new GeneratedParameter("*Shield", Type.Categorical, 1, values);
    
    shieldParameter.addANDPreCondition(HasBallCondition);
    shieldParameter.addANDPreCondition(skillIDShieldCondition);
    
    shieldParameter.addANDPostConditionSuccess(HasBallCondition);
    
    shieldParameter.addORPostConditionFailed(opponentWithBallCondition);
    shieldParameter.addORPostConditionFailed(refboxStopCondition);
    
    result.add(shieldParameter);
    
    //PASS
    GeneratedParameter passParameter = new GeneratedParameter("*Pass", Type.Categorical, 1, values);
    
    passParameter.addANDPreCondition(HasBallCondition);
    passParameter.addANDPreCondition(shotTypePassCondition);
    
    passParameter.addANDPostConditionSuccess(CPBTeamCondition);
    passParameter.addANDPostConditionSuccess(NotBallCondition);
    
    passParameter.addORPostConditionFailed(HasBallCondition);
    passParameter.addORPostConditionFailed(opponentWithBallCondition);
    passParameter.addORPostConditionFailed(refboxStopCondition);
    
    result.add(passParameter);
    
    //Goal
    GeneratedParameter goalParameter = new GeneratedParameter("*Goal", Type.Categorical, 1, values);
    
    goalParameter.addANDPreCondition(HasBallCondition);
    goalParameter.addANDPreCondition(shotTypeShotLobCondition);
    
    goalParameter.addANDPostConditionSuccess(refboxGoalCondition);
    
    goalParameter.addORPostConditionFailed(refboxNotGoalCondition);
    
    result.add(goalParameter);
    
    
    //Ball loss
    GeneratedParameter ballLossParameter = new GeneratedParameter("*Ball loss", Type.Categorical, 1, values);
    
    ballLossParameter.addANDPreCondition(HasBallCondition);
    
    ballLossParameter.addANDPostConditionSuccess(NotBallTeamCondition);
    ballLossParameter.addANDPostConditionSuccess(NotBallCondition);
    ballLossParameter.addANDPostConditionSuccess(opponentWithBallCondition);
    
    ballLossParameter.addORPostConditionFailed(CPBTeamCondition);
    ballLossParameter.addORPostConditionFailed(refboxStopCondition);
        
    result.add(ballLossParameter);
    
    
    //Illegal SkillID
    GeneratedParameter illegalBallParameter = new GeneratedParameter("*Illegal SkillID (no ball)", Type.Categorical, 1, values);
    
    illegalBallParameter.addANDPreCondition(NotBallCondition);
    illegalBallParameter.addANDPreCondition(skillIDBallCondition);
    
    result.add(illegalBallParameter);
    
    
    //Illegal SkillID Ball
    GeneratedParameter illegalSkillParameter = new GeneratedParameter("*Illegal Move (has ball)", Type.Categorical, 1, values);
    
    illegalSkillParameter.addANDPreCondition(HasBallCondition);
    illegalSkillParameter.addANDPreCondition(skillIDSMoveCondition);
        
    result.add(illegalSkillParameter);
    
    return result;
  }
  
  /*
  public static List<Condition> loadConditions() {
    List<Condition> conditions = new ArrayList();
    
    conditions.add(new Condition("Opponnant has ball", "Opponent with ball", 0, "opponent-with-ball", Equation.IS_NOT, Arrays.asList("None")));
    conditions.add(new Condition("skillID Intercept", "Skill ID", 0, "skill ID", Equation.IS, Arrays.asList("Intercept")));
    conditions.add(new Condition("skillID Shield", "Skill ID", 0, "skill ID", Equation.IS, Arrays.asList("Shield")));
    conditions.add(new Condition("skillID Move", "Skill ID", 0, "skill ID", Equation.IS, Arrays.asList("Move")));
    conditions.add(new Condition("skillID Ball", "Skill ID", 0, "skill ID", Equation.IS, Arrays.asList("Shield", "Kick", "Aim", "Dribbel")));
    
    conditions.add(new Condition("ShotType Pass", "Skill ID", 0, "shottype", Equation.IS, Arrays.asList("Static pass", "Dynamic pass")));
    conditions.add(new Condition("ShotType Shot", "Skill ID", 0, "shottype", Equation.IS, Arrays.asList("Flat shot", "Dynamic lob", "Static lob", "Dynamic push", "Penalty")));

    conditions.add(new Condition("Has Ball", "CPB", 0, "CPB", Equation.IS, Arrays.asList("True")));
    conditions.add(new Condition("Not Ball", "CPB", 0, "CPB", Equation.IS, Arrays.asList("False")));
    conditions.add(new Condition("CPB Team", "CPB team", 0, "CPB-team", Equation.IS_NOT, Arrays.asList("None")));
    conditions.add(new Condition("CPB Not Team", "CPB team", 0, "CPB-team", Equation.IS, Arrays.asList("None")));
    
    conditions.add(new Condition("Refbox Stop", "Refbox command", 0, "all", Equation.IS, Arrays.asList("Stop")));
    conditions.add(new Condition("Refbox Not Goal", "Refbox command", 0, "all", Equation.IS_NOT, Arrays.asList("Stop", "Start", "Goal magenta", "Goal cyan", "Kickoff magenta", "Kickoff cyan", "Subgoal magenta", "Subgoal cyan", "Repair in cyan", "Repair out cyan", "Repair in magenta", "Repair out magenta")));
    conditions.add(new Condition("Refbox Goal", "Refbox command", 0, "all", Equation.IS, Arrays.asList("Goal magenta", "Goal cyan", "Kickoff magenta", "Kickoff cyan", "Subgoal magenta", "Subgoal cyan")));
    
    return conditions;
  }
  */
  
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
          newCondition = new Condition(name, parameterName, (int)parameterIndex, valueName, equationType, range);
        } else {
          newCondition = new Condition(name, parameterName, (int)parameterIndex, valueName, equationType, values);
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