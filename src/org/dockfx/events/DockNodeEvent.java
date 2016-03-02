/**
 * @file DockNodeEvent.java
 * @brief DockNode event object
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

import org.dockfx.DockNode;

/**
 * DockNodeEvent
 *
 * @since DockFX 0.1
 */
public class DockNodeEvent {

  /**
   * Source dock node of the event
   */
  private DockNode source;

  /**
   * Creates new event object
   *
   * @param source Source node of the event
   */
  public DockNodeEvent(DockNode source) {
    this.source = source;
  }

  /**
   * Return source node of the event
   *
   * @return Source node of the event
   */
  public DockNode getSource() {
    return source;
  }

}
