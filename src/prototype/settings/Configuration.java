package prototype.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import prototype.object.ParameterMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Configuration {

  public int MaxTurtles = 7;
  public int MaxOpponents = 10;
  
  public double PenaltyWidth = 6.37;
  public double PenaltyLength = 2.125;
  public double PenaltySpot = 2.87;
  
  public double FieldWidth = 11.88;
  public double FieldLength = 17.88;
  public double GoalAreaWidth = 3.37;
  public double GoalAreaLength = 0.625;
  public double GoalWidth = 2.00;
  public double GoalDepth = 0.575;
  
  public String Pose = "";
  public String PoseX = "";
  public String PoseY = "";
  public String PoseRot = "";
  
  public String RobotInField = "";
  public String RobotInFieldIndex = "";
  
  public String Opponent = "";
  public String OpponentX = "";
  public String OpponentY = "";
  
  public String Opponentlabelnumber = "";
  public String OpponentlabelnumberIndex = "";
  
  public String Ball = "";
  public String BallX = "";
  public String BallY = "";
  
  public String BallFound = "";
  public String BallFoundIndex = "";
  
  public Configuration() {
    load();
  }
  
  public void Save() {    
    try
    {
      JSONObject rootObject = new JSONObject();
      rootObject.put("MaxTurtles", MaxTurtles);
      rootObject.put("MaxOpponents", MaxOpponents);
      
      rootObject.put("PenaltyWidth", PenaltyWidth);
      rootObject.put("PenaltyLength", PenaltyLength);
      rootObject.put("PenaltySpot", PenaltySpot);
      
      rootObject.put("FieldWidth", FieldWidth);
      rootObject.put("FieldLength", FieldLength);
      rootObject.put("GoalAreaWidth", GoalAreaWidth);
      rootObject.put("GoalAreaLength", GoalAreaLength);
      rootObject.put("GoalWidth", GoalWidth);
      rootObject.put("GoalDepth", GoalDepth);
      
      rootObject.put("Pose", Pose);
      rootObject.put("PoseX", PoseX);
      rootObject.put("PoseY", PoseY);
      rootObject.put("PoseRot", PoseRot);
      
      rootObject.put("RobotInField", RobotInField);
      rootObject.put("RobotInFieldIndex", RobotInFieldIndex);
      
      rootObject.put("Opponent", Opponent);
      rootObject.put("OpponentX", OpponentX);
      rootObject.put("OpponentY", OpponentY);
      
      rootObject.put("Opponentlabelnumber", Opponentlabelnumber);
      rootObject.put("OpponentlabelnumberIndex", OpponentlabelnumberIndex);
      
      rootObject.put("Ball", Ball);
      rootObject.put("BallX", BallX);
      rootObject.put("BallY", BallY);
      
      rootObject.put("BallFound", BallFound);
      rootObject.put("BallFoundIndex", BallFoundIndex);
        
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
  
  public boolean complete() {
    ParameterMap parameterMap = new ParameterMap();
    
    if(Pose == null || Pose.equals("") || !parameterMap.ContainsParameter(Pose)) return false;
    if(PoseX == null || PoseX.equals("") || !parameterMap.GetParameter(Pose).containsValue(PoseX)) return false;
    if(PoseY == null || PoseY.equals("") || !parameterMap.GetParameter(Pose).containsValue(PoseY)) return false;
    if(PoseRot == null || PoseRot.equals("") || !parameterMap.GetParameter(Pose).containsValue(PoseRot)) return false;  
    
    if(RobotInField == null || RobotInField.equals("") || !parameterMap.ContainsParameter(RobotInField)) return false;
    if(RobotInFieldIndex == null || RobotInFieldIndex.equals("") || !parameterMap.GetParameter(RobotInField).containsValue(RobotInFieldIndex)) return false;
    
    if(Opponent == null || Opponent.equals("") || !parameterMap.ContainsParameter(Opponent)) return false;
    if(OpponentX == null || OpponentX.equals("") || !parameterMap.GetParameter(Opponent).containsValue(OpponentX)) return false;
    if(OpponentY == null || OpponentY.equals("") || !parameterMap.GetParameter(Opponent).containsValue(OpponentY)) return false;
    
    if(Opponentlabelnumber == null || Opponentlabelnumber.equals("") || !parameterMap.ContainsParameter(Opponentlabelnumber)) return false;
    if(OpponentlabelnumberIndex == null || OpponentlabelnumberIndex.equals("") || !parameterMap.GetParameter(Opponentlabelnumber).containsValue(OpponentlabelnumberIndex)) return false;  
    
    if(Ball == null || Ball.equals("") || !parameterMap.ContainsParameter(Ball)) return false;
    if(BallX == null || BallX.equals("") || !parameterMap.GetParameter(Ball).containsValue(BallX)) return false;
    if(BallY == null || BallY.equals("") || !parameterMap.GetParameter(Ball).containsValue(BallY)) return false;  
    
    if(BallFound == null || BallFound.equals("") || !parameterMap.ContainsParameter(BallFound)) return false;
    if(BallFoundIndex == null || BallFoundIndex.equals("") || !parameterMap.GetParameter(BallFound).containsValue(BallFoundIndex)) return false;
    
    return true;
  }
  
  private void load() {
    try
    {
      JSONParser parser = new JSONParser();
    
      Object obj = parser.parse(new FileReader(getParametersFile()));

      JSONObject jsonObject = (JSONObject) obj;

      if(jsonObject.containsKey("MaxTurtles"))
        MaxTurtles = (int)(long)jsonObject.get("MaxTurtles");
      if(jsonObject.containsKey("MaxOpponents"))
        MaxOpponents = (int)(long)jsonObject.get("MaxOpponents");
      
      if(jsonObject.containsKey("PenaltyWidth"))
        PenaltyWidth = (double)jsonObject.get("PenaltyWidth");
      if(jsonObject.containsKey("PenaltyLength"))
        PenaltyLength = (double)jsonObject.get("PenaltyLength");
      if(jsonObject.containsKey("PenaltySpot"))
        PenaltySpot = (double)jsonObject.get("PenaltySpot");
      
      if(jsonObject.containsKey("FieldWidth"))
        FieldWidth = (double)jsonObject.get("FieldWidth");
      if(jsonObject.containsKey("FieldLength"))
        FieldLength = (double)jsonObject.get("FieldLength");
      if(jsonObject.containsKey("GoalAreaWidth"))
        GoalAreaWidth = (double)jsonObject.get("GoalAreaWidth");
      if(jsonObject.containsKey("GoalAreaLength"))
        GoalAreaLength = (double)jsonObject.get("GoalAreaLength");
      if(jsonObject.containsKey("GoalWidth"))
        GoalWidth = (double)jsonObject.get("GoalWidth");
      if(jsonObject.containsKey("GoalDepth"))
        GoalDepth = (double)jsonObject.get("GoalDepth");
      
      if(jsonObject.containsKey("Pose"))
        Pose = (String)jsonObject.get("Pose");
      if(jsonObject.containsKey("PoseX"))
        PoseX = (String)jsonObject.get("PoseX");
      if(jsonObject.containsKey("PoseY"))
        PoseY = (String)jsonObject.get("PoseY");
      if(jsonObject.containsKey("PoseRot"))
        PoseRot = (String)jsonObject.get("PoseRot");
      
      if(jsonObject.containsKey("RobotInField"))
        RobotInField = (String)jsonObject.get("RobotInField");
      if(jsonObject.containsKey("RobotInFieldIndex"))
        RobotInFieldIndex = (String)jsonObject.get("RobotInFieldIndex");
      
      if(jsonObject.containsKey("Opponent"))
        Opponent = (String)jsonObject.get("Opponent");
      if(jsonObject.containsKey("OpponentX"))
        OpponentX = (String)jsonObject.get("OpponentX");
      if(jsonObject.containsKey("OpponentY"))
        OpponentY = (String)jsonObject.get("OpponentY");
      
      if(jsonObject.containsKey("Opponentlabelnumber"))
        Opponentlabelnumber = (String)jsonObject.get("Opponentlabelnumber");
      if(jsonObject.containsKey("OpponentlabelnumberIndex"))
        OpponentlabelnumberIndex = (String)jsonObject.get("OpponentlabelnumberIndex");
      
      if(jsonObject.containsKey("Ball"))
        Ball = (String)jsonObject.get("Ball");
      if(jsonObject.containsKey("BallX"))
        BallX = (String)jsonObject.get("BallX");
      if(jsonObject.containsKey("BallY"))
        BallY = (String)jsonObject.get("BallY");
      
      if(jsonObject.containsKey("BallFound"))
        BallFound = (String)jsonObject.get("BallFound");
      if(jsonObject.containsKey("BallFoundIndex"))
        BallFoundIndex = (String)jsonObject.get("BallFoundIndex");
      
    } catch (Exception e) {
        e.printStackTrace();
    }    
  }
  
  private static File getParametersFile() throws Exception {
    String fileName = "configuration.json";    
    
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