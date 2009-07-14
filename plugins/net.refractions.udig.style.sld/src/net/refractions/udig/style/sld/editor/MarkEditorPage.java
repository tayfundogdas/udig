package net.refractions.udig.style.sld.editor;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.Page;

/**
 * 
 * This class handles the specifics of a 
 * <p>
 *
 * </p>
 * @author mleslie
 * @since 1.1.0
 */
public class MarkEditorPage extends Page {
    private static String[] WELL_KNOWN_NAMES = {"square", "circle", "triangle", "star", "cross", "X"};
    
    Composite markComposite;
    private Label titleLabel;
    private ComboViewer markerTypeCombo;
    
    /**
     * This is fairly straight forward, as we only really accept the name of a marker and
     * then defer two sections to Fill and Stroke Editors.  It will not set layout data on the
     * returned composite, leaving that for the calling method.
     *
     * @param parent
     * @return
     */
    public void createControl( Composite parent ) {
        markComposite = new Composite(parent, SWT.NONE);
        markComposite.setLayout(new GridLayout());
        titleLabel = new Label(markComposite, SWT.NONE);
        titleLabel.setText("Mark");
        titleLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
        Composite additionalComposite = new Composite(markComposite, SWT.NONE);
        additionalComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        additionalComposite.setLayout(new TableWrapLayout());
    }

    @Override
    public Control getControl() {
        return markComposite;
    }

    @Override
    public void setFocus() {
    }
    
}
