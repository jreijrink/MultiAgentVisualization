package prototype.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    
    Condition opponentWithBallCondition = new Condition("Opponent with ball", 0, "opponent-with-ball", Equation.IS_NOT, Arrays.asList("None"));
    Condition skillIDInterceptCondition = new Condition("Skill ID", 0, "skill ID", Equation.IS, Arrays.asList("Intercept"));
    Condition skillIDShieldCondition = new Condition("Skill ID", 0, "skill ID", Equation.IS, Arrays.asList("Shield"));
    Condition skillIDSMoveCondition = new Condition("Skill ID", 0, "skill ID", Equation.IS, Arrays.asList("Move"));
    Condition skillIDBallCondition = new Condition("Skill ID", 0, "skill ID", Equation.IS, Arrays.asList("Shield", "Kick", "Aim", "Dribbel"));
    
    Condition shotTypePassCondition = new Condition("Skill ID", 0, "shottype", Equation.IS, Arrays.asList("Static pass", "Dynamic pass"));
    Condition shotTypeShotLobCondition = new Condition("Skill ID", 0, "shottype", Equation.IS, Arrays.asList("Flat shot", "Dynamic lob", "Static lob", "Dynamic push", "Penalty"));

    Condition HasBallCondition = new Condition("CPB", 0, "CPB", Equation.IS, Arrays.asList("True"));
    Condition NotBallCondition = new Condition("CPB", 0, "CPB", Equation.IS, Arrays.asList("False"));
    Condition CPBTeamCondition = new Condition("CPB team", 0, "CPB-team", Equation.IS_NOT, Arrays.asList("None"));
    Condition NotBallTeamCondition = new Condition("CPB team", 0, "CPB-team", Equation.IS, Arrays.asList("None"));
    
    Condition refboxStopCondition = new Condition("Refbox command", 0, "all", Equation.IS, Arrays.asList("Stop"));
    Condition refboxNotGoalCondition = new Condition("Refbox command", 0, "all", Equation.IS_NOT, Arrays.asList("Stop", "Start", "Goal magenta", "Goal cyan", "Kickoff magenta", "Kickoff cyan", "Subgoal magenta", "Subgoal cyan", "Repair in cyan", "Repair out cyan", "Repair in magenta", "Repair out magenta"));
    Condition refboxGoalCondition = new Condition("Refbox command", 0, "all", Equation.IS, Arrays.asList("Goal magenta", "Goal cyan", "Kickoff magenta", "Kickoff cyan", "Subgoal magenta", "Subgoal cyan"));
    
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
}