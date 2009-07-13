package net.refractions.udig.style.sld.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class PointEditorPage extends StyleEditorPage {

    public PointEditorPage() {
    }

    @Override
    public void createPageContent( Composite parent ) {
        Label label = new Label(parent, SWT.DEFAULT);
        label.setText("Hello World");
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        label.setLayoutData(gd);                
    }

    @Override
    public String getErrorMessage() {
        // check over content and return
        // non null if in error
        return null;
    }

    @Override
    public String getLabel() {
        return null; // not sure what this is for we need to update the javadocs
    }

    @Override
    public void gotFocus() {
        //refresh();
        //dirty = false;
    }

    @Override
    public boolean performCancel() {
        return true;
    }

    @Override
    public void styleChanged( Object source ) {
        
    }

    public boolean okToLeave() {
        return true;
    }

    public boolean performApply() {
        return true;
    }

    public boolean performOk() {
        return true;
    }

    public void refresh() {
    }

}
