/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package net.refractions.udig.tools.edit;

/**
 * Enumerates the type of Events that a {@link net.refractions.udig.tools.edit.EventBehaviour} can react to.
 * 
 * @author jones
 * @since 1.1.0
 */
public enum EventType {
    DOUBLE_CLICK, DRAGGED, ENTERED, EXITED, MOVED, PRESSED, RELEASED, WHEEL, HOVERED;
}