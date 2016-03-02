/**
 * @file TaskBar.java
 * @brief Bottom horizontal bar where minimized nodes are displayed
 *
 * @section License
 *
 *          This file is a part of the DockFX Library. Copyright (C) 2015 Robert B. Colton
 *
 *          This program is free software: you can redistribute it and/or modify it under the terms
 *          of the GNU Lesser General Public License as published by the Free Software Foundation,
 *          either version 3 of the License, or (at your option) any later version.
 *
 *          This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *          WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *          PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 *          You should have received a copy of the GNU Lesser General Public License along with this
 *          program. If not, see <http://www.gnu.org/licenses/>.
 **/

package org.dockfx.taskBar;

import java.util.HashMap;
import java.util.Map;

import org.dockfx.DockNode;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * Horizontal bar where TaskBarItems are displayed.
 *
 */
public class TaskBar extends HBox {

  /**
   * Height of the task bar.
   */
  public static final int TASK_BAR_HEIGHT = 25;

  public static double BACKGROUND_COLOR = 0.9;

  private Map<DockNode, TaskBarItem> taskBarItems;

  /**
   * Creates TaskBar as HBox and sets height.
   */
  public TaskBar() {
    super();
    this.setMinHeight(TASK_BAR_HEIGHT);
    this.setBackground(new Background(new BackgroundFill(Color.color(BACKGROUND_COLOR, BACKGROUND_COLOR, BACKGROUND_COLOR), null, null)));
    taskBarItems = new HashMap<DockNode, TaskBarItem>();
  }

  /**
   * Adds TaskBatItem to TaskBar. Called when node is minimized.
   *
   * @param dockNode Node that was minimized.
   */
  public void addTaskBarItemForNode(DockNode dockNode) {
    TaskBarItem taskBarItem = new TaskBarItem(dockNode);
    getChildren().add(taskBarItem);
    taskBarItems.put(dockNode, taskBarItem);
  }

  public void removeTaskBarItemForNode(DockNode dockNode) {
    getChildren().remove(taskBarItems.get(dockNode));
    taskBarItems.remove(dockNode);
  }
}
