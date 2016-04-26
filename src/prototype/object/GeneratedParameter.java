package prototype.object;

import java.util.ArrayList;
import java.util.List;

public class GeneratedParameter extends Parameter {
  private final List<Condition> preConditions;
  private final List<Condition> postConditionsSuccess;
  private final List<Condition> postConditionsFailed;
    
  public GeneratedParameter(String name, Type type, int count, List<Value> values) {
    super.name = name;
    super.type = type;
    super.count = count;
    super.values = values;
    
    this.preConditions = new ArrayList();
    this.postConditionsSuccess = new ArrayList();
    this.postConditionsFailed = new ArrayList();
  }
  
  public void addPreCondition(Condition condition) {
    preConditions.add(condition);
  }
  
  public List<Condition> getPreConditions() {
    return preConditions;
  }
  
  public void addPostConditionSuccess(Condition condition) {
    postConditionsSuccess.add(condition);
  }
  
  public List<Condition> getPostConditionsSuccess() {
    return postConditionsSuccess;
  }
  
  public void addPostConditionFailed(Condition condition) {
    postConditionsFailed.add(condition);
  }
  
  public List<Condition> getPostConditionsFailed() {
    return postConditionsFailed;
  }
}