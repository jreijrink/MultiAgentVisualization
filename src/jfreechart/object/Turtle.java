package jfreechart.object;

import static jfreechart.Parser.*;

public class Turtle {
  public final int turtleID;
  private final double[] values;
  public final int timeFrame;
    
  public Turtle(int turtleID, int timeFrame, double[] values) {
    this.turtleID = turtleID;
    this.timeFrame = timeFrame;
    this.values = values;
  }
  
  public ParameterMap getParameters() {
    ParameterMap parameters = createParameterMap();
    int paramSize = parameters.getSize();
    
    int startIndex = turtleID * paramSize;
    int endIndex = (turtleID + 1) * (paramSize - 1);
    
    for(int i = startIndex; i < endIndex; i++) {
      double value = values[i];
      parameters.setValue(i - startIndex, value);
    }
    
    return parameters;
  }
  
  public int getID() {
    return turtleID;
  }
  
  private ParameterMap createParameterMap() {
    ParameterMap parameters = new ParameterMap(timeFrame);
    
    parameters.addParameter("pose", 3, 1);    
    parameters.addParameter("ball", 3, 1);    
    parameters.addParameter("ballWM", 3, 1);    
    parameters.addParameter("obstacles", 3, MAXNOBJ_LOCAL);    
    parameters.addParameter("opponent", 3, MAX_OPPONENTS);    
    parameters.addParameter("turtlewm", 3, MAX_TURTLES);    
    parameters.addParameter("target", 3, 1);    
    parameters.addParameter("subtarget", 2, 1);    
    parameters.addParameter("skillid", 1, 1);    
    parameters.addParameter("CPB", 1, 1);    
    parameters.addParameter("lineposrecog", 2, 1);    
    parameters.addParameter("teamcolor", 1, 1);    
    parameters.addParameter("blueishomegoal", 1, 1);    
    parameters.addParameter("batteryVoltage", 1, 1);    
    parameters.addParameter("emergencyStatus", 1, 1);    
    parameters.addParameter("roleId", 2, 1);
    parameters.addParameter("refboxRole", 1, 1);    
    parameters.addParameter("defaultRole", 1, 1);
    parameters.addParameter("CPBteam", 1, 1);    
    parameters.addParameter("robotInField", 1, 1);    
    parameters.addParameter("ballFound", 1, 1);    
    parameters.addParameter("cpu0Load", 1, 1);    
    parameters.addParameter("cpu1Load", 1, 1);    
    parameters.addParameter("cameraStatus", 1, 1);   
    parameters.addParameter("restartCountMotion", 1, 1);    
    parameters.addParameter("restartCountVision", 1, 1);    
    parameters.addParameter("restartCountWorldmodel", 1, 1);    
    parameters.addParameter("ball_confidence", 1, 1);    
    parameters.addParameter("motion_status", 1, 1);    
    parameters.addParameter("vision_status", 1, 1);    
    parameters.addParameter("worldmodel_status", 1, 1);    
    parameters.addParameter("active", 1, 1);    
    parameters.addParameter("waypoints", 2, 8);
    parameters.addParameter("pathlength", 1, 1);    
    parameters.addParameter("motorTemperature", 1, 3);    
    parameters.addParameter("ball_xyz_vxvyvz_est", 1, 6);    
    parameters.addParameter("subsubtarget", 1, 2);    
    parameters.addParameter("opponentVelocities", 2, MAX_OPPONENTS);
    parameters.addParameter("refboxcommand", 1, 1);
    parameters.addParameter("usedBallTurtleID", 1, 1);    
    parameters.addParameter("mergedBallSource", 1, 1);    
    parameters.addParameter("opponentlabelnumber", 1, MAX_OPPONENTS);    
    parameters.addParameter("opponentwithball", 1, 1);    
    parameters.addParameter("packetLossTurtles", 1, MAX_ACTIVE_TURTLES);    
    parameters.addParameter("packetLossCoach", 1, 1);    
    parameters.addParameter("cpb_poi_xyo", 1, 3);    
    parameters.addParameter("muFieldNr", 1, 1);
    
    return parameters;
  }
  
  @Override
  public String toString() {
    return String.format("Turtle: %d", turtleID);
  }
}
