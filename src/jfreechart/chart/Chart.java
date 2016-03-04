/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfreechart.chart;

import java.util.EventListener;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javax.swing.event.EventListenerList;
import static jfreechart.Parser.MAX_TURTLES;
import jfreechart.listener.SelectionEventListener;
import jfreechart.object.StringValuePair;
import jfreechart.object.TimeFrame;

public interface Chart extends EventListener {
  
  EventListenerList listenerList = new EventListenerList();
  
  public void updateData(List<TimeFrame> data);
  
  public String getName();
  public Node getNode();
  
  public void addSelectionEventListener(SelectionEventListener listener);
  public void selectFrames(int startIndex, int endIndex, boolean drag);
  
  public void showParameterDialog();
  
  
  static ListView getTurtleListView(int[] selected) {
    ListView<StringValuePair<String, Integer>> turtleListView = new ListView();              
    ObservableList<StringValuePair<String, Integer>> turtleList = FXCollections.observableArrayList();
    turtleListView.setItems(turtleList);
    for(int i = 0; i < MAX_TURTLES; i++) {
      turtleList.add(new StringValuePair(String.format("Turtle %d", i+1), i));
    }

    turtleListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    for(int selection : selected) {
      turtleListView.getSelectionModel().select(selection);
    }        
    turtleListView.setPrefHeight(200);
    return turtleListView;
  }
  
  static CheckBox getCheckbox(String name, boolean checked) {
    CheckBox newbox = new CheckBox(name);
    newbox.setSelected(checked);
    return newbox;
  }
}