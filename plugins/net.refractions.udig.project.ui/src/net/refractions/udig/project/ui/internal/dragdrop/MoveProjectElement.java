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
package net.refractions.udig.project.ui.internal.dragdrop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import net.refractions.udig.project.internal.Project;
import net.refractions.udig.project.internal.ProjectElement;
import net.refractions.udig.project.internal.ProjectPlugin;
import net.refractions.udig.ui.IDropAction;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

/**
 * Move Project elements between projects
 * 
 * @author Jesse
 * @since 1.1.0
 */
public class MoveProjectElement extends IDropAction {

    @Override
    public boolean accept() {
        Collection<ProjectElement> elements = toProjectElements();
        if (elements == null)
            return false;

        return true;
    }

    private Collection<ProjectElement> toProjectElements() {
        if (getData() instanceof ProjectElement) {
            ProjectElement element = (ProjectElement) getData();
            return Collections.singleton(element);
        }

        if (getData() instanceof EObject) {
            EObject eobj = (EObject) getData();
            while( eobj != null && !(eobj instanceof ProjectElement) )
                eobj = eobj.eContainer();

            if (eobj instanceof ProjectElement)
                return Collections.singleton((ProjectElement) eobj);

            return null;
        }

        if (getData() instanceof Collection<?>) {
            Collection<ProjectElement> elements = new HashSet<ProjectElement>();
            Collection<?> data = (Collection<?>) getData();
            for( Object object : data ) {
                if (object instanceof ProjectElement) {
                    ProjectElement element = (ProjectElement) object;
                    elements.add(element);
                }

                if (object instanceof EObject) {
                    EObject eobj = (EObject) object;
                    while( eobj != null && !(eobj instanceof ProjectElement) )
                        eobj = eobj.eContainer();

                    if (eobj instanceof ProjectElement)
                        elements.add((ProjectElement) eobj);
                }
            }
            
            if( !elements.isEmpty() )
                return elements;
        }
        return null;
    }
    @Override
    public void perform( IProgressMonitor monitor ) {
        Collection<ProjectElement> elements = toProjectElements();
        
        Project destination=(Project) getDestination();

        
        Collection<Project> projects = new ArrayList<Project>();
        projects.add(destination);

        for( ProjectElement projectElement : elements ) {
            Project projectInternal = projectElement.getProjectInternal();
            if( projectInternal!=null){
                projects.add(projectInternal);
            }
        }
        
        Collection<String> messages = ProjectPlugin.saveProjects(projects);
        if( !messages.isEmpty() ){
            MessageDialog
                    .openError(
                            Display.getDefault().getActiveShell(),
                            "Error saving projects",
                            "An error occurred while attempting to save projects.  Please verify you have write access to the project files and no other applications have locked the files.");
            return;
        }
        
        for( ProjectElement projectElement : elements ) {
            Project projectInternal = projectElement.getProjectInternal();
            if( projectInternal!=null){
                projectInternal.getElementsInternal().remove(projectElement);
            }
        }
        
        destination.getElementsInternal().addAll(elements);
        
        Collection<String> errors = ProjectPlugin.saveProjects(projects);
        
        if (!errors.isEmpty()) {
            MessageDialog
                    .openError(
                            Display.getDefault().getActiveShell(),
                            "Error saving projects",
                            "An error occurred while attempting to save projects.  Please verify you have write access to the project files and no other applications have locked the files.");
            return;
        }
    }

}
