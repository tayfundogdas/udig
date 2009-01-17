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
package net.refractions.udig.tools.edit.behaviour;

import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.project.command.UndoRedoCommand;
import net.refractions.udig.project.command.UndoableComposite;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.tools.edit.EditPlugin;
import net.refractions.udig.tools.edit.EditState;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.EventBehaviour;
import net.refractions.udig.tools.edit.EventType;
import net.refractions.udig.tools.edit.commands.SelectVertexCommand;
import net.refractions.udig.tools.edit.commands.SetEditStateCommand;
import net.refractions.udig.tools.edit.commands.SelectVertexCommand.Type;
import net.refractions.udig.tools.edit.preferences.PreferenceUtil;
import net.refractions.udig.tools.edit.support.EditBlackboard;
import net.refractions.udig.tools.edit.support.EditGeom;
import net.refractions.udig.tools.edit.support.Point;
import net.refractions.udig.tools.edit.support.Selection;

import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * <p>Requirements:
 * <li>handler has currentGeom</li>
 * <li>edit state is modified or NONE</li>
 *</ul>
 * <li>sets Edit State to Modified</li>
 *</ul>
 * @author jones
 * @since 1.1.0
 */
public class SelectVertexOnMouseDownBehaviour implements EventBehaviour {
    
    public boolean isValid( EditToolHandler handler, MapMouseEvent e, EventType eventType ) {
        boolean currentGeomNotNull = handler.getCurrentGeom()!=null;
        boolean eventTypePressed = eventType==EventType.PRESSED;
        boolean button1Changed=e.button==MapMouseEvent.BUTTON1;
        EditBlackboard editBlackboard = handler.getEditBlackboard(handler.getEditLayer());
        boolean overPoint = editBlackboard.overVertex(Point.valueOf(e.x,e.y), PreferenceUtil.instance().getVertexRadius())!=null;

        return currentGeomNotNull && eventTypePressed && button1Changed
        && !e.modifiersDown() && e.buttons==MapMouseEvent.BUTTON1 && overPoint;
    }

    public UndoableMapCommand getCommand( EditToolHandler handler, MapMouseEvent e, EventType eventType ) {
        if( !isValid(handler, e, eventType) )
            throw new IllegalArgumentException("Not valid state", new Exception()); //$NON-NLS-1$

        EditBlackboard editBlackboard = handler.getEditBlackboard(handler.getEditLayer());
        Point point = handler.getEditBlackboard(handler.getEditLayer()).overVertex(Point.valueOf(e.x,e.y), PreferenceUtil.instance().getVertexRadius());
        List<EditGeom> geoms = null;
        if( point != null )
            geoms= editBlackboard.getGeoms(point.getX(),point.getY());
        else{
            EditPlugin.trace(EditPlugin.SELECTION, "VertexSelectorBehaviour: Not over vertex (" //$NON-NLS-1$
                    +e.x+","+e.y+")", null); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        }
        Selection selection = editBlackboard.getSelection();
        List<UndoableMapCommand> commands=new ArrayList<UndoableMapCommand>();

        if( !selection.contains(point) )
            commands.add(  new SelectVertexCommand(editBlackboard, point, Type.SET) );

        if( geoms!=null && geoms.contains(handler.getCurrentGeom()) ){
            if( handler.getCurrentState()==EditState.NONE){
                commands.add( new SetEditStateCommand( handler, EditState.MODIFYING ) );
            }
        }
        if( commands.size() == 0)
            return null;
        else{
            UndoableComposite undoableComposite = new UndoableComposite(commands);
            undoableComposite.setMap(handler.getContext().getMap());
            try {
                undoableComposite.execute(new NullProgressMonitor());
            } catch (Exception e1) {
                throw (RuntimeException) new RuntimeException( ).initCause( e1 );
            }
            return new UndoRedoCommand(undoableComposite);
        }
    }

    public void handleError( EditToolHandler handler, Throwable error, UndoableMapCommand command ) {
        EditPlugin.log("", error); //$NON-NLS-1$
    }

}