/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfreechart;

import java.util.EventListener;
import java.util.List;

public interface SelectionEventListener extends EventListener {
  public void timeFrameSelected(int startIndex, int endIndex, boolean drag);  
}
