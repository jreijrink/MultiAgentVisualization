package prototype.object;

import java.util.ArrayList;
import java.util.List;
import static prototype.settings.DataGeneration.loadConditions;

public class CombinedANDConditions {
  private List<Integer> conditionIDs;
  
  public CombinedANDConditions(Condition... initConditions) {
    this.conditionIDs = new ArrayList();
    for(Condition condition : initConditions) {
      this.conditionIDs.add(condition.GetID());
    }
  }
  
  public void setConditions(List<Condition> conditions) {
    this.conditionIDs = new ArrayList();
    for(Condition condition : conditions) {
      this.conditionIDs.add(condition.GetID());
    }
  }
  
  public void addCondition(Condition condition) {
    this.conditionIDs.add(condition.GetID());
  }
  
  public void addCondition(int conditionId) {
    this.conditionIDs.add(conditionId);
  }
  
  public List<Condition> getConditions() {
    List<Condition> allConditions = loadConditions();
    List<Condition> conditions = new ArrayList();
    
    for(Condition condition : allConditions) {
      for(int conditionID : conditionIDs) {
        if(conditionID == condition.GetID()) {
          conditions.add(condition);
        }
      }
    }
    
    return conditions;
  }
  
  public CombinedANDConditions copy() {
    List<Condition> conditions = getConditions();
    return new CombinedANDConditions(conditions.toArray(new Condition[conditions.size()]));
  }
  
  @Override
  public String toString() {
    List<Condition> conditions = loadConditions();
    
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < conditionIDs.size(); i++){
      
      if(i >= 3) {
        sb.append(" ...");
        break;
      }
      
      int conditionID = conditionIDs.get(i);
      for(Condition condition : conditions) {
        if(conditionID == condition.GetID()) {
          sb.append(condition.GetName());

          if(i < conditionIDs.size() - 1)
            sb.append(" AND ");
        }
      }
    }
    return sb.toString();        
  }
}