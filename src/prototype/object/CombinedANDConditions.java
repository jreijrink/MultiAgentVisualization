package prototype.object;

import java.util.ArrayList;
import java.util.List;

public class CombinedANDConditions {
  private final List<Condition> conditions;
  
  public CombinedANDConditions(Condition... initConditions) {
    this.conditions = new ArrayList();
    for(Condition condition : initConditions) {
      this.conditions.add(condition);
    }
  }
  
  public void addCondition(Condition condition) {
    this.conditions.add(condition);
  }
  
  public List<Condition> getConditions() {
    return this.conditions;
  } 
}