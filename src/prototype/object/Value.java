package prototype.object;

import java.util.ArrayList;
import java.util.List;

public class Value {
  private String name;
  private int index;
  private String unit;
  private String decimalMask;
  private boolean rangeEnabled;
  private int min;
  private int max;
  private List<Category> categories;
  
  public Value() {
    this("", 0, "", "", false, 0, 0, new ArrayList());
  }
  
  public Value(String name, int index, String unit, String decimalMask, boolean rangeEnabled, int min, int max, List<Category> categories) {
    this.name = name;
    this.index = index;
    this.unit = unit;
    this.decimalMask = decimalMask;
    this.rangeEnabled = rangeEnabled;
    this.min = min;
    this.max = max;
    this.categories = categories;
  }

  public String getName() {
      return this.name;
  }
  
  public void setName(String name) {
    this.name = name;
  }

  public int getIndex() {
      return this.index;
  }
  
  public void setIndex(int index) {
    this.index = index;
  }

  public String getUnit() {
      return this.unit;
  }
    
  public void setUnit(String unit) {
    this.unit = unit;
  }

  public String getDecimalmask() {
      return this.decimalMask;
  }
    
  public void setDecimalmask(String decimalMask) {
    this.decimalMask = decimalMask;
  }
  
  public void setMin(int min) {
    this.min = min;
  }

  public boolean getRangeEnabled() {
    return rangeEnabled;
  }
  
  public void setRangeEnabled(boolean rangeEnabled) {
    this.rangeEnabled = rangeEnabled;
  }
  
  public int getMin() {
    return this.min;
  }
  
  public void setMax(int max) {
    this.max = max;
  }

  public int getMax() {
    return this.max;
  }
  
  public boolean aboveMin(double value) {
    if(this.rangeEnabled) {
      if(value < this.min)
        return false;
    }
    return true;
  }
  
  public boolean belowMax(double value) {
    if(this.rangeEnabled) {
      if(value > this.max)
        return false;
    }
    return true;
  }
  
  public void setgetCategories(List<Category> categories) {
    this.categories = categories;
  }

  public List<Category> getCategories() {
      return this.categories;
  }
  
  public int getCategoryIndex(int categoryValue) {
    for(int i = 0; i < this.categories.size(); i++) {
      if(this.categories.get(i).getValue() == categoryValue)
        return i;
    }
    return -1;
  }
  
  public Category getCategory(int categoryValue) {
    int categoryIndex = getCategoryIndex(categoryValue);
    if(categoryIndex >= 0)
      return this.categories.get(categoryIndex);
    else
      return null;
  }
    
  public String getCategoryName(int categoryValue) {
    Category category = getCategory(categoryValue);
    if(category != null)
      return category.getName();
    else
      return "";
  }
    
  public int getCategoryValue(String categoryName) {
    for (Category categorie : this.categories) {
      if (categorie.getName().equals(categoryName)) {
        return categorie.getValue();
      }
    }
    return -1;
  }
  
  public Value copy() {
    ArrayList<Category> categoriesCopy = new ArrayList();
    for(Category category : this.categories) {
      categoriesCopy.add(new Category(category.getValue(), category.getName()));
    }
    return new Value(this.name, this.index, this.unit, this.decimalMask, this.rangeEnabled, this.min, this.max, categoriesCopy);
  }
  
  @Override
  public String toString() {
      return String.format("[%d] %s (%s)", this.index, this.name, this.unit);
  }
}