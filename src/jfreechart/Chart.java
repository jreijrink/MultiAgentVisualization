/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfreechart;

import java.util.EventListener;
import java.util.List;
import javafx.scene.Node;
import javax.swing.event.EventListenerList;

public interface Chart extends EventListener {
  
  EventListenerList listenerList = new EventListenerList();
  
  public void updateData(List<TimeFrame> data);
  
  public String getName();
  public Node getNode();
  
  public void addSelectionEventListener(SelectionEventListener listener);
  public void selectFrames(int startIndex, int endIndex, boolean drag);
}
