package prototype.object;

public class Range<T extends Number>  {

    private final T low;
    private final T high;

    public Range(T low, T high){
        this.low = low;
        this.high = high;
    }

    public boolean contains(T number){
        return (number.doubleValue() >= low.doubleValue() && number.doubleValue() <= high.doubleValue());
    }
    
    public T GetMin() {
      return low;
    }
    
    public T GetMax() {
      return high;
    }
    
    @Override
    public String toString() {      
      return String.format("[%d - %d]", this.low, this.high);
    }
}