package prototype.object;

import java.util.Comparator;

public class DateComparator implements Comparator<LayoutChart> {

  @Override
  public int compare(LayoutChart o1, LayoutChart o2) {
    if(o1.getTime() == null)
      return -1;
    if(o2.getTime() == null)
      return 1;
    
    return o1.getTime().compareTo(o2.getTime());
  }

}