/**
 * @file DockNodeEventListener.java
 * @brief Adapter class for DockNodeEventListenerInterface
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
package org.dockfx.events;

/**
 * Adapter class for {@link org.dockfx.events.DockNodeEventListenerInterface}
 *
 * @since DockFX 0.1
 */
public class DockNodeEventListener implements DockNodeEventListenerInterface {

  /**
   * Fired when node is closed
   *
   * @param e Event object
   */
  public void dockNodeClosed(DockNodeEvent e) {}

  /**
   * Fired when node is maximized
   *
   * @param e Event object
   */
  public void dockNodeMaximized(DockNodeEvent e) {}

  /**
   * Fired when node is windowed and was maximized
   *
   * @param e Event object
   */
  public void dockNodeWindowed(DockNodeEvent e) {}

  /**
   * Fired when node is minimized
   *
   * @param e Event object
   */
  public void dockNodeMinimized(DockNodeEvent e) {}

  /**
   * Fired when restored to floating window state from minimized or maximized state
   *
   * @param e Event object
   */
  public void dockNodeRestored(DockNodeEvent e) {}

  /**
   * Fired when node is docked
   *
   * @param e Event object
   */
  public void dockNodeDocked(DockNodeEvent e) {}

  /**
   * Fired when node is floated
   *
   * @param e Event object
   */
  public void dockNodeFloated(DockNodeEvent e) {}

  /**
   * Fired when node gains focus
   *
   * @param e Event object
   */
  public void dockNodeFocused(DockNodeEvent e) {}

  /**
   * Fired when node loses focus
   *
   * @param e Event object
   */
  public void dockNodeDefocused(DockNodeEvent e) {}

}
