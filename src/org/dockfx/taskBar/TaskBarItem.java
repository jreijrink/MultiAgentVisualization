/**
 * @file TaskBarItem.java
 * @brief View added to TaskBar when DockNode is minimized
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

import org.dockfx.DockNode;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

/**
 * View representing minimized DockNode in TaskBar.
 */
public class TaskBarItem extends HBox {

  /**
   * Default preferred width of TaskBarItem
   */
  public static final int TASK_BAR_ITEM_PREF_WIDTH = 150;

  /**
   * Displays title of minimized DockNode
   */
  Label label;

  /**
   * Hides this TaskBarItem and restores the DockNode to floating state
   */
  Button button;

  /**
   * Fills space between Label and Button
   */
  Pane fillPane;

  /**
   * Node that was minimized
   */
  DockNode dockNode;

  /**
   * Creates TaskBarItem with title and graphic of passed DockNode.
   *
   * @param dockNode Minimized dockNode.
   */
  public TaskBarItem(DockNode dockNode) {
    super();
    this.dockNode = dockNode;
    setPrefWidth(TASK_BAR_ITEM_PREF_WIDTH);
    setPadding(new Insets(3));

    label = new Label("Title");
    label.textProperty().bind(dockNode.titleProperty());

    if (dockNode.getGraphic() instanceof ImageView) {
      label.setGraphic(new ImageView(((ImageView)dockNode.getGraphic()).getImage()));
    }

    button = new Button();
    button.getStyleClass().add("dock-state-button");
    button.setOnAction(new EventHandler<ActionEvent>() {

      public void handle(ActionEvent event) {
        handleClick();
      }
    });

    this.setOnMouseClicked(new EventHandler<MouseEvent>() {
      public void handle(MouseEvent event) {
        handleClick();
      }
    });

    fillPane = new Pane();
    HBox.setHgrow(fillPane, Priority.ALWAYS);

    // this.getStyleClass().add("dock-title-bar");
    this.getStyleClass().add("task-bar-item");
    getChildren().addAll(label, fillPane, button);

  }

  /**
   * Handles click on restore button or on TaskBarItem itself. Removes this TaskBarItem from TaskBar
   * and restores minimized DockNode.
   */
  private void handleClick() {
    dockNode.setMinimized(false);
  }
}
