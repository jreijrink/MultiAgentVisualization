package prototype.object;

import java.util.Comparator;

public class DateComparator implements Comparator<LayoutChart> {

  @Override
  public int compare(LayoutChart o1, LayoutChart o2) {
    if(o1.GetTime() == null)
      return -1;
    if(o2.GetTime() == null)
      return 1;
    
    return o1.GetTime().compareTo(o2.GetTime());
  }

}