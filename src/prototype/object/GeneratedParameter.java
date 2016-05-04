package prototype.object;

import java.util.ArrayList;
import java.util.List;

public class GeneratedParameter extends Parameter {
  private List<CombinedANDConditions> preConditions;
  private List<CombinedANDConditions> postConditionsSuccess;
  private List<CombinedANDConditions> postConditionsFailed;
  
  public GeneratedParameter(String name, Type type, int count, List<Value> values) {
    super.name = name;
    super.type = type;
    super.count = count;
    super.values = values;
    
    this.preConditions = new ArrayList();
    this.postConditionsSuccess = new ArrayList();
    this.postConditionsFailed = new ArrayList();
  }
  
  public GeneratedParameter(String name) {
    this(name, Type.Categorical, 1, new ArrayList());
    
    List<Category> categories = new ArrayList();
    categories.add(new Category(1, "Success"));
    categories.add(new Category(2, "Failed"));
    
    List<Value> initValues = new ArrayList();
    initValues.add(new Value("result", 0, "", "", false, 0, 0, categories));
    super.values = initValues;    
  }
  
  public void setPreConditions(List<CombinedANDConditions> conditions) {
    preConditions = conditions;
  }
    
  public void addPreCondition(CombinedANDConditions condition) {
    preConditions.add(condition);
  }
  
  public List<CombinedANDConditions> getPreConditions() {
    return preConditions;
  }
  
  public void setPostConditionsSuccess(List<CombinedANDConditions> conditions) {
    postConditionsSuccess = conditions;
  }
    
  public void addPostConditionSuccess(CombinedANDConditions condition) {
    postConditionsSuccess.add(condition);
  }
  
  public List<CombinedANDConditions> getPostConditionsSuccess() {
    return postConditionsSuccess;
  }
  
  public void setPostConditionsFailed(List<CombinedANDConditions> conditions) {
    postConditionsFailed = conditions;
  }
    
  public void addPostConditionFailed(CombinedANDConditions condition) {
    postConditionsFailed.add(condition);
  }
  
  public List<CombinedANDConditions> getPostConditionsFailed() {
    return postConditionsFailed;
  }
}