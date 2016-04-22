/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package prototype.listener;

import java.util.EventListener;
import java.util.List;

public interface SelectionEventListener extends EventListener {
  public void timeFrameSelected(int startIndex, int endIndex, boolean drag, boolean forward);
  public void update();
}
