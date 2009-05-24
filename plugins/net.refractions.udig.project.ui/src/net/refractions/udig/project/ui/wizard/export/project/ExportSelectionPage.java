/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.refractions.udig.project.ui.wizard.export.project;

import java.io.File;
import java.util.List;

import net.refractions.udig.project.internal.Project;
import net.refractions.udig.project.internal.ProjectPlugin;
import net.refractions.udig.project.internal.ProjectRegistry;

import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class ExportSelectionPage extends WizardPage {

    private DirectoryFieldEditor editor;
    private ComboFieldEditor project;
    private Project selectedProject;
    
    public ExportSelectionPage(String title, String description, ImageDescriptor pageIcon) {
        super("Export Selection Page", title, pageIcon);
        setDescription(description);
    }
    
    public void createControl( Composite parent ) {
        Composite fileSelectionArea = new Composite(parent, SWT.NONE);
        GridData fileSelectionData = new GridData(GridData.GRAB_HORIZONTAL
                | GridData.FILL_HORIZONTAL);
        fileSelectionArea.setLayoutData(fileSelectionData);
        fileSelectionArea.setLayout(new GridLayout(3, false));
        createProjectEditor( fileSelectionArea );
        createFileEditor(fileSelectionArea);
        fileSelectionArea.moveAbove(null);
        setControl(fileSelectionArea);
        setPageComplete(false);
        setMessage(null);
        setErrorMessage(null);
    }

    private void createProjectEditor( Composite parent ) {
        ProjectRegistry registery = ProjectPlugin.getPlugin().getProjectRegistry();
        List<Project> list = registery.getProjects();
        String[][] projects = new String[ list.size()][];
        int index = 0;
        for( Project project : list ){
            projects[index]=new String[]{ project.getName(), project.getID().toString()};
            index++;
        }
        if( getDialogSettings() != null){
            if( registery.getCurrentProject() != null ){
                URI uri = registery.getCurrentProject().getID();
                if( uri != null ){
                    String selected = uri.toFileString();
                    this.getDialogSettings().put("projectSelect", selected );
                }                        
            }
        }        
        project = new ComboFieldEditor("projectSelect", "Project:", projects, parent );
        project.setPage( this );        
        project.setPropertyChangeListener( new IPropertyChangeListener(){
            public void propertyChange( PropertyChangeEvent event ) {
                selectProject( (String) event.getNewValue() );
                check();
            }            
        });
    }
    
    private void createFileEditor( Composite parent ) {
        editor = new DirectoryFieldEditor("directorySelect", "Destination: ", parent){ //$NON-NLS-1$
            {
                setValidateStrategy( VALIDATE_ON_KEY_STROKE );
                setEmptyStringAllowed(false);                
            }
            @Override
            public boolean isValid() {
                File file = new File(getStringValue());
                if( file.isDirectory()){
                    return true;
                }
                else {
                    setErrorMessage("Please select a directory to export the project into");
                    return false;
                }
            }
        };
        editor.setPage(this);
        editor.getTextControl(parent).addModifyListener(new ModifyListener(){
            public void modifyText( ModifyEvent e ) {
                check();
            }
        });
    }
    public void check(){
        if( !editor.isValid()){
            setPageComplete( false );
            setMessage( editor.getErrorMessage(), ERROR );
            return;
        }
        if( getProject() == null ){
            setPageComplete( false );
            setErrorMessage("Please select a project to export" );
            return;
        }
        setPageComplete(true);
        setMessage("Export the selected project", INFORMATION );
    }
    public void selectProject( String uri ){
        ProjectRegistry registery = ProjectPlugin.getPlugin().getProjectRegistry();
        List<Project> list = registery.getProjects();
        for( Project project : list ){
           if( uri.equals( project.getID().toString() ) ){
               selectedProject = project;
               return;
           }
        }
        selectedProject = null;
    }
    public Project getProject(){
        return selectedProject;
    }
    public String getDestinationDirectory(){
        return editor.getStringValue();
    }
}
