package prototype.object;

public class Category {
  private int value;
  private String name;
            
  public Category() {
    this(0, "");
  }
  
  public Category(int value, String name) {
    this.value = value;
    this.name = name;
  }

  public int getValue() {
      return this.value;
  }
  
  public void setValue(int value) {
    this.value = value;
  }

  public String getName() {
      return this.name;
  }
  
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
      return String.format("[%d] %s", this.value, this.name);
  }
}